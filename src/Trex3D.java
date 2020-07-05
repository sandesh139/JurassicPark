import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Random;

public class Trex3D {
    private Group tRexGroup;
    private Random random;

    public void initialize() {
        random = new Random();

        FXMLLoader tRexLoader = new FXMLLoader(getClass().getResource("Trex.fxml"));
        try {
            tRexGroup = tRexLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        tRexGroup.setTranslateX(0);
        tRexGroup.setTranslateY(650);
        tRexGroup.setTranslateZ(0);
        tRexGroup.setScaleX(40);
        tRexGroup.setScaleY(40);
        tRexGroup.setScaleZ(40);
        MeshView tRexSkin = (MeshView)tRexGroup.getChildren().get(0);
        PhongMaterial skinMaterial = new PhongMaterial();
        skinMaterial.setSpecularPower(18.0);
        skinMaterial.setBumpMap(new Image(getClass().getResourceAsStream("TrexBump012714.jpg")));
        skinMaterial.setDiffuseMap(new Image(getClass().getResourceAsStream("TrexColor01152015.jpg")));
        skinMaterial.setSpecularMap(new Image(getClass().getResourceAsStream("TrexSpec.png")));
        tRexSkin.setMaterial(skinMaterial);

        MeshView tRexEye = (MeshView)tRexGroup.getChildren().get(1);
        PhongMaterial eyeMaterial = new PhongMaterial();
        eyeMaterial.setSpecularPower(32.0);
        eyeMaterial.setDiffuseMap(new Image(getClass().getResourceAsStream("TrexEyeColor.jpg")));
        tRexEye.setMaterial(eyeMaterial);

        MeshView tRexTeeth = (MeshView)tRexGroup.getChildren().get(4);
        PhongMaterial toothMaterial = new PhongMaterial();
        toothMaterial.setSpecularPower(32.0);
        toothMaterial.setBumpMap(new Image(getClass().getResourceAsStream("TrexTooth.png")));
        toothMaterial.setDiffuseMap(new Image(getClass().getResourceAsStream("TrexTooth.png")));
        tRexTeeth.setMaterial(toothMaterial);

        MeshView tRexTeeth2 = (MeshView)tRexGroup.getChildren().get(5);
        toothMaterial.setSpecularPower(32.0);
        toothMaterial.setBumpMap(new Image(getClass().getResourceAsStream("TrexTooth.png")));
        toothMaterial.setDiffuseMap(new Image(getClass().getResourceAsStream("TrexTooth.png")));
        tRexTeeth2.setMaterial(toothMaterial);
    }

    public void startRandomWalk() {
        TranslateTransition translateTransition = new TranslateTransition();
        translateTransition.setNode(tRexGroup);
        translateTransition.setToX(getRandomValue());
        translateTransition.setToZ(getRandomValue());
        translateTransition.setDuration(Duration.seconds(15));
        translateTransition.setInterpolator(Interpolator.LINEAR);
        translateTransition.setCycleCount(1);
        translateTransition.setOnFinished(e -> {
            translateTransition.setToX(getRandomValue());
            translateTransition.setToZ(getRandomValue());
            translateTransition.play();
        });

        translateTransition.play();
    }

    private float getRandomValue() {
        return -5000 + random.nextFloat() * (5000 + 5000);
    }

    public Group gettRexGroup() {
        return tRexGroup;
    }
}
