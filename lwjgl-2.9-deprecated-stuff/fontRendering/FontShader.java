package framework.fontRendering;

import framework.shader.GLShader;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class FontShader extends GLShader
{

	private static final String VERTEX_FILE = "fontVertexShader.glsl";
	private static final String FRAGMENT_FILE = "fontFragmentShader.glsl";
	private int locationColor;
	private int locationTranslation;
	
	public FontShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocations()
	{
		locationColor = super.getUniformLocation("color");
		locationTranslation = super.getUniformLocation("translation");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "textureCoords");
	}

	protected void loadColor(Vector3f color)
	{
		super.loadVector3f(locationColor, color);
	}

	protected void loadTranslation(Vector2f translation)
	{
		super.loadVector2f(locationTranslation, translation);
	}


}
