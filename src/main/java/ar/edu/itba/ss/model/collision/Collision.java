package ar.edu.itba.ss.model.collision;

import ar.edu.itba.ss.model.Particle;

import java.util.List;

public interface Collision {

    List<Particle> collide();

    List<Particle> getParticles();

    double getTime();
}
