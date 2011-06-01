package nl.mpi.arbil.util;

/**
 * Buffer mainly for reloading/sorting actions that get performed on request,
 * where multiple request may occur in a short time frame and repeated execution
 * is not desired. Such requests can be 'time-buffered' so that a set amount of
 * request idleness is required before the action is executed.
 *
 * The actual thread is not created until a request is put
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ArbilActionBuffer {

    /**
     *
     * @param title Title of the thread that will be created
     * @param delay Amount of time that is waited after a request before action is executed
     * @param maxDelay Maximum amount of time that will be waited before action is executed
     */
    public ArbilActionBuffer(String title, int delay, int maxDelay) {
        this(title, delay, maxDelay, new Object());
    }

    /**
     *
     * @param title Title of the thread that will be created
     * @param delay Amount of time that is waited after a request before action is executed
     */
    public ArbilActionBuffer(String title, int delay) {
        this(title, delay, 0, new Object());
    }

    /**
     *
     * @param title Title of the thread that will be created
     * @param delay Amount of time that is waited after a request before action is executed
     * @param lock External lock to use
     */
    public ArbilActionBuffer(String title, int delay, int maxDelay, final Object lock) {
        this.actionLock = lock;
        this.delay = delay;
        this.title = title;
        this.maxDelay = maxDelay;
    }

    public void requestAction() {
        synchronized (actionLock) {
            actionRequested = true;

            if (workerThread == null || !workerThread.isAlive()) {
                workerThread = new Thread(new ArbilBufferedWorkerRunnable(), title);
                workerThread.start();
            }
        }
    }

    public void requestActionAndNotify() {
        synchronized (actionLock) {
            requestAction();
            actionLock.notifyAll();
        }
    }

    protected abstract void executeAction();
    private final Object actionLock;
    private String title;
    private int delay;
    private int maxDelay;
    private boolean actionRequested;
    private Thread workerThread;

    private class ArbilBufferedWorkerRunnable implements Runnable {

        @Override
        public void run() {
            // There may be new requests. If so, keep in the loop
            while (actionRequested) {
                try {
                    // Go into wait for some short time while more actions are requested
                    waitForIncomingRequests();
                    // No requests have been added for some time, so execute the action
                    executeAction();
                } catch (InterruptedException ex) {
                    return;
                }
            }
        }

        private void waitForIncomingRequests() throws InterruptedException {
            long waitStartTime = System.currentTimeMillis();
            synchronized (actionLock) {
                if (!actionRequested) {
                    // No action has been requested. Wait for one to be requested.
                    actionLock.wait();
                }

                while (actionRequested) {
                    // Action requested. Invalidate request.
                    actionRequested = false;

                    if (maxDelay > 0 && (System.currentTimeMillis() - waitStartTime) > maxDelay) {
                        // Total waiting time exceeds maximum. Return in any case.
                        return;
                    } else {
                        // Give some time for another reload to be requested
                        actionLock.wait(delay);
                    }
                }
            }
        }
    }
}