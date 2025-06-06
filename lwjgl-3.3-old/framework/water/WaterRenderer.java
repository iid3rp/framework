package framework.water;

import framework.hardware.Display;
import framework.entity.Camera;
import framework.entity.Light;
import framework.lang.Mat4;
import framework.lang.Vec3;
import framework.loader.ModelLoader;
import framework.model.Model;
import framework.renderer.MasterRenderer;
import framework.textures.Texture;
import framework.lang.Math;
import framework.util.LinkList;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class WaterRenderer {

	private static final String DuDv_MAP = "dude.png";
	private static final String NORMALS_MAP = "normalWaterMap.png";
	private static final float WAVE_SPEED = 0.04f;
	private Model quad;
	private WaterShader shader;
	private WaterFrameBufferObject buffer;
	private Texture duDvTexture;
	private Texture normalTexture;
	private float moveFactor;

	public WaterRenderer(WaterShader shader, Mat4 projectionMatrix, WaterFrameBufferObject buffer) {
		this.shader = shader;
		this.buffer = buffer;
		duDvTexture = new Texture(ModelLoader.loadTexture(DuDv_MAP));
		normalTexture = new Texture(ModelLoader.loadTexture(NORMALS_MAP));
		shader.bind();
		shader.connectTextureUnits();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.unbind();
		setUpVAO();
	}

	public void render(LinkList<WaterTile> water, Camera camera, Light light) {
		prepareRender(camera, light);
		for (WaterTile tile : water) {
			MasterRenderer.disableCulling();
			Mat4 modelMatrix = Math.createTransformationMatrix(
					new Vec3(tile.getX(), tile.getHeight(), tile.getZ()), 0, 0, 0,
					WaterTile.TILE_SIZE);
			shader.loadModelMatrix(modelMatrix);
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, quad.vertexCount());
			MasterRenderer.enableCulling();
		}
		unbind();
	}

	private void prepareRender(Camera camera, Light light)
	{
		shader.bind();
		shader.loadViewMatrix(camera);
		moveFactor += WAVE_SPEED * Display.getDeltaInSeconds();
		moveFactor %= 1;
		shader.loadLight(light);
		shader.loadMoveFactor(moveFactor);
		GL30.glBindVertexArray(quad.vaoId());
		GL20.glEnableVertexAttribArray(0);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, buffer.getReflectionTexture());
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, buffer.getRefractionTexture());
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, duDvTexture.getID());
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalTexture.getID());
		GL13.glActiveTexture(GL13.GL_TEXTURE4);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, buffer.getRefractionDepthTexture());

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private void unbind()
	{
		GL11.glDisable(GL11.GL_BLEND);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		shader.unbind();
	}

	private void setUpVAO()
	{
		// Just x and z vertex positions here, y is set to 0 in v.shader
		float[] vertices = { -1, -1, -1, 1, 1, -1, 1, -1, -1, 1, 1, 1};
		quad = ModelLoader.loadToVaoInt(vertices, 2);
	}
}
