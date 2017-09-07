package ar.edu.itba.ss.simulator;

import ar.edu.itba.ss.io.AppendFileParticlesWriter;
import ar.edu.itba.ss.method.CellIndexMethod;
import ar.edu.itba.ss.model.Direction;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.collision.Collision;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.collision.ParticleCollision;
import ar.edu.itba.ss.model.collision.WallCollision;
import javafx.geometry.Point2D;

import java.io.IOException;
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
        AppendFileParticlesWriter writer = new AppendFileParticlesWriter("./prueba");
        List<Particle> currentParticles = initialParticles;

        for (int i = 0; i < 500; i++) {
            try {
                writer.write(i,currentParticles);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Collision collision = nextCollision(currentParticles);
            currentParticles = nextParticles(currentParticles,collision);
        }

    }

    private Collision nextCollision(List<Particle> particles){
        Set<Collision> collisions = new HashSet<>();
        particles.stream().forEach(p -> {
            collisions.add(nextCollisionOfSpecificParticle(p, particles));
        });

        return collisions.stream().min(Comparator.comparingDouble(Collision::getTime)).get();
    }

    private Collision nextCollisionOfSpecificParticle(Particle particle, List<Particle> neighbours){
        Set<Collision> collisions = neighbours.stream()
                .map(n -> collisionBetweenParticles(particle, n))
                .collect(Collectors.toSet());
        collisions.add(collisionWithHorizontalWall(particle));
        collisions.add(collisionWithVerticalWall(particle));

        return collisions.stream().min(Comparator.comparingDouble(Collision::getTime)).get();
    }

    private Collision collisionBetweenParticles(final Particle particle1, final Particle particle2){
        if(particle1.id() == particle2.id()){
            //FIXME
            return new ParticleCollision(999999,null,null);
        }
        Point2D deltaV = particle2.velocity().subtract(particle1.velocity());
        Point2D deltaR = new Point2D(particle2.position().getX()*particle2.radius() - particle1.position().getX()*particle1.radius(),
            particle2.position().getY()*particle2.radius() - particle1.position().getY()*particle1.radius());
        double sigma = particle1.radius() + particle2.radius();
        double d = (deltaV.dotProduct(deltaR)*deltaV.dotProduct(deltaR))
                - (deltaV.dotProduct(deltaV))*(deltaR.dotProduct(deltaR) - sigma*sigma);

        if(deltaV.dotProduct(deltaR) >= 0 || d < 0){
            //FIXME
            return new ParticleCollision(999999,null,null);
        }

        double time = (deltaV.dotProduct(deltaR) + Math.sqrt(d))/(deltaV.dotProduct(deltaV));
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
        oldParticles.forEach(p->{
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
