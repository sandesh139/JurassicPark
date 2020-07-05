import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

// The MainController is responsible for all the back-end logic of the simulation.
// It will communicate with the GUI only by updating it's simState Map.
public class MainController implements Runnable, InputEventListener{
    // Globals
    // =======

    // this map is the go-between for the "back-end" and the "front-end"
    private ConcurrentHashMap<ID, Asset> simState = new ConcurrentHashMap<>();
    // All available wristbands
    private ConcurrentLinkedQueue<Integer> inactiveUniqueIds = new ConcurrentLinkedQueue<>();
    private Log log;

    // Queue for holding patrons who need to be assigned to cars
    private ConcurrentLinkedQueue<Patron> needAssigning = new ConcurrentLinkedQueue<>();

    private long lastDepartureTime = 0;

    private boolean emergency = false;

    private SafetyBunker[] safetyBunker;

    private InputController inputController;

    public MainController() {
        inputController = new InputController(this);
        inputController.setupWristbandScanners();
    }

    // Public methods
    // ==============
    public void setLog(Log log){
        this.log = log;
    }

    // getSimState returns the simulation state
    // This is intended to be the only way the GUI know about
    // the state of the simulation.
    public ConcurrentHashMap<ID, Asset> getSimState() {
        return this.simState;
    }

