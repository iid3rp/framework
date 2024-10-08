package framework.post_processing.blur;

import framework.shader.GLShader;

public class VerticalBlurShader extends GLShader
{

	private static final String VERTEX_FILE = "verticalBlurVertexShader.glsl";
	private static final String FRAGMENT_FILE = "blurFragmentShader.glsl";
	
	private int location_targetHeight;
	
	protected VerticalBlurShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}
	
	protected void loadTargetHeight(float height){
		super.loadFloat(location_targetHeight, height);
	}

	@Override
	protected void getAllUniformLocations() {	
		location_targetHeight = super.getUniformLocation("targetHeight");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}
}
