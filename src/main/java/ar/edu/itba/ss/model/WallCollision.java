package ar.edu.itba.ss.model;

import java.util.List;

public class WallCollision implements Collision {
//    TODO: HACER BIEN
    private final double time;

    public WallCollision(double time) {
        this.time = time;
    }

    @Override
    public List<Particle> collide() {
        return null;
    }

    @Override
    public double getTime() {
        return time;
    }
}