    @Override
    synchronized public void wristbandScannedForEntry(Integer uniqueId)  {
        Patron patron = new Patron(uniqueId);
        simState.put(patron.getID(), new Asset(AssetType.Patron, patron));
        needAssigning.add(patron);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> assignToCar(), 1000, TimeUnit.MILLISECONDS);
        scheduler.shutdown();
    }

    @Override
    synchronized public void wristbandScannedForExit(Integer uniqueId) {
        inactiveUniqueIds.add(uniqueId);
    }

    @Override
    public void setEmergencyMode(boolean enable) {
        if (emergency == enable) return;
        emergency = enable;
        runEmergency(enable);
    }

    // Private methods
    // ===============

    synchronized private long getLastDepartureTime(){
        return lastDepartureTime;
    }

    synchronized private void setLastDepartureTime(long time){
        lastDepartureTime = time;
    }

    boolean safetyBunkersEmpty() {
        for (SafetyBunker s : safetyBunker) {
            if (s.getNumOccupants() != 0) {
                return false;
            }
        }

        return true;
    }

    // start creating people to come
    private void createPatrons() {
        TimerTask createPatronTimerTask = new TimerTask() {
            @Override
            public void run() {
                // Simulate arriving patrons
                int numPatron = (int) (Math.random() * 10) + 1;

                boolean bunkersEmpty = safetyBunkersEmpty();

                if (!emergency && bunkersEmpty) {
                    System.out.println(numPatron + " arrived");
                    log.updateMainLog(numPatron + " patrons entered the park");
                }

                for (int i = 0; i < numPatron; i++) {
                    // simulate taking a wristband
                    if (!inactiveUniqueIds.isEmpty() && !emergency && bunkersEmpty) {
                        Integer id = inactiveUniqueIds.poll();
                        // the incoming patrons' wristbands are scanned
                        WristbandScanners.ENTRY_SCANNER.scanWristband(id);
                    } else {
                        if (!emergency && bunkersEmpty) {
                            System.out.println("Ran out of IDs!!!");
                            log.updateMainLog("Ran out of IDs!!!");
                        }
                    }
                }
                System.out.println("Available ids = " + inactiveUniqueIds.size());
                log.updateMainLog("Available ids = " + inactiveUniqueIds.size());
            }
        };
        Timer createPatronTimer = new Timer(true);
        createPatronTimer.scheduleAtFixedRate(createPatronTimerTask, 0, 2000);
    }

    private void runEmergency(boolean enable){
        if (enable) {
            System.out.println("--------------------------Emergency is on-----------------------------");
            log.updateMainLog("EMERGENCY ON");
            // Everyone who is waiting should leave park
            for (Patron p : needAssigning) {
                if (p.getNeedsAssigning()) {
                    p.exitPark();
                    needAssigning.remove(p);
                }
            }
        } else {
            System.out.println("---------------------------Emergency is off------------------");
            log.updateMainLog("EMERGENCY OFF");
        }

        for (Asset a : simState.values()) {
            switch (a.getType()) {
                case Car:
                    Car c = (Car) a.getAsset();
                    c.setEmergency(enable);
                    if (!enable) {
                        for (SafetyBunker s : safetyBunker) {
                            if (s.getNumOccupants() != 0 && s.getNeedsPickup()) {
                                c.pickupAtBunker(s);
                                s.setNeedsPickup(false);
                                break;
                            }
                        }
                    }
                    break;
                case Patron:
                    Patron p = (Patron) a.getAsset();
                    if (p.getState() != MovementState.LEFT_PARK) {
                        p.setEmergency(enable, safetyBunker);
                    }
                    break;
                case PopupWall:
                    PopupWalls pw = (PopupWalls) a.getAsset();
                    if (enable) {
                        pw.activate();
                    } else {
                        pw.deactivate();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    // Make 1 car and give to main controller
    private void makeCar(int num) {
        ArrayList<Car> cars = new ArrayList<>();

        for (int i = 0; i < num; i++){
            cars.add(new Car(log));
            setAllCars(cars);
        }
    }

    // Make 1000 unique id and give to create patron task
    private void makeIDs() {
        for (int i = 1; i <= 1000; i++){
            inactiveUniqueIds.add(100000+i);
        }
    }

    private void makeBunkers(){
        safetyBunker = new SafetyBunker[]{new SafetyBunker(285, 190), new SafetyBunker(285, 642)};
        for (SafetyBunker bunker : safetyBunker){
            simState.put(bunker.getId(), new Asset(AssetType.Bunker, bunker));
        }
    }
/*
    private void initInputController(){
        inputController = new InputController(this);
        inputController.setupWristbandScanners();
    }
*/

    public void setAllCars(ArrayList<Car> cars){
        for (Car c : cars) {
            simState.put(c.getID(), new Asset(AssetType.Car, c));
        }
    }

    public void createWall(){
        PopupWalls p = new PopupWalls();
        simState.put(p.getId(), new Asset(AssetType.PopupWall, p));
    }

    synchronized private void assignToCar() {
        while (!needAssigning.isEmpty() && !emergency && safetyBunkersEmpty()){
            boolean carIsFull;
            Car available = null;

            // for every car in the simulation state
            ForAsset:
            for (Asset a : simState.values()) {
                switch (a.getType()) {
                case Car:
                    Car c = (Car) a.getAsset();
                    if (c.getState() == MovementState.IDLE && !c.carIsFull() && !c.isScheduled()) {
                        available = c;
                        break ForAsset;
                    }
                    break;
                default:
                    break;
                }
            }

            if (available != null && available.getState() == MovementState.IDLE){
                // available car found and put person in car
                Patron p = needAssigning.poll();
                if (p.getState() != MovementState.LEFT_PARK){
                    p.setNeedsAssigning(false);
                    p.setState(MovementState.IN_CAR);
                    carIsFull = available.assignToCar(p);

                    if (carIsFull) {
                        long currTime = System.currentTimeMillis();
                        long timeDelay = currTime - getLastDepartureTime();
                        available.startTrip(timeDelay);
                        if (timeDelay <= 2000){
                            setLastDepartureTime(currTime + timeDelay);
                        }
                        else {
                            setLastDepartureTime(currTime);
                        }
                    }
                }
            }

            else {
                break;
            }
        }
    }

    public InputController getInputController() {
        return inputController;
    }

    // "Main"
    // ======

    // run is like main, but is used to implement Runnable
    // so this can run on a new thread, separate from the GUI.
    @Override
    public void run() {
        // main loop timer
        System.out.println("Main Controller started");

        // initialization
        int numCars = 8;
        makeCar(numCars);
        makeIDs();
        makeBunkers();
        //initInputController();
        createWall();

        // Starts the simulation by creating new guests
        createPatrons();
    }
}