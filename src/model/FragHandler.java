package model;

import control.Fragment;
import control.Message;
import view.UI;

import java.util.HashMap;

import static java.util.Collections.max;

/**
 * Fragmentation handler object which composes a fragmented message.
 */
@SuppressWarnings("InvisibleCharacter")
public class FragHandler implements Runnable {
    private int sourceID;
    private int seqNum;
    private HashMap<Integer, String> fragments = new HashMap<>();
    // DON'T REMOVE "", it is a special character we actually use
    private final String delimiter = "";

    /**
     * Constructor for the fragmentation handler putting the fragment into a storing HashMap.
     *
     * @param sourceID node ID of the source of the fragment
     * @param seqNum   sequence number of the fragment
     * @param fragID   fragmentation number of the fragment
     * @param fragment text message contained in the fragment
     */
//    public FragHandler(int sourceID, int seqNum, int fragID, String fragment) {
//        this.sourceID = sourceID;
//        this.seqNum = seqNum;
//        fragments.put(fragID, fragment);
//        System.out.println(this);
//        System.out.println("frag handler created for sequence " + seqNum + " and fragment number " + fragID);
//        System.out.println("fragments length " + fragments.size());
//    }

    /**
     * Constructor for the fragmentation handler only needing a Fragment object as input.
     *
     * @param fragment fragment for which the fragmentation handler is made
     */
    public FragHandler(Fragment fragment) {
        sourceID = fragment.getHeader().getSource();
        seqNum = fragment.getHeader().getSeqNum();
        fragments.put(fragment.getHeader().getFragNum(), fragment.getMessagePart());
    }

    /**
     * Empty constructor.
     */
    public FragHandler() {

    }

    /**
     * Runs to look if a message got completed.
     */
    public void run() {
        // check every 0.1 second if a full message is complete
        while (!isComplete()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO: ask a TA what to put here
            }
        }

        // if the message is complete, print it to the UI
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < fragments.size(); i++) {
            String fragment = fragments.get(i);
            message.append(fragment);
        }
        Message fullMessage = new Message(sourceID, message.toString());
        fullMessage.removeCharacter(delimiter);
        UI.printMessage(fullMessage);
    }

    /**
     * Adds a fragment to the fragmentation handler.
     *
     * @param fragID   fragmentation number of the fragment
     * @param fragment message text contained in the fragment
     */
    public void addFragment(int fragID, String fragment) {
        fragments.put(fragID, fragment);
    }

    /**
     * Adds a fragment to the fragmentation handler only needing a Fragment object as input.
     *
     * @param fragment Fragment object to put into the fragmentation handler
     */
    public void addFragment(Fragment fragment) {
        addFragment(fragment.getHeader().getFragNum(), fragment.getMessagePart());
        System.out.println(this);
        System.out.println("fragment with sequence " + fragment.getHeader().getSeqNum() + " and frag " + fragment.getHeader().getFragNum());
        System.out.println("fragments length " + fragments.size());
        System.out.println("data length set to " + fragment.getHeader().getDataLen());
    }

    /**
     * Checks if the fragmentation handler already contains the given fragmentation number.
     *
     * @param fragID fragmentation number to check
     * @return true if the fragmentation handler already contains the given fragmentation number
     */
    public boolean hasFragment(int fragID) {
        return fragments.containsKey(fragID);
    }

    /**
     * Checks if a message has all its fragments.
     *
     * @return true if the message is complete
     */
    @SuppressWarnings("InvisibleCharacter")
    public boolean isComplete() {
        // for a message to be complete means that the final segment has been received,
        // and that the length of the fragments list equals the ID of the last fragment
        if (fragments.isEmpty()) return false;
        System.out.println("does not always return false");
        int highestID = max(fragments.keySet());
        String lastFragment = fragments.get(highestID);
        System.out.println(lastFragment);
        System.out.println(lastFragment.endsWith(delimiter));
        System.out.println(fragments.size());
        return lastFragment.endsWith(delimiter) && fragments.size() == highestID + 1;
    }
}
