package framework.fontExperiment;

import framework.h.Display;
import framework.loader.ModelLoader;
import framework.model.Model;
import framework.util.GeomMath;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;

public class TextEntityRenderer
{
    private Model quad;
    private static TextShader shader;
    private Vector2f size;
    private int cursorX;
    private int cursorY;
    public TextEntityRenderer()
    {
        float[] positions = {-1, -1, -1, 1, 1, -1, 1, -1, -1, 1, 1, 1};
        quad = ModelLoader.loadToVao(positions, 2);
        size = new Vector2f(1, 1);
        shader = new TextShader();
    }

    public void render(Font font, Text text)
    {
        shader.bind();
        GL30.glBindVertexArray(quad.getVaoId());
        GL20.glEnableVertexAttribArray(0);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        renderText(font, text);

        GL11.glEnable((GL11.GL_DEPTH_TEST));
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        shader.unbind();
    }

    private void renderText(Font font, Text text)
    {
        cursorX = 0;
        cursorY = 0;

        // rendering
        List<Char> chars = new ArrayList<>();
        for(char c : text.getText().toCharArray()) {
            if(c == ' ')
            {
                setWord(font, chars, text);
                chars = new ArrayList<>();
            }
            else if(c == '\n')
            {
                setWord(font, chars, text);
                cursorY += font.getLineHeight();
                chars = new ArrayList<>();
            }
            else
            {
                chars.add(font.getCharacterMap().get(c));
            }
        }
        setWord(font, chars, text);
    }

    private void setWord(Font font, List<Char> chars, Text text)
    {
        int width = 0;
        for(Char c : chars)
            width += c.getWidth();
//        if (width > text.getMaxWidth()) {
//            cursorY += font.getLineHeight();
//            cursorX = 0;
//        }
        for(Char c : chars) {
            renderCharacter(c, font, text);
            cursorX += c.getXAdvance();
        }
        cursorX += font.getCharacterMap().get(' ').getXAdvance(); // space character
    }

    private void renderCharacter(Char c, Font font, Text text)
    {
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, font.getTexture());

        Vector2f size = setSize(c.getWidth(), c.getHeight());
        Vector2f pos = setLocation(cursorX + c.getXOffset(), cursorY + c.getYOffset(), size);

        // set the transformation matrix of the whole font texture
        Matrix4f letterMatrix = GeomMath.createTransformationMatrix(
                (pos),
                new Vector3f(0),
                (size)
        );

        // set the transformation matrix of each letter...
        Matrix4f scaleMatrix = GeomMath.createTransformationMatrix(
                getNormal(c.getX(), c.getY()),
                new Vector3f(0),
                getNormal(font.getScaleW(), font.getScaleH())
        );

        shader.loadTexturePosition(getNormal(c.getX(), c.getY()));
        shader.loadOffset(getNormal(c.getXOffset(), c.getYOffset()));
        shader.loadPosition(getNormal(c.getX(), c.getY()));
        shader.loadSize(getNormal(font.getScaleW(), font.getScaleH()));
        shader.loadScale(getNormal(c.getWidth(), c.getHeight()));
        shader.loadScaleMatrix(scaleMatrix);
        shader.loadTransformation(letterMatrix);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, quad.getVertexCount());
        //System.out.println(c + " cursorX: " + cursorX + " cursorY: " + cursorY);
    }

    public Vector2f setSize(int width, int height)
    {
        float w = (float) width / Display.getWidth();
        float h = (float) height / Display.getHeight();
        return new Vector2f(w, h);
    }

    public Vector2f setLocation(int x, int y, Vector2f size)
    {
        float posX = (((float) x / (Display.getWidth())) * 2) - 1;
        float posY = 1 - (((float) y / (Display.getHeight())) * 2);

        posX += size.x;
        posY -= size.y;

        return new Vector2f(posX, posY);
    }

    public Vector2f getNormal(int x, int y)
    {
        float _x = (float) x / Display.getWidth();
        float _y = (float) y / Display.getHeight();
        return new Vector2f(_x, _y);
    }
}