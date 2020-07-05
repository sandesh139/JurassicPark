import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Example Car Class
public class Car implements Runnable, Movable {
    private static Point2D startPos              = new Point2D(881.0, 249.0);
    private static Point2D exhibitLoc            = new Point2D(436.0, 249.0);
    private final ID id                          = new ID();
    private final Point2D idleLoc                = startPos;
    private final Point2D endLoc                 = new Point2D(881, 545);
    private final Point2D[] toExhibit            = {new Point2D(881.0, 249), new Point2D(436, 246), exhibitLoc};
    private final Point2D[] toEnd                = {new Point2D(436, 545), new Point2D(879, 545), endLoc};
    private final Point2D[] toIdle               = {idleLoc};
    private final Double[] carSize               = {31.0, 31.0};
    private Point2D pos                          = startPos;
    private Long speed                           = 10L; // ms per pixel
    private ArrayList<Patron> assignedPassengers = new ArrayList<>();
    private ArrayList<Patron> inCar              = new ArrayList<>();
    private MovementState state                  = MovementState.IDLE;
    private boolean emergency                    = false;
    private boolean needToNotify                 = false;
    private boolean handlingEmergency            = false;
    private ScheduledExecutorService scheduler   = Executors.newSingleThreadScheduledExecutor();
    private int numAtExhibit                     = 0; // counting semaphore
    private boolean scheduled                    = false;
    private Log log;

    // Constructors
    // ============
    public Car(Log log) {
        this.log = log;
        state = MovementState.IDLE;
        startPos = startPos.add(new Point2D(0.0, carSize[1] + 5));
        exhibitLoc = exhibitLoc.add(new Point2D(0.0, carSize[1] + 5));
        setPos(idleLoc);
    }

    // Getters and Setters
    // id getter
    synchronized public ID getID() {
        return this.id;
    }

    // pos getter/setter
    @Override
    synchronized public Point2D getPos() {
        return pos;
    }

    @Override
    synchronized public void setPos(Point2D pos) {
        this.pos = pos;
    }

    // speed getter/setter
    @Override
    synchronized public Long getSpeed() {
        return this.speed;
    }

    @Override
    synchronized public void setSpeed(Long speed) {
        this.speed = speed;
    }

    // state getter/setter
    synchronized public MovementState getState() {
        return state;
    }

    synchronized public void setState(MovementState state) {
        this.state = state;
    }

    // assignedPassengers getter
    synchronized public ArrayList<Patron> getAssignedPassengers() {
        return assignedPassengers;
    }

    // number of passengers getter
    synchronized public Integer getNumPassengers() {
        return getAssignedPassengers().size();
    }

    synchronized public void addToAssigned(Patron p){
        getAssignedPassengers().add(p);
    }

    synchronized private void clearAssigned(){
        getAssignedPassengers().clear();
    }

    synchronized public void setEmergency(boolean isEmergency) {
        if (emergency == isEmergency) return;
        emergency = isEmergency;
        setSpeed(8L);
        if (emergency) {
            startEmergency();
        } else {
            endEmergency();
        }
    }

    synchronized public boolean isEmergency() {
        return emergency;
    }

