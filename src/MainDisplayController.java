import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;



public class MainDisplayController {

    public GridPane OverheadViewGridPane;
    public GridPane MainControlRoomGridPane;
    public VBox MainControlRoomSetOperationMode;
    public VBox OverheadViewSetOperationMode;
    public TextArea TopLoggingArea;
    public TextArea BottomLoggingArea;

    public Canvas OverheadViewCanvas;
    public SubScene DinosaurViewSubScene;

    private InputController ic;

    public void initialize() {
        Enclosure3D enclosure = new Enclosure3D();
        Trex3D tRex = new Trex3D();

        enclosure.initialize();
        tRex.initialize();
        tRex.startRandomWalk();
        enclosure.getMainGroup().getChildren().add(tRex.gettRexGroup());
        DinosaurViewSubScene.setFill(Color.GREY);
        DinosaurViewSubScene.getCamera().setTranslateX(-5000);
        DinosaurViewSubScene.getCamera().setTranslateY(-200);
        DinosaurViewSubScene.getCamera().setTranslateZ(-5000);
        DinosaurViewSubScene.getCamera().getTransforms().addAll(new Rotate(-10, Rotate.X_AXIS));
        DinosaurViewSubScene.setRoot(enclosure.getMainGroup());


        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(30), DinosaurViewSubScene.getCamera());
        rotateTransition.setAutoReverse(true);
        rotateTransition.setAxis(Rotate.Y_AXIS);
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(60);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.play();
    }

    public void setInputController(InputController ic) {
        this.ic = ic;
    }

    public void setNormalMode(ActionEvent event) {
        ic.setNormalMode();
    }

    public void setEmeregencyMode(ActionEvent event) {
        ic.setEmergencyMode();
    }

    synchronized public void updateMainLog(String newMessage) {
        Platform.runLater(() -> TopLoggingArea.appendText(newMessage));
    }

    synchronized public void updateCarLog(String newMessage) {
        Platform.runLater(() -> BottomLoggingArea.appendText(newMessage));
    }
}
