package framework.water;

import java.nio.ByteBuffer;

import framework.hardware.DisplayManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

public class WaterFrameBufferObject
{
	protected static final int REFRACTION_WIDTH = DisplayManager.getWindowWidth();
	private static final int REFRACTION_HEIGHT = DisplayManager.getWindowHeight();
	protected static final int REFLECTION_WIDTH = REFRACTION_WIDTH / 4;
	private static final int REFLECTION_HEIGHT = REFRACTION_HEIGHT / 4;
	private static int SCREEN_WIDTH = REFLECTION_WIDTH / 4;
	private static int SCREEN_HEIGHT = REFLECTION_HEIGHT / 4;

	private int reflectionFrameBuffer;
	private int reflectionTexture;
	private int reflectionDepthBuffer;

	private int refractionFrameBuffer;
	private int refractionTexture;
	private int refractionDepthTexture;

	private int screenFrameBuffer;
	private int screenTexture;
	private int screenDepthTexture;

	public WaterFrameBufferObject() {
		//call when loading the game
		initializeScreenFrameBuffer();
		initializeReflectionFrameBuffer();
		initializeRefractionFrameBuffer();
	}

	public void dispose() {
		//call when closing the game
		GL30.glDeleteFramebuffers(reflectionFrameBuffer);
		GL11.glDeleteTextures(reflectionTexture);
		GL30.glDeleteRenderbuffers(reflectionDepthBuffer);

		GL30.glDeleteFramebuffers(refractionFrameBuffer);
		GL11.glDeleteTextures(refractionTexture);
		GL11.glDeleteTextures(refractionDepthTexture);

		GL30.glDeleteFramebuffers(screenFrameBuffer);
		GL11.glDeleteTextures(screenTexture);
		GL11.glDeleteTextures(screenDepthTexture);
	}

	public void bindScreenFrameBuffer()
	{
		bindFrameBuffer(screenFrameBuffer, SCREEN_WIDTH, SCREEN_HEIGHT);
	}

	public void bindReflectionFrameBuffer() {//call before rendering to this FBO
		bindFrameBuffer(reflectionFrameBuffer,REFLECTION_WIDTH,REFLECTION_HEIGHT);
	}

	public void bindRefractionFrameBuffer() {//call before rendering to this FBO
		bindFrameBuffer(refractionFrameBuffer,REFRACTION_WIDTH,REFRACTION_HEIGHT);
	}

	public void unbindCurrentFrameBuffer() {//call to switch to default frame buffer
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL11.glViewport(0, 0, DisplayManager.getWindowWidth(), DisplayManager.getWindowHeight());
	}

	public int getScreenTexture()
	{
		return screenTexture;
	}

	public int getReflectionTexture() {//get the resulting texture
		return reflectionTexture;
	}

	public int getRefractionTexture() {//get the resulting texture
		return refractionTexture;
	}

	public int getRefractionDepthTexture(){//get the resulting depth texture
		return refractionDepthTexture;
	}

	private void initializeReflectionFrameBuffer() {
		reflectionFrameBuffer = createFrameBuffer();
		reflectionTexture = createTextureAttachment(REFLECTION_WIDTH,REFLECTION_HEIGHT);
		reflectionDepthBuffer = createDepthBufferAttachment(REFLECTION_WIDTH,REFLECTION_HEIGHT);
		unbindCurrentFrameBuffer();
	}

	private void initializeRefractionFrameBuffer() {
		refractionFrameBuffer = createFrameBuffer();
		refractionTexture = createTextureAttachment(REFRACTION_WIDTH,REFRACTION_HEIGHT);
		refractionDepthTexture = createDepthTextureAttachment(REFRACTION_WIDTH,REFRACTION_HEIGHT);
		unbindCurrentFrameBuffer();
	}

	private void initializeScreenFrameBuffer()
	{
		screenFrameBuffer = createFrameBuffer();
		screenTexture = createTextureAttachment(SCREEN_WIDTH, SCREEN_HEIGHT);
		screenDepthTexture = createDepthTextureAttachment(SCREEN_WIDTH, SCREEN_HEIGHT);
		unbindCurrentFrameBuffer();
	}

	private void bindFrameBuffer(int frameBuffer, int width, int height){
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0); //To make sure the texture isn't bound
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
		GL11.glViewport(0, 0, width, height);
	}

	private int createFrameBuffer() {
		int frameBuffer = GL30.glGenFramebuffers();
		//generate name for frame buffer
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
		//create the framebuffer
		GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);

		//indicate that we will always render to color attachment 0
		return frameBuffer;
	}

	private int createTextureAttachment( int width, int height) {
		int texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height,
				0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
				texture, 0);
		return texture;
	}
	
	private int createDepthTextureAttachment(int width, int height){
		int texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, width, height,
				0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
				texture, 0);
		return texture;
	}

	private int createDepthBufferAttachment(int width, int height) {
		int depthBuffer = GL30.glGenRenderbuffers();
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width,
				height);
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
				GL30.GL_RENDERBUFFER, depthBuffer);
		return depthBuffer;
	}
}
