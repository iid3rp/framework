package framework;

import framework.event.MouseAdapter;
import framework.event.MouseEvent;
import framework.loader.ModelLoader;
import framework.entity.Light;
import framework.entity.Player;
import framework.environment.Environment;
import framework.environment.Scene;
import framework.loader.ObjLoader;
import framework.loader.TextureLoader;
import framework.model.TexturedModel;
import framework.scripting.FrameworkScript;
import framework.terrain.Terrain;
import framework.textures.TerrainTexture;
import framework.textures.TerrainTexturePack;
import framework.textures.Texture;
import framework.entity.Entity;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Random;

import static framework.toolkit.Display.createDisplay;
import static framework.toolkit.Display.getLwjglVersionMessage;
import static framework.toolkit.Display.getOpenGlVersionMessage;
import static framework.toolkit.Display.setShowFPSTitle;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;

public final class Main
{
    static Random random = new Random();

    private static void start() {
        createDisplay();

        // Show FPS title only if debugging
        if (getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp")) {
            setShowFPSTitle(true);
        }

        // uhm how about yes?
        setShowFPSTitle(true);

        System.out.println("OpenGL: " + getOpenGlVersionMessage());
        System.out.println("LWJGL: " + getLwjglVersionMessage());
    }

    public static void main(String[] args) {
        start();
        Scene scene = new Scene();
        Environment.setScene(scene);


        Light lighting = new Light(new Vector3f(100_000, 100_000, -100_000), new Vector3f(1f, 1f, .5f));

        scene.getLights().add(lighting);
        scene.getLights().add(new Light(new Vector3f(0, 10, 0), new Vector3f(1, 0, 1), new Vector3f(1, 0f, 200f)));
        scene.getLights().add(new Light(new Vector3f(20, 0, 20), new Vector3f(1, 0, 1), new Vector3f(1, 0.01f, 0.02f)));
        scene.getLights().add(new Light(new Vector3f(40, 0, 40), new Vector3f(1, 0, 1), new Vector3f(1, 0.1f, 0.002f)));
        scene.getLights().add(new Light(new Vector3f(-20, 0, 60), new Vector3f(1, 0, 0), new Vector3f(1, 0.01f, 0.02f)));
        scene.getLights().add(new Light(new Vector3f(0, 0, -80), new Vector3f(1, 0, 1), new Vector3f(1, 0.1f, 0.002f)));

        int grass = ModelLoader.loadTexture("grass.png");
        TerrainTexturePack bg = new TerrainTexturePack(
                new TerrainTexture(grass),
                new TerrainTexture(grass),
                new TerrainTexture(grass),
                new TerrainTexture(grass)
        );
        TerrainTexture blend = new TerrainTexture(ModelLoader.loadTexture("blendMap.png"));
        Terrain terrain = new Terrain(0, 0, bg, blend);

        scene.setTerrain(terrain);

        TexturedModel barrel = new TexturedModel(ObjLoader.loadObject("barrel.obj"),
                TextureLoader.generateTexture("barrel.png"));
        barrel.getTexture().setNormalMap(ModelLoader.loadTexture("barrelNormal.png"));
        barrel.getTexture().setReflectivity(.002f);
        barrel.getTexture().setShineDampening(1);
        Entity barrelEntity = new Entity(barrel, new Vector3f(0, 200, 0), 0, 0, 0, 1);
        Entity reference = new Entity(barrel, new Vector3f(212, 0, 607), 0, 0, 0, 1);
        scene.getEntities().add(barrelEntity);
        Random random = new Random();
        int progress = 0;


        Player player = new Player(barrelEntity);
        //player.setLight();
        //player.setLightColor(255, 255, 255);
        //player.setLightAttenuation(new Vector3f(.1f, .01f, .01f));
        scene.setPlayer(player);
        scene.getEntities().add(player);
        //scene.getLights().add(player.getLight());
        scene.setCamera(player.getCamera());


        Entity entity = new Entity(new TexturedModel(
                ObjLoader.loadObject("crate.obj"),
                new Texture(ModelLoader.loadTexture("brat.png"))),
                new Vector3f(0, 0, 0), 0f, 0f, 0f, 1f);
        scene.add(entity);

        TexturedModel chrysalis = new TexturedModel(ObjLoader.loadObject("tree.obj"), new Texture(ModelLoader.loadTexture("grass.png")));
        chrysalis.getTexture().setShineDampening(1f);
        chrysalis.getTexture().setReflectivity(.1f);

        for(int i = 0 ; i < 100; i++) {
            float x = random.nextFloat(terrain.getSize()) - (terrain.getSize() / 2);
            float z = random.nextFloat(terrain.getSize()) - (terrain.getSize() / 2);
            float y = terrain.getHeightOfTerrain(x, z);
            Entity crystal = new Entity(chrysalis, new Vector3f(x, y, z), 0, 0, 0, 10f);
            crystal.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseEntered(MouseEvent e) {
                    crystal.setHighlightColor(new Vector4f(1, 0, 0, 1));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    crystal.setHighlightColor(new Vector4f(0, 0, 0, 0));
                }

            });
            if(crystal.getPosition().y > 0) {
                scene.getEntities().add(crystal);
            }
            else i--;
        }

//        ParticleSystem system = new ParticleSystem(
//                100,
//                25,
//                .075f,
//                30,
//                1,
//                new ParticleTexture(
//                        ModelLoader.loadTexture("particleStar.png"),1, true
//                )
//        );
//        //system.setDirection(new Vector3f(20, 100, 20), .5f);
//        system.setLifeError(.1f);
//        system.setScaleError(1f);
//        system.setSpeedError(.7f);
//        scene.setParticleSystem(system);
//        WaterTile tile = new WaterTile(0, 0,0);
//        scene.add(tile);



        //Environment.run(new Count());
        Environment.start();
    }
}