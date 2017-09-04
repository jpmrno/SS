package ar.edu.itba.ss.model;

import java.util.List;

public interface Collision {

    List<Particle> collide();

    double getTime();
}
