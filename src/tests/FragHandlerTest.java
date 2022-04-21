package tests;

import model.objects.Fragment;
import model.objects.Header;
import model.FragHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests methods from the FragHandler.
 */
public class FragHandlerTest {
    private FragHandler fragHandler;
    private Fragment fragment;
    private Header header;
    private String message;

    private final String delimiter = "";

    @BeforeEach
    void setUp() {
        message = "Hey!";
        header = new Header();
        header.setFrag(true);
        header.setDataLen(message.length());
    }

    @Test
    void testAddFragment() {
        header.setFragNum(0);
        fragment = new Fragment(header, message);

        fragHandler = new FragHandler();
        fragHandler.addFragment(fragment);
        assertTrue(fragHandler.hasFragment(0));
    }

    @Test
    void testIsComplete() {
        header.setFragNum(0);
        fragment = new Fragment(header, message + delimiter);

        fragHandler = new FragHandler();
        fragHandler.addFragment(fragment);

        assertTrue(fragHandler.isComplete());

        header = new Header();
        header.setFrag(true);
        header.setFragNum(1);
        header.setDataLen(message.length());
        fragment = new Fragment(header, message + delimiter);

        fragHandler.addFragment(fragment);

        assertTrue(fragHandler.isComplete());
    }

}
