package framework.renderer;

import framework.entity.Entity;
import framework.model.Model;
import framework.model.TexturedModel;
import framework.shader.EntityShader;
import framework.textures.Texture;
import framework.util.Math;
import framework.util.Key;
import framework.util.LinkList;
import framework.util.Map;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL46.*;

public class EntityRenderer {
    private final EntityShader staticShader;

    public EntityRenderer(EntityShader staticShader, Matrix4f projectionMatrix) {
        if (staticShader == null) {
            throw new IllegalArgumentException("staticShader argument has not been initialized!");
        }

        if (projectionMatrix == null) {
            throw new IllegalArgumentException("projectionMatrix argument has not been initialized!");
        }

        this.staticShader = staticShader;
        staticShader.bind();
        staticShader.loadProjectionMatrix(projectionMatrix);
        staticShader.connectTextureUnits();
        staticShader.unbind();
    }

    public void render(Map<TexturedModel, LinkList<Entity>> entities, Matrix4f shadow) {
        staticShader.loadShadowMatrix(shadow);

        for (Key<TexturedModel, LinkList<Entity>> key : entities) {
            prepareTexturedModel(key.getKey());
            LinkList<Entity> entityList = entities.get(key.getKey());

            for (Entity entity : entityList) {
                prepareEntity(entity);
                glDrawElements(GL_TRIANGLES, key.getKey().getModel().getVertexCount(), GL_UNSIGNED_INT, 0);    // Draw using index buffer and triangles
            }
            unbindTexturedModel();
        }
    }

    private void prepareTexturedModel(TexturedModel texturedModel) {
        Model rawModel = texturedModel.getModel();

        glBindVertexArray(rawModel.getVaoId());
        glEnableVertexAttribArray(0);   // VAO 0 = vertex spacial coordinates
        glEnableVertexAttribArray(1);   // VAO 1 = texture coordinates
        glEnableVertexAttribArray(2);   // VAO 2 = normals
        glEnableVertexAttribArray(3);   // VAO 3 = tangents

        Texture texture = texturedModel.getTexture();
        staticShader.loadNumberOfRowsInTextureAtlas(texture.getNumberOfRows());

        if (texture.hasTransparency()) {
            MasterRenderer.disableCulling();
        }

        staticShader.loadFakeLighting(texture.isUseFakeLighting());
        staticShader.loadShineVariables(texture.getShineDampening(), texture.getReflectivity());
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texturedModel.getTexture().getTextureId());   // sampler2D in fragment shader uses texture bank 0 by default
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, texturedModel.getTexture().getNormalMap()); // but then were using normals in the entities...

        staticShader.loadHasSpecularMap(texture.hasSpecularMap());
        if(texture.hasSpecularMap()) {
            glActiveTexture(GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_2D, texturedModel.getTexture().getTextureId());
        }
    }

    private void unbindTexturedModel() {
        MasterRenderer.enableCulling(); // make sure culling is enabled for the next model that renders
        glDisableVertexAttribArray(0);  // VAO 0 = vertex spacial coordinates
        glDisableVertexAttribArray(1);  // VAO 1 = texture coordinates
        glDisableVertexAttribArray(2);  // VAO 2 = normals
        glDisableVertexAttribArray(3);  // VAO 3 = tangents
        glBindVertexArray(0);   // Unbind the VAO
    }

    private void prepareEntity(Entity entity) {
        Matrix4f transformationMatrix = Math.createTransformationMatrix(entity.getPosition(), entity.getRotationX(), entity.getRotationY(), entity.getRotationZ(), entity.getScale());
        staticShader.loadTransformationMatrix(transformationMatrix);
        staticShader.loadHighlightColor(entity.getHighlightColor());
        staticShader.loadMouseEventColor(entity.getMouseColor());
        staticShader.loadOffset(entity.getTextureXOffset(), entity.getTextureYOffset());
    }
}