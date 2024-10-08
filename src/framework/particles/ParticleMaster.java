package framework.particles;

import framework.entity.Camera;
import framework.lang.Mat4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ParticleMaster
{
    public static Map<ParticleTexture, List<Particle>> particles = new HashMap<>();
    private static ParticleRenderer renderer;
    public static void initialize(Mat4 projection)
    {
        renderer = new ParticleRenderer(projection);
    }

    public static void update(Camera camera)
    {
        Iterator<Entry<ParticleTexture, List<Particle>>> it = particles.entrySet().iterator();
        while(it.hasNext())
        {
            List<Particle> list = it.next().getValue();
            Iterator<Particle> i = list.iterator();
            while(i.hasNext())
            {
                Particle particle = i.next();
                boolean stillAlive = particle.update(camera);
                if(!stillAlive)
                {
                    i.remove();
                    if(list.isEmpty())
                    {
                        it.remove();
                    }
                }
            }
            InsertionSort.sortHighToLow(list);
        }
    }

    public static void renderParticles(Camera camera)
    {
        renderer.render(particles, camera);
    }

    public static void dispose()
    {
        renderer.dispose();
    }

    public static void addParticle(Particle particle)
    {
        List<Particle> list = particles.computeIfAbsent(particle.getTexture(), k -> new ArrayList<>());
        list.add(particle);
    }
}
