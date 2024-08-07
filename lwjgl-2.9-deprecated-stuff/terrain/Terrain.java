package framework.terrain;

import framework.model.Model;
import framework.display.ModelLoader;
import framework.texture.TerrainTexture;
import framework.texture.TerrainTexturePack;
import framework.toolbox.GeomMath;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Terrain
{
    public static final float SIZE = 800;
    private static final float MAX_HEIGHT = 150;
    private static final float MIN_HEIGHT = -100;
    private static final float MAX_PIXEL_COLOR = 256 * 256 * 256;
    private String heightMap;
    public float x;
    public float z;
    private Model model;
    private TerrainTexturePack texturePack;
    private TerrainTexture blendMap;

    private float[][] heights;
    private float size;

    public Terrain()
    {
        this.x = 0;
        this.z = 0;
    }

    public Terrain(float x, float z, TerrainTexturePack texturePack, TerrainTexture blendMap, String heightMap)
    {
        this.size = SIZE;
        this.x = x;
        this.z = z;
        this.texturePack = texturePack;
        this.blendMap = blendMap;
        this.heightMap = heightMap;
        this.model = generateTerrain(0);
    }

    public TerrainTexturePack getTexturePack()
    {
        return texturePack;
    }

    public TerrainTexture getBlendMap()
    {
        return blendMap;
    }

    public float getX()
    {
        return x;
    }

    public float getZ()
    {
        return z;
    }

    public Model getModel()
    {
        return model;
    }

    public float getHeightOfTerrain(float x, float z)
    {
        float terrainX = x + (getSize() / 2);
        float terrainZ = z + (getSize() / 2);
        float gridSquareSize = (size / (heights.length - 1));

        int gridX = (int) Math.floor(terrainX / gridSquareSize);
        int gridZ = (int) Math.floor(terrainZ / gridSquareSize);

        if(gridX >= heights.length - 1 || gridZ >= heights.length - 1 || gridX < 0 || gridZ < 0)
        {
            return 0;
        }

        float xCoordinate = (terrainX % gridSquareSize) / gridSquareSize;
        float zCoordinate = (terrainZ % gridSquareSize) / gridSquareSize;

        float result;

        if(xCoordinate <= (1 - zCoordinate))
        {
            result = GeomMath.barryCentric(
                    new Vector3f(0, heights[gridX][gridZ], 0),
                    new Vector3f(1, heights[gridX + 1][gridZ], 0),
                    new Vector3f(0, heights[gridX][gridZ + 1], 1),
                    new Vector2f(xCoordinate, zCoordinate));
        } else {
            result = GeomMath.barryCentric(
                    new Vector3f(1, heights[gridX + 1][gridZ], 0),
                    new Vector3f(1, heights[gridX + 1][gridZ + 1], 1),
                    new Vector3f(0, heights[gridX][gridZ + 1], 1),
                    new Vector2f(xCoordinate, zCoordinate));
        }
        return result;
    }

    private Model generateTerrain(String heightMap)
    {

        BufferedImage image;
        try {
            image = ImageIO.read(new File("resources/textures/" + heightMap + ".png"));
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }

        int vertexCount = image.getHeight();
        heights = new float[vertexCount][vertexCount];

        int count = vertexCount * vertexCount;
        float[] vertices = new float[count * 3];
        float[] normals = new float[count * 3];
        float[] textureCoords = new float[count * 2];
        int[] indices = new int[6 * (vertexCount - 1) * (vertexCount - 1)];
        int vertexPointer = 0;
        for(int i = 0; i < vertexCount; i++)
        {
            for(int j = 0; j < vertexCount; j++)
            {
                vertices[vertexPointer * 3] = (float) j / ((float)vertexCount - 1) * size;
                float height = getHeight(j, i, image);
                heights[j][i] = height;
                vertices[vertexPointer * 3 + 1] = height;
                vertices[vertexPointer * 3 + 2] = (float)i/((float)vertexCount - 1) * size;
                Vector3f normal = calculateNormal(j, i, image);
                normals[vertexPointer * 3] = normal.x;
                normals[vertexPointer * 3 + 1] = normal.y;
                normals[vertexPointer * 3 + 2] = normal.z;
                textureCoords[vertexPointer*2] = (float)j/((float)vertexCount - 1);
                textureCoords[vertexPointer*2+1] = (float)i/((float)vertexCount - 1);
                vertexPointer++;
            }
        }
        int pointer = 0;
        for(int gz = 0; gz < vertexCount - 1; gz++)
        {
            for(int gx = 0; gx < vertexCount - 1; gx++)
            {
                int topLeft = (gz * vertexCount) + gx;
                int topRight = topLeft + 1;
                int bottomLeft = ((gz + 1) * vertexCount) + gx;
                int bottomRight = bottomLeft + 1;
                indices[pointer++] = topLeft;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = topRight;
                indices[pointer++] = topRight;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = bottomRight;
            }
        }
        return ModelLoader.loadToVAO(vertices, textureCoords, normals, indices);
    }

    private Model generateTerrain(int seed)
    {
        HeightGenerator gen = new HeightGenerator();
        int vertexCount = 128;
        heights = new float[vertexCount][vertexCount];

        int count = vertexCount * vertexCount;
        float[] vertices = new float[count * 3];
        float[] normals = new float[count * 3];
        float[] textureCoords = new float[count * 2];
        int[] indices = new int[6 * (vertexCount - 1) * (vertexCount - 1)];
        int vertexPointer = 0;
        for(int i = 0; i < vertexCount; i++)
        {
            for(int j = 0; j < vertexCount; j++)
            {
                vertices[vertexPointer * 3] = (float) j / ((float)vertexCount - 1) * size;
                float height = getHeight(j, i, gen);
                heights[j][i] = height;
                vertices[vertexPointer * 3 + 1] = height;
                vertices[vertexPointer * 3 + 2] = (float)i/((float)vertexCount - 1) * size;
                Vector3f normal = calculateNormal(j, i, gen);
                normals[vertexPointer * 3] = normal.x;
                normals[vertexPointer * 3 + 1] = normal.y;
                normals[vertexPointer * 3 + 2] = normal.z;
                textureCoords[vertexPointer*2] = (float)j/((float)vertexCount - 1);
                textureCoords[vertexPointer*2+1] = (float)i/((float)vertexCount - 1);
                vertexPointer++;
            }
        }
        int pointer = 0;
        for(int gz = 0; gz < vertexCount - 1; gz++)
        {
            for(int gx = 0; gx < vertexCount - 1; gx++)
            {
                int topLeft = (gz * vertexCount) + gx;
                int topRight = topLeft + 1;
                int bottomLeft = ((gz + 1) * vertexCount) + gx;
                int bottomRight = bottomLeft + 1;
                indices[pointer++] = topLeft;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = topRight;
                indices[pointer++] = topRight;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = bottomRight;
            }
        }
        return ModelLoader.loadToVAO(vertices, textureCoords, normals, indices);
    }

    private Vector3f calculateNormal(int x, int z, BufferedImage image)
    {
        float heightL = getHeight(x - 1,  z, image);
        float heightR = getHeight(x + 1, z, image);
        float heightD = getHeight(x, z - 1, image);
        float heightU = getHeight(x, z + 1, image);
        Vector3f normal = new Vector3f(heightL - heightR, 2f, heightD - heightU);
        normal.normalise();
        return normal;

    }


    private float getHeight(int x, int z, BufferedImage image)
    {
        if (x + 1 >= heights.length || z + 1 >=heights.length || x < 0 || z < 0)
        {
            return 0;
        }
        float height = image.getRGB(x, z);
        height += MAX_PIXEL_COLOR / 2f;
        height /= MAX_PIXEL_COLOR / 2f;
        height *= MAX_HEIGHT;
        return height;
    }

    private Vector3f calculateNormal(int x, int z, HeightGenerator generator)
    {
        float heightL = getHeight(x - 1,  z, generator);
        float heightR = getHeight(x + 1, z, generator);
        float heightD = getHeight(x, z - 1, generator);
        float heightU = getHeight(x, z + 1, generator);
        Vector3f normal = new Vector3f(heightL - heightR, 2f, heightD - heightU);
        normal.normalise();
        return normal;

    }

    private float getHeight(int x, int z, HeightGenerator generator)
    {
        return generator.generateHeight(x, z);
    }

    public float getSize()
    {
        return size;
    }

    public void setSize(float size)
    {
        this.size = size;
        this.model = generateTerrain(0);
    }
}
