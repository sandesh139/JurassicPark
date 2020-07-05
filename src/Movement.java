import javafx.geometry.Point2D;

// movement related methods
public class Movement {
    public static void travelPath(Movable asset, Point2D[] path,  MovementState state) {
        for (Point2D p : path) {
            travelTo(asset, p, state);
        }
    }

    public static void travelTo(Movable asset, Point2D destination, MovementState state) {
        while (asset.getPos() != destination) {
            if (state != asset.getState()) return;
            // line to pos
            // y - y1 = m(x - x1)
            // m = (y - y1) / (x - x1)
            // y = m(x - x1) + y1
            // x = (y - y1)/m + x1
            Double carX = asset.getPos().getX();
            Double carY = asset.getPos().getY();
            Double destinationX = destination.getX();
            Double destinationY = destination.getY();

            // floating point errors...
            if (Math.abs(carX - destinationX) < 0.001) {
                carX = destinationX;
            }
            if (Math.abs(carY - destinationY) < 0.001) {
                carY = destinationY;
            }
            if (carX == destinationX && carY == destinationY) {
                asset.setPos(destination);
                break;
            }

            // new pos one x closer to destination
            Double m = (carY - destinationY) / (carX - destinationX);
            Double direction = (destinationX > carX) ? 1.0 : -1.0;
            Double xDifference = Math.abs(carX - destinationX);
            Double yDifference = Math.abs(carY - destinationY);
            Double newX;
            Double newY;
            if (xDifference > yDifference) { // move x and solve for y
                newX = carX + direction;
                newY = m * (newX - destinationX) + destinationY;
            } else { // move y and solve for x
                Double yDirection = (destinationY > carY) ? 1.0 : -1.0;
                newY = carY + yDirection;
                newX = (carY - destinationY) / m + destinationX;
            }
            asset.setPos(new Point2D(newX, newY));

            try {
                Thread.sleep(asset.getSpeed());
            } catch (InterruptedException e) {
                if (!asset.isEmergency()) e.printStackTrace();
            }
        }
    }
}