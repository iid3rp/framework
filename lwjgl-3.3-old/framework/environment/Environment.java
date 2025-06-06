package framework.environment;

import framework.Scratch;
import framework.font.Char;
import framework.font.Font;
import framework.font.FontFile;
import framework.font.Text;
import framework.hardware.Display;
import framework.hardware.Keyboard;
import framework.lang.Vec3;
import framework.lang.Vec4;
import framework.particles.ParticleMaster;
import framework.post_processing.FrameBufferObject;
import framework.post_processing.PostProcessing;
import framework.renderer.MasterRenderer;
import framework.loader.ModelLoader;
import framework.event.MouseEvent;
import framework.scripting.FrameworkScript;
import framework.scripting.StackScript;
import framework.swing.PictureBox;

import java.awt.Color;
import java.util.List;

import static org.lwjgl.opengl.GL46.*;

// the commented codes will be uncommented once the game is set up!
public final class Environment
{
    public static Scene scene;
    private static final StackScript stack = new StackScript();
    public static FrameBufferObject multi = new FrameBufferObject(Display.getWidth(), Display.getHeight());
    public static FrameBufferObject out = new FrameBufferObject(Display.getWidth(), Display.getHeight(), FrameBufferObject.DEPTH_TEXTURE);
    public static FrameBufferObject bright = new FrameBufferObject(Display.getWidth(), Display.getHeight(), FrameBufferObject.DEPTH_TEXTURE);
    public static FrameBufferObject mouseEventBuffer = new FrameBufferObject(Display.getWidth(), Display.getHeight(), FrameBufferObject.DEPTH_TEXTURE);
    public static FrameBufferObject pixelBuffer = new FrameBufferObject(2, 2, FrameBufferObject.DEPTH_TEXTURE);

    public static boolean bloom = false;
    private static boolean shadows = true;

    public static void start()
    {
        // static method calling goes here:
        MasterRenderer.setRenderer(scene.getCamera());
        PostProcessing.initialize();
        ParticleMaster.initialize(MasterRenderer.getProjectionMatrix());

        if(scene != null)
        {
            MouseEvent event = new MouseEvent();
            event.setCamera(getScene().getCamera());
            event.setProjection(MasterRenderer.getProjectionMatrix());
            getScene().setEvent(event);
            //scene.getContentPane().add(fps);

            // these are just example of implementations for future debugging...
            //
            PictureBox pb = new PictureBox();
            pb.setTexture(MasterRenderer.getShadowMapTexture());
            pb.setLocation(10, 10);
            pb.setScale(200, 200);
            pb.setSize(200, 200);
            //scene.getContentPane().add(pb);


            loop();
            exit();
        }
        else throw new NullPointerException("no scene applied in the render.");
    }

