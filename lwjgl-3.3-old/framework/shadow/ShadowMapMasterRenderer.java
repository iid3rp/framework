package framework.shadow;

import framework.entity.Camera;
import framework.entity.Entity;
import framework.entity.Light;
import framework.lang.Mat4;
import framework.lang.Vec2;
import framework.lang.Vec3;
import framework.model.TexturedModel;
import framework.util.LinkList;


import static org.lwjgl.opengl.GL46.*;

/**
 * This class is in charge of using all the classes in the shadows package to
 * carry out the shadow render pass, i.e. rendering the scene to the shadow map
 * texture. This is the only class in the shadows package which needs to be
 * referenced from outside the shadows package.
 * 
 * @author Karl
 *
 */
public class ShadowMapMasterRenderer {

	private static final int SHADOW_MAP_SIZE = 4096;

	private ShadowFrameBuffer shadowFbo;
	private ShadowShader shader;
	public ShadowBox shadowBox;
	private Mat4 projectionMatrix = new Mat4();
	private Mat4 lightViewMatrix = new Mat4();
	private Mat4 projectionViewMatrix = new Mat4();
	private Mat4 offset = createOffset();

	private ShadowMapEntityRenderer entityRenderer;

	/**
	 * Creates instances of the important objects needed for rendering the scene
	 * to the shadow map. This includes the {@link ShadowBox} which calculates
	 * the position and size of the "view cuboid", the simple renderer and
	 * shader program that are used to render objects to the shadow map, and the
	 * {@link ShadowFrameBuffer} to which the scene is rendered. The size of the
	 * shadow map is determined here.
	 * 
	 * @param camera
	 *            - the camera being used in the scene.
	 */
	public ShadowMapMasterRenderer(Camera camera) {
		shader = new ShadowShader();
		shadowBox = new ShadowBox(lightViewMatrix, camera);
		shadowFbo = new ShadowFrameBuffer(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE);
		entityRenderer = new ShadowMapEntityRenderer(shader, projectionViewMatrix);
	}

	/**
	 * Carries out the shadow render pass. This renders the entities to the
	 * shadow map. First the shadow box is updated to calculate the size and
	 * position of the "view cuboid". The light direction is assumed to be
	 * "-lightPosition" which will be fairly accurate assuming that the light is
	 * very far from the scene. It then prepares to render, renders the entities
	 * to the shadow map, and finishes rendering.
	 * 
	 * @param entities
	 *            - the lists of entities to be rendered. Each list is
	 *            associated with the {@link TexturedModel} that all the
	 *            entities in that list use.
	 * @param sun
	 *            - the light acting as the sun in the scene.
	 */
	public void render(Map<TexturedModel, LinkList<Entity>> entities, Light sun) {
		shadowBox.update();
		Vec3 sunPosition = sun.getPosition();
		Vec3 lightDirection = new Vec3(-sunPosition.x, -sunPosition.y, -sunPosition.z);
		prepare(lightDirection, shadowBox);
		entityRenderer.render(entities);
		finish();
	}

	/**
	 * This biased projection-view matrix is used to convert fragments into
	 * "shadow map space" when rendering the main render pass. It converts a
	 * world space position into a 2D coordinate on the shadow map. This is
	 * needed for the second part of shadow mapping.
	 * 
	 * @return The to-shadow-map-space matrix.
	 */
	public Mat4 getToShadowMapSpaceMatrix() {
		return Mat4.mul(offset, projectionViewMatrix, null);
	}

	/**
	 * Clean up the shader and FBO on closing.
	 */
	public void dispose() {
		shader.dispose();
		shadowFbo.cleanUp();
	}

	/**
	 * @return The ID of the shadow map texture. The ID will always stay the
	 *         same, even when the contents of the shadow map texture change
	 *         each frame.
	 */
	public int getShadowMap() {
		return shadowFbo.getShadowMap();
	}

	/**
	 * @return The light's "view" matrix.
	 */
	protected Mat4 getLightSpaceTransform() {
		return lightViewMatrix;
	}

