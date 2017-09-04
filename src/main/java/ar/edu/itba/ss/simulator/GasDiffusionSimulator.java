package ar.edu.itba.ss.simulator;

import ar.edu.itba.ss.method.CellIndexMethod;
import ar.edu.itba.ss.model.Collision;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.ParticleCollision;
import javafx.geometry.Point2D;

import java.util.*;
import java.util.stream.Collectors;

public class GasDiffusionSimulator {

    private final List<Particle> initialParticles;
    private final double rc;
    private final double k;
    private final CellIndexMethod neighbourFinder;
//      TODO: should be here?
    private static double MAX_RADIUS = 0.0015;


    public GasDiffusionSimulator(List<Particle> initialParticles, double rc, double k) {
        this.initialParticles = initialParticles;
        this.rc = rc;
        this.k = k;
        this.neighbourFinder = new CellIndexMethod(0.24,false);
    }

    public void simulate(){
        List<Particle> currentParticles = initialParticles;

        while (true){
            Collision collision = nextCollision(currentParticles);
            double tc = collision.getTime();
//          TODO: move particles
            List<Particle> nextParticles = new LinkedList<>();


//          TODO: check if the state should be saved

//          TODO: calculate velocities
        }

    }

    private Collision nextCollision(List<Particle> particles){
        Map<Particle, Set<Neighbour>> neighbours = neighbourFinder.apply(particles,MAX_RADIUS,rc);

        Set<Collision> collisions = new HashSet<>();
        neighbours.entrySet().parallelStream().forEach(n->{
            collisions.add(nextCollisionOfSpecificParticle(n.getKey(),n.getValue()));
        });

        return collisions.parallelStream().min(Comparator.comparingDouble(Collision::getTime)).get();
    }

    private Collision nextCollisionOfSpecificParticle(Particle particle, Set<Neighbour> neighbours){
        Set<Collision> collisions = neighbours.parallelStream()
                .map(n -> collisionBetweenParticles(particle, n.getNeighbourParticle()))
                .collect(Collectors.toSet());

//        TODO: calculate collision time to walls

        return collisions.parallelStream().min(Comparator.comparingDouble(Collision::getTime)).get();
    }

    private Collision collisionBetweenParticles(Particle particle1, Particle particle2){
        Point2D deltaV = particle2.velocity().subtract(particle1.velocity());
        Point2D deltaR = particle2.position().subtract(particle1.position());
        double sigma = particle1.radius() + particle2.radius();
        double d = Math.pow(deltaV.dotProduct(deltaR),2) - (deltaV.dotProduct(deltaV))*(deltaR.dotProduct(deltaR) - sigma*sigma);

        if(deltaV.dotProduct(deltaR) >= 0 || d < 0)
            return null;

        double time = -(deltaV.dotProduct(deltaR) + Math.sqrt(d))/(deltaV.dotProduct(deltaV));
        return new ParticleCollision(time, particle1, particle2);
    }
}
