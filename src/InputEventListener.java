// interface for handling incoming events
public interface InputEventListener {

    void wristbandScannedForEntry(Integer uniqueId);

    void wristbandScannedForExit(Integer uniqueId);

    void setEmergencyMode(boolean enable);
}
