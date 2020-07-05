// object for the various wristband scanners around the park
public enum WristbandScanners {
    ENTRY_SCANNER (true),
    EXIT_SCANNER (false);

    private boolean atTicketStation;
    private InputEventListener eventListener;

    WristbandScanners(boolean entry){
        atTicketStation = entry;
    }

    public void setListener(InputEventListener listener){
        eventListener = listener;
    }

    synchronized public void scanWristband(Integer uniqueId){
        if (atTicketStation) eventListener.wristbandScannedForEntry(uniqueId);
        else eventListener.wristbandScannedForExit(uniqueId);
    }
}

