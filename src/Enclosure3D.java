import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class Enclosure3D {
    private Group mainGroup;


    public void initialize() {
        mainGroup = new Group();

        Box ground = new Box(10000, 80, 10000);
        ground.setTranslateY(852);
        ground.setScaleX(1);
        ground.setScaleY(1);
        ground.setScaleZ(1);

        PhongMaterial groundMaterial = new PhongMaterial();
        groundMaterial.setDiffuseColor(Color.LIGHTGREEN);
        //groundMaterial.setSpecularColor(Color.GREEN);
        ground.setMaterial(groundMaterial);

        //AmbientLight ambientLight = new AmbientLight(Color.WHITE);

        //mainGroup.getChildren().add(ambientLight);
        mainGroup.getChildren().add(ground);
    }

    public Group getMainGroup() { return mainGroup; }
}
