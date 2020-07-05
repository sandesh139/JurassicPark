import javafx.geometry.Point2D;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SafetyBunker {
    private Point2D pos;
    private final ID id = new ID();
    private Set<Patron> occupants = ConcurrentHashMap.newKeySet();
    private boolean needsPickup = false;

    public SafetyBunker(double x, double y){
        this.pos = new Point2D(x, y);
    }

    public double getDistanceFromBunker(Point2D patronPos) {
        double xDiff = pos.getX() - patronPos.getX();
        double yDiff = pos.getY() - patronPos.getY();
        return Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
    }

    public Point2D getPos(){
        return pos;
    }

    public ID getId() {
        return id;
    }

    synchronized public void addToBunker(Patron p) {
        occupants.add(p);
        needsPickup = true;
    }

    synchronized public void clearBunker() { occupants.clear(); }

    synchronized public int getNumOccupants() {
        return occupants.size();
    }

    synchronized public Set<Patron> getOccupants() { return occupants; }

    synchronized boolean getNeedsPickup() { return needsPickup; }
    synchronized void setNeedsPickup(boolean needsPickup) { this.needsPickup = needsPickup; }
}
