package framework.post_processing.blur;

import framework.post_processing.ImageRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class HorizontalBlur {
	
	private ImageRenderer renderer;
	private HorizontalBlurShader shader;
	
	public HorizontalBlur(int targetFboWidth, int targetFboHeight){
		shader = new HorizontalBlurShader();
		shader.bind();
		shader.loadTargetWidth(targetFboWidth);
		shader.unbind();
		renderer = new ImageRenderer(targetFboWidth, targetFboHeight);
	}
	
	public void render(int texture){
		shader.bind();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		renderer.renderQuad();
		shader.unbind();
	}
	
	public int getOutputTexture(){
		return renderer.getOutputTexture();
	}
	
	public void dispose(){
		renderer.dispose();
		shader.dispose();
	}

}
