package ar.edu.itba.ss.simulator;

import ar.edu.itba.ss.method.CellIndexMethod;
import ar.edu.itba.ss.model.Direction;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.collision.Collision;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.collision.ParticleCollision;
import ar.edu.itba.ss.model.collision.WallCollision;
import javafx.geometry.Point2D;

import java.util.*;
import java.util.stream.Collectors;

public class GasDiffusionSimulator {

    private final List<Particle> initialParticles;
    private final double boxHeight;
    private final double boxWidth;


    public GasDiffusionSimulator(List<Particle> initialParticles, double boxHeight, double boxWidth) {
        this.initialParticles = initialParticles;
        this.boxHeight = boxHeight;
        this.boxWidth = boxWidth;
    }

    public void simulate(){
        List<Particle> currentParticles = initialParticles;

        while (true){
            Collision collision = nextCollision(currentParticles);
            currentParticles = nextParticles(currentParticles,collision);
        }

    }

    private Collision nextCollision(List<Particle> particles){
        Set<Collision> collisions = new HashSet<>();
        particles.stream().forEach(p -> {
            collisions.add(nextCollisionOfSpecificParticle(p, particles));
        });

        return collisions.parallelStream().min(Comparator.comparingDouble(Collision::getTime)).get();
    }

    private Collision nextCollisionOfSpecificParticle(Particle particle, List<Particle> neighbours){
        Set<Collision> collisions = neighbours.parallelStream()
                .map(n -> collisionBetweenParticles(particle, n))
                .collect(Collectors.toSet());
        collisions.add(collisionWithHorizontalWall(particle));
        collisions.add(collisionWithVerticalWall(particle));

        return collisions.stream().min(Comparator.comparingDouble(Collision::getTime)).get();
    }

    private Collision collisionBetweenParticles(final Particle particle1, final Particle particle2){
        if(particle1.id() == particle2.id()){
            new ParticleCollision(-1,null,null);
        }
        Point2D deltaV = particle2.velocity().subtract(particle1.velocity());
        Point2D deltaR = particle2.position().subtract(particle1.position());
        double sigma = particle1.radius() + particle2.radius();
        double d = Math.pow(deltaV.dotProduct(deltaR),2) - (deltaV.dotProduct(deltaV))*(deltaR.dotProduct(deltaR) - sigma*sigma);

        if(deltaV.dotProduct(deltaR) >= 0 || d < 0)
            new ParticleCollision(-1,null,null);

        double time = -(deltaV.dotProduct(deltaR) + Math.sqrt(d))/(deltaV.dotProduct(deltaV));
        return new ParticleCollision(time, particle1, particle2);
    }

    private Collision collisionWithVerticalWall(final Particle particle){
        double time;
        Direction direction;

        if(particle.velocity().getX() > 0){
            time = (boxWidth - particle.radius() - particle.position().getX())/particle.velocity().getX();
            direction = Direction.EAST;
        } else {
            time = (0 + particle.radius() - particle.position().getX())/particle.velocity().getX();
            direction = Direction.WEST;
        }
        return new WallCollision(time,direction,particle);
    }

    private Collision collisionWithHorizontalWall(final Particle particle){
        double time;
        Direction direction;

        if(particle.velocity().getY() > 0){
            time = (boxHeight - particle.radius() - particle.position().getY())/particle.velocity().getY();
            direction = Direction.NORTH;
        } else {
            time = (0 + particle.radius() - particle.position().getY())/particle.velocity().getY();
            direction = Direction.SOUTH;
        }
        return new WallCollision(time,direction,particle);
    }

    private List<Particle> nextParticles(List<Particle> oldParticles, Collision collision){
        List<Particle> nextParticles = new LinkedList<>();
        List<Integer> collidedIds = collision.getParticles().stream().map(Particle::id).collect(Collectors.toList());

        nextParticles.addAll(collision.collide());
        oldParticles.parallelStream().forEach(p->{
            if(!collidedIds.contains(p.id())){
                nextParticles.add(ImmutableParticle.builder()
                        .from(p)
                        .position(calculateNewPosition(p,collision.getTime()))
                        .build());
            }
        });
        return nextParticles;
    }

    private Point2D calculateNewPosition(final Particle particle, final double time){
        double x = particle.position().getX() + particle.velocity().getX() * time;
        double y = particle.position().getY() + particle.velocity().getY() * time;
        return new Point2D(x, y);
    }
}
