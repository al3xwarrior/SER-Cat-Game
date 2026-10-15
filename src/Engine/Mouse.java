package Engine;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// This project had 0 support for mouse clicks. Not sure if we would use it for anything but UI
// but I made this for the Level Selector - Alex
public class Mouse {
    private static int x = -1;
    private static int y = -1;
    private static boolean leftDown = false;

    // a click is "consumed" once a screen has handled it, so one press only triggers one action
    private static boolean leftClickPending = false;

    private static final MouseAdapter mouseListener = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            update(e);
            if (e.getButton() == MouseEvent.BUTTON1) {
                leftDown = true;
                leftClickPending = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            update(e);
            if (e.getButton() == MouseEvent.BUTTON1) {
                leftDown = false;
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            update(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            update(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            x = -1;
            y = -1;
        }

        private void update(MouseEvent e) {
            Point p = e.getPoint();
            x = p.x;
            y = p.y;
        }
    };

    // prevents Mouse from being instantiated
    private Mouse() { }

    public static MouseAdapter getMouseListener() {
        return mouseListener;
    }

    public static int getX() {
        return x;
    }

    public static int getY() {
        return y;
    }

    public static boolean isLeftDown() {
        return leftDown;
    }

    // returns true once per left click, then resets
    public static boolean consumeLeftClick() {
        boolean clicked = leftClickPending;
        leftClickPending = false;
        return clicked;
    }

    // discards any click that happened before a screen started listening (e.g. the click that opened it)
    public static void clearLeftClick() {
        leftClickPending = false;
    }
}
