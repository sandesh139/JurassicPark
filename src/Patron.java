import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import javafx.geometry.Point2D;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Patron Class
public class Patron implements Movable, Runnable {
    private static Point2D startPos     = new Point2D(0,0);
    private final Point2D exhibitOrigin = new Point2D(300, 250);
    private final ID id                 = new ID();
    private final double patronSize     = 10.0;
    private Point2D pos                 = startPos;
    private Long speed                  = 17L;
    private Integer wristbandID         = 0;
    private Boolean needsAssigning      = true;
    private boolean inBunker            = false;
    private MovementState state         = MovementState.QUEUED;
    private boolean emergency           = false;
    private Point2D carPos              = null;
    private SafetyBunker[] safetyBunker = null;


    // remove and give back when done locations to give each patron a unique spot at the exhibit
    public static ConcurrentLinkedDeque<Point2D> exhibitPositions = null;

    // Constructors
    // ===========
    public Patron (Integer id){
        wristbandID = id;
        startPos = startPos.add(new Point2D(10.0, 0.0));

        // initialize exhibit locations
        // make 400 locations
        // only make on first patron instantiation
        if (exhibitPositions == null) {
            exhibitPositions = new ConcurrentLinkedDeque<>();
            List<Point2D> positionsTemp = new ArrayList<>();
            int numExhibitLines = 0;
            for (int i = 1; i <= 400; i++) {
                Point2D nextPos;
                double maxPatrons = (420 - exhibitOrigin.getX()) / patronSize;
                nextPos = (exhibitOrigin.add(new Point2D(patronSize * (i % maxPatrons), numExhibitLines * patronSize)));
                if (i % maxPatrons == 0) {
                    numExhibitLines++;
                }
                positionsTemp.add(nextPos);
            }
            // shuffle all positions
            Collections.shuffle(positionsTemp);
            // add all position to deque
            exhibitPositions.addAll(positionsTemp);
        }
    }

    // Getters and Setters
    // ===================

    // id  getter
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

    synchronized public void setEmergency(boolean enable) {
        this.emergency = enable;
    }

    synchronized public Point2D getCarPos() {
        return carPos;
    }

    synchronized public void setCarPos(Point2D carPos) {
        this.carPos = carPos;
    }

    // patron state getter/setter
    synchronized public MovementState getState() {
        return state;
    }
    synchronized void setState(MovementState state) {
        this.state = state;
    }

    // needsAssigning getter/setter
    synchronized public Boolean getNeedsAssigning() {
        return needsAssigning;
    }
    synchronized public void setNeedsAssigning(Boolean b) {
        this.needsAssigning = b;
    }

    // wristband id getter
    synchronized public Integer getWristbandID() {
        return wristbandID;
    }

    // patron size getter
    synchronized public double getPatronSize() {
        return patronSize;
    }

    synchronized public boolean isEmergency() {
        return emergency;
    }

    synchronized public SafetyBunker[] getSafetyBunker() {
        return safetyBunker;
    }

    synchronized public void exitPark(){
        WristbandScanners.EXIT_SCANNER.scanWristband(getWristbandID());
        setState(MovementState.LEFT_PARK);
    }

    public void setEmergency(boolean isEmergency, SafetyBunker[] bunkers){
        emergency = isEmergency;
        safetyBunker = bunkers;
        setSpeed(15L);

        if (!emergency && inBunker) {
            needsAssigning = true;
        }
    }

    public boolean notifyReturn(){
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        if (isEmergency() && getState() != MovementState.GOING_CAR){
            int rand = (int) (Math.random() * 6) + 1;
            if (rand == 2){
                // Make patron take too long to get to car so they go to bunker
                scheduler.schedule(() -> toBunker(), 5500, TimeUnit.MILLISECONDS);
                scheduler.shutdown();
                inBunker = true;
                return false;
            }
        }

        // If car didn't leave them
        if (getCarPos() != null) {
            if (getState() != MovementState.IN_BUNKER) {
                Patron.exhibitPositions.add(getPos()); // give location back so someone else can use it
            }
            setState(MovementState.GOING_CAR);
            Movement.travelTo(this, getCarPos().add(-getPatronSize(), 0), getState()); // person width left of car so don't walk over car
            setState(MovementState.IN_CAR);
            return true;
        }
        else {
            scheduler.schedule(() -> toBunker(), 5000, TimeUnit.MILLISECONDS);
            scheduler.shutdown();
            inBunker = true;
            return false;
        }
    }

    private void toBunker(){
        double minDist = Double.MAX_VALUE;
        SafetyBunker destBunker = null;
        for (SafetyBunker bunker : getSafetyBunker()){
            double dist = bunker.getDistanceFromBunker(getPos());
            if (dist < minDist) {
                minDist = dist;
                destBunker = bunker;
            }
        }
        if (destBunker != null) {
            setState(MovementState.GOING_BUNKER);
            Movement.travelTo(this, destBunker.getPos(), getState());
            setState(MovementState.IN_BUNKER);
            destBunker.addToBunker(this);
        }
    }

    @Override
    public void run() {
        setPos(getCarPos().add(-getPatronSize(), 0)); // patron size left of cars current pos
        setState(MovementState.PATRON_AT_EXHIBIT);
        Movement.travelTo(this, Patron.exhibitPositions.remove(), getState()); // take one position from dequeue
    }
}