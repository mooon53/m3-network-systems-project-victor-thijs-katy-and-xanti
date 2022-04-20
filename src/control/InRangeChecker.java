package control;

import java.util.Timer;

/**
 * InRangeChecker object which checks if a node is still in range.
 */
public class InRangeChecker implements Runnable {
    private int id;
    private boolean inRange;
    private Thread thread;

    public InRangeChecker(int id, boolean inRange) {
        this.id = id;
        this.inRange = inRange;
        thread = new Thread(this);
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                // TODO: ask a TA what to put here
            }
            inRange = false;
        }
    }

    public void resetTimer() {
        thread.interrupt();
        thread = new Thread(this);
        inRange = true;
    }

    public void startThread() {
        thread.start();
    }

    public boolean getInRange() {
        return inRange;
    }
}
