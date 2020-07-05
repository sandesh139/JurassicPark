// ID is used as the key for the simulation state map.
// Each new instance is just an incremented value.
public class ID {
    private static long nextID = 0;
    private final long id;

    synchronized public long getID() {
        return id;
    }

    public ID(){
        id = nextID;
        nextID++;
    }
}
