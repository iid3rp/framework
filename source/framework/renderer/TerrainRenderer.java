package framework.renderer;

import framework.model.Model;
import framework.shader.TerrainShader;
import framework.terrains.Terrain;
import framework.textures.TerrainTexturePack;
import framework.utils.GeomMath;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class TerrainRenderer {
    private TerrainShader terrainShader;

    public TerrainRenderer(TerrainShader terrainShader, Matrix4f projectionMatrix) {
        if (terrainShader == null) {
            throw new IllegalArgumentException("terrainShader argument has not been initialized!");
        }

        if (projectionMatrix == null) {
            throw new IllegalArgumentException("projectionMatrix argument has not been initialized!");
        }

        this.terrainShader = terrainShader;
        terrainShader.bind();
        terrainShader.loadProjectionMatrix(projectionMatrix);
        terrainShader.connectTextureUnits();
        terrainShader.unbind();
    }

    public void render(List<Terrain> terrainList) {
        for (Terrain terrain : terrainList) {
            prepareTerrain(terrain);
            loadModelMatrix(terrain);
            glDrawElements(GL_TRIANGLES, terrain.getModel().getVertexCount(), GL_UNSIGNED_INT, 0);    // Draw using index buffer and triangles
            unbindTexturedModel();
        }
    }

    private void bindTextures(Terrain terrain) {
        TerrainTexturePack terrainTexturePack = terrain.getTerrainTexturePack();

        // Bind background texture to texture bank 0
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, terrainTexturePack.getBackground().getTextureId());

        // Bind r texture to texture bank 1
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, terrainTexturePack.getRed().getTextureId());

        // Bind g texture to texture bank 2
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, terrainTexturePack.getGreen().getTextureId());

        // Bind b texture to texture bank 3
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, terrainTexturePack.getBlue().getTextureId());

        // Bind blend map texture to texture bank 4
        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, terrain.getBlendMap().getTextureId());
    }

    private void prepareTerrain(Terrain terrain) {
        Model rawModel = terrain.getModel();

        glBindVertexArray(rawModel.getVaoId());
        glEnableVertexAttribArray(0);   // VAO 0 = vertex spacial coordinates
        glEnableVertexAttribArray(1);   // VAO 1 = texture coordinates
        glEnableVertexAttribArray(2);   // VAO 2 = normals

        bindTextures(terrain);
        // terrainShader.loadSpecularLight(texture.getShineDamper(), texture.getReflectivity());
        terrainShader.loadSpecularLight(1, 0);  // just for now
    }

    private void unbindTexturedModel() {
        glDisableVertexAttribArray(0);  // VAO 0 = vertex spacial coordinates
        glDisableVertexAttribArray(1);  // VAO 1 = texture coordinates
        glDisableVertexAttribArray(2);  // VAO 2 = normals
        glBindVertexArray(0);   // Unbind the VAO
    }

    private void loadModelMatrix(Terrain terrain) {
        Matrix4f transformationMatrix = GeomMath.createTransformationMatrix(
                new Vector3f(terrain.getX() - (terrain.getSize() / 2), 0, terrain.getZ() - (terrain.getSize() / 2)), 0, 0, 0, 1);
        terrainShader.loadTransformationMatrix(transformationMatrix);
    }
}