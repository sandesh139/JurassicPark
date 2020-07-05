import javafx.geometry.Point2D;

// interface for things that are moveable
public interface Movable {
    public Point2D getPos();
    public void setPos(Point2D p);
    public Long getSpeed();
    // speed is delay in ms before next movement
    public void setSpeed(Long speed);
    public MovementState getState();
    public boolean isEmergency();
}