    synchronized private void endEmergency() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        numAtExhibit = 0;
        needToNotify = false;
        setScheduled(false);
    }

    synchronized private void startEmergency() {
        scheduler.shutdownNow();
        Thread emergencyMode = new Thread(this);
        emergencyMode.start();
    }

    synchronized private ArrayList<Patron> inCar(){
        return inCar;
    }

    synchronized private void clearInCar(){
        inCar().clear();
    }

    synchronized private void addInCar(Patron p){
        inCar().add(p);
    }

    // car size getter
    synchronized public Double[] getCarSize() {
        return carSize;
    }

    synchronized public void setScheduled(boolean enable) {
        scheduled = enable;
    }

    synchronized public boolean isScheduled() {
        return scheduled;
    }

    // Public Methods
    // ==============

    synchronized public boolean carIsFull() {
        return getNumPassengers() >= 10;
    }

    // Assign person to car, if the car is full after assigning, return true
    // If car is not full after adding, return false
    synchronized public boolean assignToCar(Patron person) {
        if (getNumPassengers() == 0) {
            // First passenger boarded, start timer
            TimerTask countWaiting = new TimerTask() {
                @Override
                public void run() {
                    if (getState() == MovementState.IDLE && !isScheduled()){
                        startTrip(5000);
                    }
                }
            };
            Timer timer = new Timer(true);
            timer.schedule(countWaiting, 5000);
        }

        addToAssigned(person);
        addInCar(person);
        person.setState(MovementState.IN_CAR);
        return carIsFull();
    }

    synchronized public void pickupAtBunker(SafetyBunker bunker) {
        Thread pickupAtBunker = new Thread(() -> {
            log.updateCarLog("Car ID " + getID().getID() + " is going to a bunker to pickup");
            System.out.println("Car is starting trip");
            setScheduled(true);
            setState(MovementState.TO_BUNKER);
            Movement.travelPath(this, toExhibit, getState());
            setState(MovementState.CAR_AT_BUNKER);
            if (isEmergency()) {
                startEmergency();
                return;
            }
            for (Patron p : bunker.getOccupants()) {
                addToAssigned(p);
                p.setCarPos(getPos());
                Thread patronReturn = new Thread(() -> {
                    if (p.notifyReturn()) {
                        addInCar(p);
                        p.setState(MovementState.IN_CAR);
                    }
                });
                patronReturn.setDaemon(true);
                patronReturn.start();

                // small delay before next patron exits bunker
                try {
                    Thread.sleep(p.getSpeed()*10);
                } catch (InterruptedException e) {
                    if (!isEmergency()) e.printStackTrace();
                }
            }

            bunker.clearBunker();

            if (isEmergency()) {
                startEmergency();
                return;
            }

            // wait until all patrons back in car
            while (inCar.size() != assignedPassengers.size()) {
                // only check once a second to not spin cpu
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    if (!isEmergency()) e.printStackTrace();
                }
            }

            if (isEmergency()) {
                startEmergency();
                return;
            }

            // travel back
            setState(MovementState.RETURNING);
            Movement.travelPath(this, toEnd, getState());

            if (isEmergency()) {
                startEmergency();
                return;
            }

            // finished trip
            log.updateCarLog("Car ID " + getID().getID() + " picked up passengers from bunker");
            System.out.println("Car ID " + getID().getID() + " picked up passengers from bunker");
            for (Patron p : inCar()) {
                p.exitPark();
            }
            clearInCar();
            clearAssigned();

            // to idle
            setState(MovementState.TO_IDLE);
            Movement.travelPath(this, toIdle, getState());
            setState(MovementState.IDLE);

            setScheduled(false);
        });
        pickupAtBunker.setDaemon(true);
        pickupAtBunker.start();
    }

    synchronized public void startTrip(long timeDelay) {
        if (isScheduled() || isEmergency()) return;

        if (timeDelay > 2000){
            timeDelay = 0;
        }

        setScheduled(true);
        scheduler.schedule(this, timeDelay, TimeUnit.MILLISECONDS);
    }

    // Private Methods
    // ===============

    // safe methods to manipulate numAtExhibit semaphore
    synchronized private int numAtExhibit() {
        return numAtExhibit;
    }

    synchronized private void incAtExhibit() {
        numAtExhibit++;
    }

    synchronized private void decAtExhibit() {
        numAtExhibit--;
    }

    synchronized private void notifyPatronReturn(Patron p){
        Thread patronReturn = new Thread(() -> {
            if (p.notifyReturn()) {
                addInCar(p);
                decAtExhibit();
            }
        });
        patronReturn.setDaemon(true);
        patronReturn.start();
    }

    // Thread
    // ======
    @Override
    public void run() {
        if (!isEmergency()) {
            normalOperation();
        }
        else {
            preemptCar();
        }
    }

    private void preemptCar(){
        if (handlingEmergency) return;

        handlingEmergency = true;

        // TODO Scheduler needs to be re-instantiated when returning to normal
        scheduler.shutdownNow();

        if (getState() == MovementState.CAR_AT_EXHIBIT){
            if (needToNotify) {
                clearInCar();
                for (Patron p : getAssignedPassengers()) {
                    notifyPatronReturn(p);
                }
            }

            // Wait for 5 seconds
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                if (!isEmergency()) e.printStackTrace();
            }
        }

        setState(MovementState.TO_IDLE);
        Movement.travelTo(this, idleLoc, getState());
        setState(MovementState.IDLE);

        for (Patron p : inCar()) {
            p.exitPark();
        }
        clearInCar();
        clearAssigned();
        handlingEmergency = false;
    }

    private void normalOperation(){
        log.updateCarLog("Car ID " + getID().getID() + " is starting trip");
        System.out.println("Car is starting trip");

        // travel to exhibit
        setState(MovementState.TO_EXHIBIT);
        Movement.travelPath(this, toExhibit, getState());
        if (isEmergency()) {
            startEmergency();
            return;
        }
        setState(MovementState.CAR_AT_EXHIBIT);

        // walk a point in the exhibit area
        needToNotify = true;
        for (Patron p : getAssignedPassengers()) {
            p.setCarPos(getPos());
            Thread patronExplore = new Thread(p);
            patronExplore.setDaemon(true);
            patronExplore.start();
            incAtExhibit();

            // small delay before next patron exits car
            try {
                Thread.sleep(p.getSpeed()*10);
            } catch (InterruptedException e) {
                if (!isEmergency()) e.printStackTrace();
                else startEmergency();
            }
        }

        if (isEmergency()) {
            startEmergency();
            return;
        }

        // spend some time at the exhibit 10 seconds
        try {
            Thread.sleep(10000); //
        } catch (InterruptedException e) {
            if (!isEmergency()) e.printStackTrace();
            else startEmergency();
        }

        // walk back to car
        clearInCar();
        needToNotify = false;
        for (Patron p : getAssignedPassengers()) {
            notifyPatronReturn(p);
        }

        if (isEmergency()) {
            startEmergency();
            return;
        }

        // Have car wait 1 second
        try {
            Thread.sleep(1000); //
        } catch (InterruptedException e) {
            if (!isEmergency()) e.printStackTrace();
            else startEmergency();
        }

        // wait until all patrons back in car
        while (numAtExhibit() > 0) {
            // only check once a second to not spin cpu
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                if (!isEmergency()) e.printStackTrace();
            }
        }

        // travel back
        setState(MovementState.RETURNING);
        Movement.travelPath(this, toEnd, getState());

        if (isEmergency()) {
            startEmergency();
            return;
        }

        // finished trip
        log.updateCarLog("Car ID " + getID().getID() + " returned to base " +
                "station");
        System.out.println("Car " + getID().getID() + " returned to base " +
                "station");
        for (Patron p : inCar()) {
            p.exitPark();
        }
        clearInCar();
        clearAssigned();

        // to idle
        setState(MovementState.TO_IDLE);
        Movement.travelPath(this, toIdle, getState());
        setState(MovementState.IDLE);
        setScheduled(false);
    }
}