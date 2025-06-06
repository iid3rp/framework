package framework.environment;

import framework.entity.Camera;
import framework.entity.Entity;
import framework.entity.Light;
import framework.entity.Player;
import framework.event.MouseEvent;
import framework.hardware.Display;
import framework.lang.Vec2;
import framework.particles.ParticleSystem;
import framework.swing.Container;
import framework.swing.ContentPane;
import framework.terrain.Terrain;
import framework.util.LinkList;
import framework.water.WaterTile;

public class Scene
{
    public static Vec2 offset;
    private LinkList<Entity> entities;
    private LinkList<Light> lights;
    private LinkList<WaterTile> waters;
    private Terrain terrain;

    private Camera camera;
    private Player player;
    private ContentPane pane;
    public MouseEvent event;
    private ParticleSystem particleSystem;

    //private Vector4f clipPlane;
    public Scene()
    {
        pane = new ContentPane();
        entities = new LinkList<>();
        lights = new LinkList<>();
        terrain = new Terrain();
        waters = new LinkList<>();
        offset = new Vec2();
    }

    public static Vec2 getOffset()
    {
        return offset;
    }

    public static void setOffset(int x, int y)
    {
        float _x = (float) x / Display.getWidth();
        float _y = (float) y / Display.getHeight();
        offset = new Vec2(_x, _y);
    }

    public LinkList<Entity> getEntities()
    {
        return entities;
    }

    public Player getPlayer()
    {
        return player;
    }

    public Terrain getTerrain()
    {
        return terrain;
    }

    public void add(WaterTile water)
    {
        waters.addAll(water);
    }
    public void add(Entity entity)
    {
        entities.addAll(entity);
    }

    public LinkList<Light> getLights()
    {
        return lights;
    }

    public void setTerrain(Terrain terrain)
    {
        this.terrain = terrain;
    }

    public void add(Light light)
    {
        lights.addAll(light);
    }

    public void add(Container texture)
    {
        pane.add(texture);
    }

    public void setPlayer(Player player)
    {
        this.player = player;
    }

    public ContentPane getContentPane()
    {
        return pane;
    }

    public void setContentPane(ContentPane pane)
    {
        this.pane = pane;
    }

    public MouseEvent getEvent()
    {
        return event;
    }

    public void setEvent(MouseEvent event)
    {
        this.event = event;
    }

    public void setCamera(Camera camera)
    {
        this.camera = camera;
    }

    public Camera getCamera()
    {
        return camera;
    }

    public void setTerrainSize(float x, float z)
    {
        terrain.x = x;
        terrain.z = z;
    }

    public Light getMainLight()
    {
        return lights.getFirst();
    }

    @Override
    public String toString() {
        int numEntities = entities.size();
        int numLights = lights.size();
        int numGUITextures = pane.getComponents().size();
        int numTerrainVertices = terrain.getModel().vertexCount();

        return "Scene Information:\n" +
                "Number of Entities: " + numEntities + "\n" +
                "Number of Lights: " + numLights + "\n" +
                "Number of GUI Textures: " + numGUITextures + "\n" +
                "Number of Terrain Vertices: " + numTerrainVertices;
    }

    public LinkList<WaterTile> getWaters()
    {
        return waters;
    }

    public ParticleSystem getParticleSystem()
    {
        return particleSystem;
    }

    public void setParticleSystem(ParticleSystem particleSystem)
    {
        this.particleSystem = particleSystem;
    }
}
