import javafx.scene.paint.Color;

public class PopupWalls {
    private ID id = new ID();
    private boolean isEnabled;
    private Integer myUniqueField;
    private Color color;

    public PopupWalls(){
        this.isEnabled = false;
        this.color = Color.SEASHELL;
    }

    public ID getId(){
        return this.id;
    }

    public Color wallColor(){
        return this.color;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void activate(){
        this.color = Color.FORESTGREEN;
        this.isEnabled = true;
    }

    public void deactivate(){
        this.isEnabled = false;
        this.color = Color.SEASHELL;
    }

    public Integer getMyUniqueField() {
        return myUniqueField;
    }

    public void setMyUniqueField(Integer i){
        this.myUniqueField = i;
    }
}