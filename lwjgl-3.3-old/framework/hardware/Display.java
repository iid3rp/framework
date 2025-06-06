package framework.hardware;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL11.*;

public final class Display implements Hardware
{
    private static int WINDOW_WIDTH = 1280;
    private static int WINDOW_HEIGHT = 720;
    private static final int REFRESH_RATE = 120;
    private static int width = WINDOW_WIDTH;
    private static int height = WINDOW_HEIGHT;
    private static long window;
    private static final String TITLE = "";
    private static int frames;
    private static long time;
    private static boolean showFPSTitle;
    private static double lastFrameTime;
    private static double deltaInSeconds;
    private static Keyboard keyboard;
    private static Mouse mouse;
    private static int currentFPSCount;

    // Hide the constructor
    private Display() {}

    public static void createDisplay(int width, int height) {
        if (!glfwInit()) {
            throw new RuntimeException("ERROR: GLFW wasn't initialized");
        }

        Display.width = width == 0? WINDOW_WIDTH : width;
        Display.height = height == 0? WINDOW_HEIGHT : height;

        glfwWindowHint(GLFW_DEPTH_BITS, 24); // Set the depth bits to 24
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_DECORATED, GL_FALSE);
        glfwWindowHint(GLFW_REFRESH_RATE, 120);
        glfwWindowHint(GLFW_DECORATED, GL_TRUE);

        window = glfwCreateWindow(width, height, TITLE, 0, 0);

        if (window == 0) {
            throw new RuntimeException("Failed to create window");
        }

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        assert vidMode != null;
        glfwSetWindowPos(window, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2);

        keyboard = new Keyboard();
        mouse = new Mouse();

        // register keyboard input callback
        glfwSetKeyCallback(window, keyboard);


        glfwSetCursorPosCallback(window, mouse.getMouseMoveCallback());
        glfwSetMouseButtonCallback(window, mouse.getMouseButtonsCallback());
        glfwSetScrollCallback(window, mouse.getMouseScrollCallback());

        glfwMakeContextCurrent(window);
        createCapabilities();
        glfwShowWindow(window);

        // Setting the value to 1 should limit to 60 FPS
        glfwSwapInterval(0);

        lastFrameTime = getCurrentTime();
        GL11.glEnable(GL13.GL_MULTISAMPLE);
    }

    public static void updateDisplay()
    {
        Mouse.mouseMoved = false;
        Mouse.mouseScrolled = false;
        Mouse.mouseDragged = false;
        glfwPollEvents();
        glfwSwapBuffers(window);

        if (showFPSTitle) {
            frames++;

            if (System.currentTimeMillis() > time + 1000) {
                glfwSetWindowTitle(window, TITLE + " | FPS: " + frames);
                time = System.currentTimeMillis();
                currentFPSCount = frames;
                frames = 0;
            }
        }

        double currentFrameTime = getCurrentTime();
        deltaInSeconds = (currentFrameTime - lastFrameTime) / 1000;
        lastFrameTime = currentFrameTime;
    }

    public static void closeDisplay() {
        mouse.destroy();
        keyboard.close();
        glfwWindowShouldClose(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public static boolean shouldDisplayClose() {
        return !glfwWindowShouldClose(window);
    }

    public static String getOpenGlVersionMessage() {
        return glGetString(GL_VERSION);
    }

    public static String getLwjglVersionMessage() { return org.lwjgl.Version.getVersion(); }

    public static void setShowFPSTitle(boolean showFPSTitle)
    {
        Display.showFPSTitle = showFPSTitle;

        if (!showFPSTitle) {
            frames = 0;
            time = 0;
        }
    }

    public static int getVolatileFPS()
    {
        return frames;
    }

    public static int getCurrentFPSCount()
    {
        return currentFPSCount;
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public static float getDeltaInSeconds() {
        return (float) deltaInSeconds;
    }

    private static double getCurrentTime() {
        return glfwGetTime() * 1000;
    }
}