    public static void loop()
    {
        long i = 0;

        // example implementation...
        Font x = FontFile.readFont("comic");
        List<Char> chars = x.getCharacters();
        Text text = new Text();
        text.setText(Scratch.guess.toUpperCase());
        text.setMaxWidth(1000);
        text.setFontSize(40);
        text.setSize(200, 500);
        text.setForegroundColor(new Color(0x2B35CD));

        while(Display.shouldDisplayClose())
        {
            //FPSCounter.update();
            // mouseEvent stuff
            scene.getEvent().update();

            //the shadow thingies
            if(shadows)
                MasterRenderer.renderShadowMap(scene.getEntities(), scene.getMainLight());

            //particle

            if(scene.getParticleSystem() != null) {
                Vec3 pos = new Vec3(scene.getPlayer().getPosition());
                pos.y += 300;
                scene.getParticleSystem().generateParticles(pos);
                ParticleMaster.update(scene.getCamera());
            }


            //if(scene.getParticleSystem() != null) {
            //}
            //ParticleMaster.renderParticles(scene.getCamera());

            // debuggers
            //System.out.println(scene.getEvent().getCurrentTerrainPoint());
            //System.out.println(scene.getEvent().currentRay);


            // the player and the camera movements
            scene.getPlayer().move(scene.getTerrain());
            scene.getCamera().move();


            // frame buffers thingy:
            // apparently, the rendering process in this stuff is that,
            // the first 3 clips [0, 1, 2] have certain black spots
            // in their skyboxes, but now it is gone in the
            // fourth index, finally.
            // took me a day to fix that :sob:
//            GL11.glEnable(GL30.GL_CLIP_DISTANCE1);
////
////            // the reflection of the water
//            MasterRenderer.buffer.bindReflectionFrameBuffer();
//            float distance = 2 * (scene.getCamera().getPosition().y - 0);
//            scene.getCamera().getPosition().y -= distance;
//            scene.getCamera().invertPitch();
//            renderScene(new Vector4f(0, 1, 0, -.001f));
//            scene.getCamera().getPosition().y += distance;
//            scene.getCamera().invertPitch();
////
//            //the refraction of the water
//            MasterRenderer.buffer.bindRefractionFrameBuffer();
//            renderScene(new Vector4f(0, -1, 0, 0));
////
//            GL11.glDisable(GL30.GL_CLIP_DISTANCE1);
//            MasterRenderer.buffer.unbindCurrentFrameBuffer();


            // frame buffer stuff
            multi.bindFrameBuffer();
            renderScene(new Vec4(0, -1, 0, 1000000));
            //MasterRenderer.renderWaters(scene.getWaters(), scene.getCamera(), scene.getMainLight());
            if(scene.getParticleSystem() != null)
            {
                ParticleMaster.renderParticles(scene.getCamera());
                System.out.println("Dfs");
            }
            multi.unbindFrameBuffer();

            //multi.resolveToScreen();
            //multi.resolveToFrameBufferObject(GL_COLOR_ATTACHMENT2, mouseEventBuffer);
            multi.resolveToScreen();
            //multi.resolvePixel(mouseEventBuffer);
            //if(i % 2 == 0)
            //     scene.getEvent().resolveColorPickFromPixel(mouseEventBuffer, pixelBuffer);
            //i++;


            if(bloom || Keyboard.isKeyDown(Keyboard.G))
            {
                multi.resolveToFrameBufferObject(GL_COLOR_ATTACHMENT1, bright);
                multi.resolveToFrameBufferObject(GL_COLOR_ATTACHMENT0, out);
                PostProcessing.doPostProcessing(out.getColorTexture(), bright.getColorTexture());
            }


            // text renderer
            // wont be rendered for now...
            //textEntityRenderer.render(x, text);

            scene.getContentPane().render(scene.getContentPane().getComponents());
            //text.setText("seconds: " + count.seconds);
            //TextMasterRenderer.render();
            runAllScripts();
            Display.updateDisplay();
        }
    }

    private static void runAllScripts()
    {
        FrameworkScript reference = null;
        for(FrameworkScript script : stack)
        {
            script.run(scene);
            if(!script.whilst())
                reference = script;
        }
        if(reference != null) {
            boolean x = stack.remove(reference);
        }
    }

    private static void renderScene(Vec4 vec4)
    {
        // the 3D space stuff...
        // the shadow thingies
        MasterRenderer.processTerrain(scene.getTerrain());
        MasterRenderer.processAllEntities(scene.getEntities());
        MasterRenderer.render(scene.getLights(), scene.getCamera(), vec4);
    }

    public static void run(FrameworkScript script)
    {
        stack.run(script);
    }

    public static void setScene(Scene scene)
    {
        Environment.scene = scene;
    }

    public static Scene getScene()
    {
        return scene;
    }

    private static void exit()
    {
        //pixel.dispose();
        PostProcessing.dispose();
        multi.dispose();
        out.dispose();
        mouseEventBuffer.dispose();
        bright.dispose();
        ParticleMaster.dispose();
        //TextMasterRenderer.dispose();
        scene.getContentPane().dispose();
        MasterRenderer.dispose();
        ModelLoader.destroy();
        Display.closeDisplay();
    }
}
