package framework.event;

import framework.hardware.Display;
import framework.entity.Camera;
import framework.entity.Entity;
import framework.environment.Environment;
import framework.hardware.Mouse;
import framework.lang.Mat4;
import framework.lang.Vec2;
import framework.lang.Vec3;
import framework.lang.Vec4;
import framework.post_processing.FrameBufferObject;
import framework.terrain.Terrain;
import framework.lang.Math;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.awt.Color;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MouseEvent
{
    public Vec3 currentRay;
    private static int recursionCount = 200;
    private static float rayRange = 6000f;
    private Mat4 projection;
    private Mat4 view;
    private Camera camera;
    private Terrain terrain;
    private Vec3 currentTerrainPoint;
    private static HashMap<Color, Entity> entityMouseEvents = new HashMap<>();
    private Entity currentEntity;
    private Color currentColor;
    private int currentMouseX;
    private int currentMouseY;
    private float clickInterval = 0;
    private boolean mousePressed;
    private boolean mouseReleased;

    public MouseEvent() {}

    public MouseEvent(Camera camera, Mat4 projection)
    {
        this.camera = camera;
        this.projection = projection;
        this.terrain = Environment.getScene().getTerrain();
        this.view = Math.createViewMatrix(camera);
    }

    public Vec3 getCurrentTerrainPoint() {
        return currentTerrainPoint;
    }

    public String getCurrentTerrainPointString()
    {
        if(currentTerrainPoint == null)
            return null;
        else return "[" + currentTerrainPoint.x + ", " +
                currentTerrainPoint.y + ", " +
                currentTerrainPoint.z + "]";
    }

    public Vec3 getCurrentRay()
    {
        return currentRay;
    }

    public void update()
    {
        view = Math.createViewMatrix(camera);
        currentRay = calculateMouseRay();
        if(intersectionInRange(0, rayRange, currentRay))
        {
            currentTerrainPoint = binarySearch(0, 0, rayRange, currentRay);
        }
        else
        {
            currentTerrainPoint = null;
        }
    }

    public Mat4 getProjection()
    {
        return projection;
    }

    public void setProjection(Mat4 projection)
    {
        this.projection = projection;
    }

    public Mat4 getView()
    {
        return view;
    }

    public void setView(Mat4 view)
    {
        this.view = view;
    }

    public Camera getCamera()
    {
        return camera;
    }

    public void setCamera(Camera camera)
    {
        this.camera = camera;
        this.view = Math.createViewMatrix(camera);
    }

    private Vec3 calculateMouseRay()
    {
        float mouseX = (float) Mouse.getMouseX();
        float mouseY = Display.getHeight() - (float) Mouse.getMouseY();
        Vec2 normalizedCoordinates = getNormalizedDeviceCoordinates(mouseX,  mouseY);
        Vec4 clipCoords = new Vec4(normalizedCoordinates.x, normalizedCoordinates.y, -1f, 1f);
        Vec4 eyeCoordinates = toEyeCoordinates(clipCoords);
        Vec3 worldRay = toWorldCoordinates(eyeCoordinates);
        //System.out.println(worldRay.x + " " + worldRay.y + " " + worldRay.z);
        return  worldRay;
    }

    private Vec3 toWorldCoordinates(Vec4 eyeCoordinates)
    {
        Mat4 invertedView = new Mat4();
        Mat4.invert(view, invertedView);
        Vec4 rayWorld = new Vec4();
        Mat4.transform(invertedView, eyeCoordinates, rayWorld);
        Vec3 mouseRay = new Vec3(rayWorld.x, rayWorld.y, rayWorld.z);
        mouseRay.normalize();
        return mouseRay;
    }

    private Vec4 toEyeCoordinates(Vec4 clipCoordinates)
    {
        Mat4 invertedProjection = new Mat4();
        Mat4.invert(projection, invertedProjection);
        Vec4 eyeCoordinates = new Vec4();
        Mat4.transform(invertedProjection, clipCoordinates, eyeCoordinates);
        return new Vec4(eyeCoordinates.x, eyeCoordinates.y, -1f, 0f);
    }

    private Vec2 getNormalizedDeviceCoordinates(float mouseX, float mouseY)
    {
        float x = (2f * mouseX) / Display.getWidth() - 1;
        float y = (2f * mouseY) / Display.getHeight() - 1;
        return new Vec2(x, y);
    }

    private Vec3 getPointOnRay(Vec3 ray, float distance) {
        Vec3 camPos = camera.getPosition();
        Vec3 start = new Vec3(camPos.x, camPos.y, camPos.z);
        Vec3 scaledRay = new Vec3(ray.x * distance, ray.y * distance, ray.z * distance);
        return Vec3.add(scaledRay, start);
    }

    private Vec3 binarySearch(int count, float start, float finish, Vec3 ray) {
        float half = start + ((finish - start) / 2f);
        if (count >= recursionCount) {
            Vec3 endPoint = getPointOnRay(ray, half);
            Terrain terrain = getTerrain(endPoint.x, endPoint.z);
            if (terrain != null) {
                return endPoint;
            } else {
                return null;
            }
        }
        if (intersectionInRange(start, half, ray)) {
            return binarySearch(count + 1, start, half, ray);
        } else {
            return binarySearch(count + 1, half, finish, ray);
        }
    }

    private boolean intersectionInRange(float start, float finish, Vec3 ray) {
        Vec3 startPoint = getPointOnRay(ray, start);
        Vec3 endPoint = getPointOnRay(ray, finish);
        return !isUnderGround(startPoint) && isUnderGround(endPoint);
    }

    private boolean isUnderGround(Vec3 testPoint) {
        Terrain terrain = getTerrain(testPoint.x, testPoint.z);
        float height = 0;
        if (terrain != null) {
            height = terrain.getHeightOfTerrain(testPoint.x, testPoint.z);
        }
        return testPoint.y < height;
    }

    private Terrain getTerrain(float worldX, float worldZ) {
        return terrain;
    }

    public void verifyMousePick()
    {
        int mouseX = Mouse.getMouseX();
        int mouseY = Display.getHeight() - Mouse.getMouseY();

        IntBuffer pixelBuffer = BufferUtils.createIntBuffer(1);
        GL11.glReadPixels(0, 0, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixelBuffer);

        int error = GL11.glGetError();
        if(error != GL11.GL_NO_ERROR) {
            System.err.println("OpenGL Error after glReadPixels: " + error);
            return;
        }

        int[] pixels = new int[1];
        pixelBuffer.get(pixels);

        int pixelInfo = pixels[0];
        int r = pixelInfo & 0xFF;
        int g = (pixelInfo >> 8) & 0xFF;
        int b = (pixelInfo >> 16) & 0xFF;
        int a = (pixelInfo >> 24) & 0xFF;

        // Create a color object from the RGB components (alpha is optional)
        Color color = new Color(r, g, b, 255);

        // Simulate the event based on the picked color
        // don't mind this method:
        simulateEvent(color);
    }


    private void simulateEvent(Color color)
    {
        Entity eventEntity = entityMouseEvents.get(color);

        if(eventEntity == null)
        {
            FocusEvent.setFocus(null);

            if(currentEntity == null)
                return;

            List<MouseListener> listeners = currentEntity.getMouseListeners();
            for(MouseListener listener : listeners)
                listener.mouseExited(this);

            currentEntity = null;
            currentColor = null;
            return;
        }

        if(currentEntity != null && currentEntity != eventEntity)
        {
            List<MouseListener> listeners = currentEntity.getMouseListeners();
            for(MouseListener l : listeners)
                l.mouseExited(this);
            FocusEvent.setFocus(null);
            currentEntity = eventEntity;
            currentColor = color;
            return;
        }

        List<MouseListener> listeners = eventEntity.getMouseListeners();
        currentColor = color;

        if(FocusEvent.isNotFocused())
            for(MouseListener l : listeners)
                l.mouseEntered(this);

        FocusEvent.setFocus(eventEntity);

        if(Mouse.isAnyButtonPressed() && !mousePressed) {
            mousePressed = true;
            mouseReleased = false;
            for(MouseListener l : listeners)
                l.mousePressed(this);
        }

        if(Mouse.isDragged()) {
            for(MouseListener l : listeners)
                l.mouseDragged(this);
        }
        else if(Mouse.isMoving()) {
            for(MouseListener l : listeners)
                l.mouseMoved(this);
        }


        if(Mouse.isAllButtonReleased() && !mouseReleased)
        {
            mousePressed = false;
            mouseReleased = true;
            for(MouseListener l : listeners)
                l.mouseReleased(this);
        }

        currentEntity = eventEntity;
        currentColor = null;
    }

    public static void addMouseListener(Entity entity)
    {
        Color color = null;
        while(!entityMouseEvents.containsKey(color))
        {
            Random r = new Random();
            int red = r.nextInt(255);
            int green = r.nextInt(255);
            int blue = r.nextInt(255);
            color = new Color(red, green, blue);
            entityMouseEvents.put(color, entity);
        }
        entity.setMouseColor(color);
    }

    public Color hashColor()
    {
     return currentColor;
    }

    public void resolveColorPickFromPixel(FrameBufferObject eventFbo, FrameBufferObject pxFbo)
    {
        int mouseX = Mouse.getMouseX();
        int mouseY = Display.getHeight() - Mouse.getMouseY();

        // Bind the frame buffers
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, pxFbo.getFrameBuffer());
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, eventFbo.getFrameBuffer());

        GL11.glDrawBuffer(GL11.GL_BACK);

        GL30.glBlitFramebuffer(
                mouseX, mouseY, mouseX + 1, mouseY + 1,
                0, 0, 1, 1,
                GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST
        );

        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, pxFbo.getFrameBuffer());

        IntBuffer pixelBuffer = BufferUtils.createIntBuffer(1);
        GL30.glReadPixels(0, 0, 1, 1, GL30.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixelBuffer);

        // clean within ourselves...
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);

        int pixel = pixelBuffer.get(0);

        // Extract color components
        int a = (pixel >> 24) & 0xFF;
        int b = (pixel >> 16) & 0xFF;
        int g = (pixel >> 8) & 0xFF;
        int r = pixel & 0xFF;

        Color color = new Color(r, g, b, a);
        // System.out.println(color); debuggers...
        simulateEvent(color);

        pixelBuffer.clear();

        // Unbind the frame buffers
        eventFbo.unbindFrameBuffer();
    }

    public boolean isMouseDown(int button)
    {
        return Mouse.isButtonDown(button);
    }

    public boolean isScrolling()
    {
        return Mouse.isScrolling();
    }

    public boolean isMoving()
    {
        return Mouse.isMoving();
    }

    public int getMouseX()
    {
        return Mouse.getMouseX();
    }

    public int getMouseY()
    {
        return Mouse.getMouseY();
    }

    public double getScrollX()
    {
        return Mouse.getMouseScrollX();
    }

    public double getScrollY()
    {
        return Mouse.getMouseScrollY();
    }

    public String getRayCoordinates()
    {
        return "x: " + currentRay.x + "\n" + "y: " + currentRay.y + "\n" + "z: " + currentRay.z;
    }

    public String getTerrainPointCoordinates()
    {
        return "x: " + currentTerrainPoint.x + "\n" + "y: " + currentTerrainPoint.y + "\n" + "z: " + currentTerrainPoint.z;
    }
}
