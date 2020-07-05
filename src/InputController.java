// Controller interface that handles incoming events

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InputController {

    private InputEventListener eventListener;

    public InputController (InputEventListener listener){
        eventListener = listener;
    }

    public void setEmergencyMode() {
        eventListener.setEmergencyMode(true);
    }

    public void setNormalMode() {
        eventListener.setEmergencyMode(false);
    }

    public void setupWristbandScanners(){
        for (WristbandScanners s : WristbandScanners.values()){
            s.setListener(eventListener);
        }
    }
}
