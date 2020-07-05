import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

// View Controller is completely in charge of everything related to the GUI.
// It is also the main entry point to the program because of how JavaFX is intended to be used.

public class ViewController extends Application {
    // Gui Stuff
    private MainDisplayController mainDisplayController;
    private Canvas simCanvas;
    private Image sgImg;

    // set up the GUI
    private final Point2D patronStartPos = new Point2D(930.0, 255.0);
    private int numPatronsQueued = 0;
    private int numPatronLines = 0;

    // main controller needs to live here since JavaFX really likes being in charge
    private MainController mainController;
    // main controller needs to live here since JavaFX really likes being in charge
    private Log logObject = new Log();

    // buildCar has logic on how to build and display a car
    private void buildCar(Car c) {
        GraphicsContext gc = simCanvas.getGraphicsContext2D();
        gc.setFill(Color.TOMATO);
        // car is a boring rectangle
        gc.fillRoundRect(c.getPos().getX(), c.getPos().getY(), c.getCarSize()[0], c.getCarSize()[1], 4, 4);
        // show number of passengers
        gc.setFill(Color.BLACK);
        gc.setTextBaseline(VPos.TOP);
        int numPassengers = 0;
        for (Patron p : c.getAssignedPassengers()) {
            if (p.getState() == MovementState.IN_CAR) {
                numPassengers++;
            }
        }
        gc.fillText("" + numPassengers, c.getPos().getX(), c.getPos().getY());
        // outline car
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRoundRect(c.getPos().getX(), c.getPos().getY(), c.getCarSize()[0], c.getCarSize()[1], 4, 4);
    }

    // buildPerson has logic on how to build and display a person
    private void buildPatron(Patron p) {
        MovementState pState = p.getState();
        double patronSize = p.getPatronSize();
        // dont draw if left park
        if (pState == MovementState.LEFT_PARK || pState == MovementState.IN_CAR || pState == MovementState.IN_BUNKER) {
            return;
        }
        // figure out position
        if (pState == MovementState.QUEUED) {
            double maxPatrons = (1140 - patronStartPos.getX()) / patronSize;
            p.setPos(patronStartPos.add(new Point2D(patronSize * (numPatronsQueued % maxPatrons), numPatronLines * patronSize)));
            numPatronsQueued++;
            if (numPatronsQueued % maxPatrons == 0) {
                numPatronLines++;
            }
        }
        GraphicsContext gc = simCanvas.getGraphicsContext2D();
        gc.setFill(Color.TAN);
        gc.fillOval(p.getPos().getX(), p.getPos().getY(), patronSize, patronSize);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(p.getPos().getX(), p.getPos().getY(), patronSize, patronSize);
    }

    private void buildPopupWalls(PopupWalls p){
        GraphicsContext gc = simCanvas.getGraphicsContext2D();
        gc.save();
        /*
        color should change when emergency is triggered
        there is a color value in the PopupWalls class that
        can be accessed
        */
        gc.setStroke(p.wallColor());
        gc.setLineWidth(4);
        gc.setLineDashes(15);
        double s = 1.0;
        gc.strokeRoundRect(170,235,256,370,81*s,77*s);
        gc.restore();
    }

    private void buildBunker(SafetyBunker bunker){
            int numOccupants = bunker.getNumOccupants();
            if (numOccupants != 0){
                GraphicsContext gc = simCanvas.getGraphicsContext2D();
                gc.setFill(Color.BLACK);
                gc.fillText("" + numOccupants, bunker.getPos().getX(), bunker.getPos().getY());
        }
    }

    private void buildPark() {
        GraphicsContext gc = simCanvas.getGraphicsContext2D();
        gc.drawImage(sgImg, 0, 0, simCanvas.getWidth(), simCanvas.getHeight());
    }

    // animationLoop build the GUI
    // this follows the Immediate Mode GUI model
    // where the entire GUI is regenerated each frame
    private void loop() {
        ConcurrentHashMap<ID, Asset> simState = mainController.getSimState();

        // clear the canvas
        GraphicsContext gc = simCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, simCanvas.getWidth(), simCanvas.getHeight());
        // reset queue counters
        numPatronsQueued = 0;
        numPatronLines = 0;

        buildPark();


        // loop through all simulations assets
        // check the asset type and cast it appropriately
        // then pass it to the correct asset builder
        for (Asset a : simState.values())  {
            switch (a.getType()) {
            case Car:
                buildCar((Car) a.getAsset());
                break;
            case Patron:
                buildPatron((Patron) a.getAsset());
                break;
            case PopupWall:
                buildPopupWalls((PopupWalls) a.getAsset());
                break;
            case Bunker:
                buildBunker((SafetyBunker) a.getAsset());
                break;
            default:
                throw new RuntimeException("invalid asset type");
            }
        }


    }

    // start is required to implement Application.
    @Override
    public void start(Stage mainStage) {
        mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

        // read background image
        File imgFile = new File("./resources/sg.png");
        sgImg = new Image(imgFile.toURI().toString());

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("MainDisplay.fxml")
        );

        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not read MainDisplay.fxml file");
            return;
        }

        mainDisplayController = loader.getController();

        Scene scene = new Scene(root);
        mainStage.setTitle("SGC Simulation");
        mainStage.setScene(scene);
        mainStage.show();

        // continuously update the GUI using an AnimationTimer
        // which does some smart stuff to only update when needed
        // and thus saving some processor time
        AnimationTimer animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                loop(); // calls our gui loop each tick
            }
        };

        simCanvas = mainDisplayController.OverheadViewCanvas;

        mainController = new MainController();

        // start the animation timer
        animationTimer.start();


        // Start the logging thread
        logObject.setGUI(mainDisplayController);

        // start the mainController on it's own thread
        mainController.setLog(logObject);
        mainDisplayController.setInputController(mainController.getInputController());
        Thread mainControllerThread = new Thread(mainController);
        mainControllerThread.setDaemon(true);
        mainControllerThread.start();
    }

    // main entry point to the simulation
    public static void main(String[] args) {
        launch(args);
    }
}