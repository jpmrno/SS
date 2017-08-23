package ar.edu.itba.ss.generator;

import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import javafx.geometry.Point2D;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class RandomParticleGenerator {

    public static List<Particle> generate(Particle minParticle, Particle maxParticle){
//        TODO: validate parameters
        List<Particle> particles = new LinkedList<>();
        int id = minParticle.id();

        while(id <= maxParticle.id()){
            double x = ThreadLocalRandom.current().nextDouble(minParticle.position().getX(), maxParticle.position().getX());
            double y = ThreadLocalRandom.current().nextDouble(minParticle.position().getY(), maxParticle.position().getY());
            double r = minParticle.radius() == maxParticle.radius() ? minParticle.radius() : ThreadLocalRandom.current().nextDouble(minParticle.radius(), maxParticle.radius());
            Particle particle = ImmutableParticle.builder().id(id).position(new Point2D(x,y)).radius(r).build();

            if (validParticle(particle, particles)) {
                id++;
                particles.add(particle);
            }
        }

        return particles;
    }

    private static boolean validParticle(Particle particle, List<Particle> particleList){
        return !particleList.parallelStream().anyMatch(p -> collide(particle, p));
    }

//    TODO: move to Particle
    private static boolean collide(Particle particle1, Particle particle2){
        return particle1.position().distance(particle2.position()) < particle1.radius() + particle2.radius();
    }
}
