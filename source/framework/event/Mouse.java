package framework.event;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import static org.lwjgl.glfw.GLFW.*;

public class Mouse {
    public static boolean mouseMoved;
    public static boolean mouseScrolled;
    private static boolean[] buttons;
    private static double mouseX;
    private static double mouseY;
    private static double swipeX;
    private static double swipeY;
    private static double oldX;
    private static double oldY;
    private static double newX;
    private static double newY;
    private static double mouseScrollX;
    private static double mouseScrollY;
    private static double oldScrollX;
    private static double oldScrollY;
    private static double newScrollX;
    private static double newScrollY;
    private final GLFWCursorPosCallback mouseMove;
    private final GLFWMouseButtonCallback mouseButtons;
    private final GLFWScrollCallback mouseScroll;

    public Mouse() {
        swipeX = swipeY = 0;
        buttons = new boolean[GLFW_MOUSE_BUTTON_LAST];


        mouseButtons = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                buttons[button] = (action != GLFW_RELEASE);
            }
        };

        mouseMove = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xPos, double yPos) {
                mouseMoved = true;
                mouseX = xPos;
                mouseY = yPos;
                newX = xPos;
                newY = yPos;
                update(xPos, yPos); // for the swiping
            }
        };

        mouseScroll = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double offsetX, double offsetY) {
                mouseScrolled = true;
                mouseScrollX = offsetX;
                mouseScrollY = offsetY;
            }
        };
    }

    public static boolean isScrolling()
    {
        return mouseScrolled;
    }

    private void update(double xPos, double yPos)
    {
        swipeX = newX - oldX;
        swipeY = newY - oldY;
        oldX = newX;
        oldY = newY;
    }

    public GLFWCursorPosCallback getMouseMoveCallback() {
        return mouseMove;
    }

    public GLFWMouseButtonCallback getMouseButtonsCallback() {
        return mouseButtons;
    }

    public GLFWScrollCallback getMouseScrollCallback() {
        return mouseScroll;
    }

    public static boolean isMoving()
    {
        return mouseMoved;
    }

    public void destroy() {
        mouseMove.free();
        mouseButtons.free();
        mouseScroll.free();
    }

    public static boolean isButtonDown(int button) {
        return buttons[button];
    }

    public static double getMouseX() {
        return mouseX;
    }

    public static double getSwipeX()
    {
        return swipeX;
    }

    public static double getSwipeY()
    {
        return swipeY;
    }

    public static double getMouseY() {
        return mouseY;
    }

    public static double getMouseScrollX() {
        return mouseScrollX;
    }

    public static double getMouseScrollY() {
        return mouseScrollY;
    }
}