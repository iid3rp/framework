package framework.display;


import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.*;

public class DisplayManager
{
    public static int width = 720;
    public static int height = 720;
    private static int frames = 120;
    private static long lastTime;
    private static float delta;
    public static void createDisplay()
    {
        ContextAttribs attributes = new ContextAttribs(3, 3);
        attributes = attributes.withForwardCompatible(true);
        attributes = attributes.withProfileCore(true);

        try {
            Display.setDisplayMode(new DisplayMode(width, height));
            Display.create(new PixelFormat().withDepthBits(24).withSamples(4), attributes);
           
            System.out.println(GL11.glGetInteger(GL11.GL_DEPTH_BITS));
        }
        catch(LWJGLException e) {
            throw new RuntimeException(e);
        }

        GL11.glViewport(0, 0, width, height);
        lastTime = getCurrentTime();
    }

    public static void updateDisplay()
    {
        Display.sync(frames);
        Display.update();
        long currentFrameTime = getCurrentTime();
        delta = (currentFrameTime - lastTime) / 1000f;
        lastTime = currentFrameTime;
    }

    public static void closeDisplay()
    {
        Display.destroy();
    }

    public static void setTitle(String title)
    {
        Display.setTitle(title);
    }

    public static void setSize(int width, int height)
    {
        DisplayManager.width = width;
        DisplayManager.height = height;
    }

    public static float getFrameTimeSeconds()
    {
        return delta;
    }

    public static long getCurrentTime()
    {
        return Sys.getTime() * 1000 / Sys.getTimerResolution();
    }


    public static boolean set()
    {
        return width != 0 || height != 0;
    }
}
