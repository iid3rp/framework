package framework.normals;

import framework.entity.Light;
import framework.shader.GLShader;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.util.List;

public class NormalMappingShader extends GLShader
{
	private static final String VERTEX_FILE = "normalMapVertexShader.glsl";
	private static final String FRAGMENT_FILE = "normalMapFragmentShader.glsl";
	
	private int location_transformationMatrix;
	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int[] location_lightPositionEyeSpace;
	private int[] location_lightColor;
	private int[] location_attenuation;
	private int location_shineDamper;
	private int location_reflectivity;
	private int location_skyColor;
	private int location_numberOfRows;
	private int location_offset;
	private int location_plane;
	private int location_modelTexture;
	private int location_normalMap;

	public NormalMappingShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "textureCoordinates");
		super.bindAttribute(2, "framework/normal");
		super.bindAttribute(3, "tangent");
	}

	@Override
	protected void getAllUniformLocations() {
		location_transformationMatrix = super.getUniformLocation("transformationMatrix");
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		location_shineDamper = super.getUniformLocation("shineDamper");
		location_reflectivity = super.getUniformLocation("reflectivity");
		location_skyColor = super.getUniformLocation("skyColor");
		location_numberOfRows = super.getUniformLocation("numberOfRows");
		location_offset = super.getUniformLocation("offset");
		location_plane = super.getUniformLocation("plane");
		location_modelTexture = super.getUniformLocation("modelTexture");
		location_normalMap = super.getUniformLocation("normalMap");

		location_lightPositionEyeSpace = new int[lights];
		location_lightColor = new int[lights];
		location_attenuation = new int[lights];
		for(int i=0;i<lights;i++){
			location_lightPositionEyeSpace[i] = super.getUniformLocation("lightPositionEyeSpace[" + i + "]");
			location_lightColor[i] = super.getUniformLocation("lightColor[" + i + "]");
			location_attenuation[i] = super.getUniformLocation("attenuation[" + i + "]");
		}
	}
	
	protected void connectTextureUnits(){
		super.loadInteger(location_modelTexture, 0);
		super.loadInteger(location_normalMap, 1);
	}
	
	protected void loadClipPlane(Vector4f plane){
		super.loadVector4f(location_plane, plane);
	}
	
	protected void loadNumberOfRows(int numberOfRows){
		super.loadFloat(location_numberOfRows, numberOfRows);
	}
	
	protected void loadOffset(float x, float y){
		super.loadVector2f(location_offset, new Vector2f(x,y));
	}
	
	protected void loadSkyColor(float r, float g, float b){
		super.loadVector3f(location_skyColor, new Vector3f(r,g,b));
	}
	
	protected void loadShineVariables(float damper,float reflectivity){
		super.loadFloat(location_shineDamper, damper);
		super.loadFloat(location_reflectivity, reflectivity);
	}
	
	protected void loadTransformationMatrix(Matrix4f matrix){
		super.loadMatrix(location_transformationMatrix, matrix);
	}
	
	protected void loadLights(List<Light> lights, Matrix4f viewMatrix) {
		for(int i = 0; i < super.lights; i++)
		{
			if(i < lights.size()){
				super.loadVector3f(location_lightPositionEyeSpace[i], getEyeSpacePosition(lights.get(i), viewMatrix));
				super.loadVector3f(location_lightColor[i], lights.get(i).getColor());
				super.loadVector3f(location_attenuation[i], lights.get(i).getAttenuation());
			}else{
				super.loadVector3f(location_lightPositionEyeSpace[i], new Vector3f(0, 0, 0));
				super.loadVector3f(location_lightColor[i], new Vector3f(0, 0, 0));
				super.loadVector3f(location_attenuation[i], new Vector3f(1, 0, 0));
			}
		}
	}
	
	protected void loadViewMatrix(Matrix4f viewMatrix){
		super.loadMatrix(location_viewMatrix, viewMatrix);
	}
	
	protected void loadProjectionMatrix(Matrix4f projection){
		super.loadMatrix(location_projectionMatrix, projection);
	}
	
	private Vector3f getEyeSpacePosition(Light light, Matrix4f viewMatrix){
		Vector3f position = light.getPosition();
		Vector4f eyeSpacePos = new Vector4f(position.x,position.y, position.z, 1f);
		Matrix4f.transform(viewMatrix, eyeSpacePos, eyeSpacePos);
		return new Vector3f(eyeSpacePos);
	}
	
	

}
