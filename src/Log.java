/**
 * Logging Class
 *
 */

import java.util.LinkedList;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class Log implements Comparable<Log>{
    /* Int representing the priority of the message (number from 1-3) */
    private int priority;
    /* Holds all messages */
    private MainDisplayController GUI;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    /* log of all the messages gotten by the main controller */
    private LinkedList<String> logMain;
    /* log of all the messages gotten by the main controller */
    private LinkedList<String> logCar;

    public Log() {
        logMain = new LinkedList<>();
        logCar = new LinkedList<>();
    }

    public void setGUI(MainDisplayController GUI){
        this.GUI = GUI;
    }

    synchronized public void updateMainLog(String message) {
        LocalDateTime now = LocalDateTime.now();
        logMain.add("["+dtf.format(now)+"] " + message+"\n");
        GUI.updateMainLog("["+dtf.format(now)+"] " + message+"\n");
    }

    synchronized public void updateCarLog(String message){
        LocalDateTime now = LocalDateTime.now();
        logCar.add("["+dtf.format(now)+"] " + message+"\n");
        GUI.updateCarLog("["+dtf.format(now)+"] " + message+"\n");
    }

    synchronized public void updatePatronsLog(String logPatrons){

    }

    synchronized public int getPriority() {
        return priority;
    }

    synchronized public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * compareTo override
     *
     * This method allows for the priorityBlockingQueue to pick a priority
     * between two Message objects. Note that priority are managed based on
     * the following scale: 1 - high, 2 - medium, 3 - low
     *
     * @param o representing the Message object.
     * @return integer representing the highest priority.
     */
    @Override
    public int compareTo(Log o) {
        if (this.priority == o.priority){
            return this.priority;
        }
        return this.priority-o.priority;
    }
}