	/**
	 * Prepare for the shadow render pass. This first updates the dimensions of
	 * the orthographic "view cuboid" based on the information that was
	 * calculated in the {@link ShadowBox} class. The light's "view" matrix is
	 * also calculated based on the light's direction and the center position of
	 * the "view cuboid" which was also calculated in the {@link ShadowBox}
	 * class. These two matrices are multiplied together to create the
	 * projection-view matrix. This matrix determines the size, position, and
	 * orientation of the "view cuboid" in the world. This method also binds the
	 * shadows FBO so that everything rendered after this gets rendered to the
	 * FBO. It also enables depth testing, and clears any data that is in the
	 * FBOs depth attachment from last frame. The simple shader program is also
	 * started.
	 * 
	 * @param lightDirection
	 *            - the direction of the light rays coming from the sun.
	 * @param box
	 *            - the shadow box, which contains all the info about the
	 *            "view cuboid".
	 */
	private void prepare(Vec3 lightDirection, ShadowBox box) {
		updateOrthographicProjectionMatrix(box.getWidth(), box.getHeight(), box.getLength());
		updateLightViewMatrix(lightDirection, box.getCenter());
		Mat4.mul(projectionMatrix, lightViewMatrix, projectionViewMatrix);
		shadowFbo.bindFrameBuffer();
		glEnable(GL_DEPTH_TEST);
		glClear(GL_DEPTH_BUFFER_BIT);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		shader.bind();
	}

	/**
	 * Finish the shadow render pass. Stops the shader and unbinds the shadow
	 * FBO, so everything rendered after this point is rendered to the screen,
	 * rather than to the shadow FBO.
	 */
	private void finish() {
		shader.unbind();
		shadowFbo.unbindFrameBuffer();
	}

	/**
	 * Updates the "view" matrix of the light. This creates a view matrix which
	 * will line up the direction of the "view cuboid" with the direction of the
	 * light. The light itself has no position, so the "view" matrix is centered
	 * at the center of the "view cuboid". The created view matrix determines
	 * where and how the "view cuboid" is positioned in the world. The size of
	 * the view cuboid, however, is determined by the projection matrix.
	 * 
	 * @param direction
	 *            - the light direction, and therefore the direction that the
	 *            "view cuboid" should be pointing.
	 * @param center
	 *            - the center of the "view cuboid" in world space.
	 */
	private void updateLightViewMatrix(Vec3 direction, Vec3 center) {
		direction.normalize();
		center.negate();
		lightViewMatrix.identity();
		float pitch = (float) Math.acos(new Vec2(direction.x, direction.z).length());
		Mat4.rotate(pitch, Vec3.xAxis, lightViewMatrix, lightViewMatrix);
		float yaw = (float) Math.toDegrees(((float) Math.atan(direction.x / direction.z)));
		yaw = direction.z > 0 ? yaw - 180 : yaw;
		Mat4.rotate((float) -Math.toRadians(yaw), Vec3.yAxis, lightViewMatrix,
				lightViewMatrix);
		Mat4.translate(center, lightViewMatrix, lightViewMatrix);
	}

	/**
	 * Creates the orthographic projection matrix. This projection matrix
	 * basically sets the width, length and height of the "view cuboid", based
	 * on the values that were calculated in the {@link ShadowBox} class.
	 * 
	 * @param width
	 *            - shadow box width.
	 * @param height
	 *            - shadow box height.
	 * @param length
	 *            - shadow box length.
	 */
	private void updateOrthographicProjectionMatrix(float width, float height, float length) {
		projectionMatrix.identity();
		projectionMatrix.m00 = 2f / width;
		projectionMatrix.m11 = 2f / height;
		projectionMatrix.m22 = -2f / length;
		projectionMatrix.m33 = 1;
	}

	/**
	 * Create the offset for part of the conversion to shadow map space. This
	 * conversion is necessary to convert from one coordinate system to the
	 * coordinate system that we can use to sample to shadow map.
	 * 
	 * @return The offset as a matrix (so that it's easy to apply to other matrices).
	 */
	private static Mat4 createOffset() {
		Mat4 offset = new Mat4();
		offset.translate(new Vec3(0.5f, 0.5f, 0.5f));
		offset.scale(new Vec3(.5f, .5f, .5f));
		return offset;
	}
}
