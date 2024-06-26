package particles;

import entity.Player;
import org.lwjgl.util.vector.Vector3f;
import render.DisplayManager;

public class Particle {
    private Vector3f position;
    private Vector3f velocity;
    private float gravity;
    private float lifeLength;
    private float rotation;
    private float scale;
    private ParticleTexture texture;

    private float elapsedTime = 0;

    public Particle(Vector3f position, Vector3f velocity, float gravity, float lifeLength, float rotation, float scale, ParticleTexture texture)
    {
        this.texture = texture;
        this.position = position;
        this.velocity = velocity;
        this.gravity = gravity;
        this.lifeLength = lifeLength;
        this.rotation = rotation;
        this.scale = scale;
    }

    public ParticleTexture getTexture()
    {
        return texture;
    }

    public void setTexture(ParticleTexture texture)
    {
        this.texture = texture;
    }

    public Particle(Vector3f position,
                    Vector3f velocity,
                    float gravity,
                    float lifeLength,
                    float rotation,
                    float scale)
    {
        this.position = position;
        this.velocity = velocity;
        this.gravity = gravity;
        this.lifeLength = lifeLength;
        this.rotation = rotation;
        this.scale = scale;
        ParticleMaster.addParticle(this);
    }

    public Vector3f getPosition()
    {
        return position;
    }

    public void setPosition(Vector3f position)
    {
        this.position = position;
    }

    public Vector3f getVelocity()
    {
        return velocity;
    }

    public void setVelocity(Vector3f velocity)
    {
        this.velocity = velocity;
    }

    public float getGravity()
    {
        return gravity;
    }

    public void setGravity(float gravity)
    {
        this.gravity = gravity;
    }

    public float getLifeLength()
    {
        return lifeLength;
    }

    public void setLifeLength(float lifeLength)
    {
        this.lifeLength = lifeLength;
    }

    public float getRotation()
    {
        return rotation;
    }

    public void setRotation(float rotation)
    {
        this.rotation = rotation;
    }

    public float getScale()
    {
        return scale;
    }

    public void setScale(float scale)
    {
        this.scale = scale;
    }

    public float getElapsedTime()
    {
        return elapsedTime;
    }

    protected boolean update()
    {
        velocity.y += Player.GRAVITY * gravity * DisplayManager.getFrameTimeSeconds();
        Vector3f change = new Vector3f(velocity);
        change.scale(DisplayManager.getFrameTimeSeconds());
        Vector3f.add(change, position, position);
        elapsedTime += DisplayManager.getFrameTimeSeconds();
        return elapsedTime < lifeLength;
    }
}
