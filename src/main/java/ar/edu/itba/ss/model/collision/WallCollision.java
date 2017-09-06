package ar.edu.itba.ss.model.collision;

import ar.edu.itba.ss.model.Direction;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import javafx.geometry.Point2D;

import java.util.LinkedList;
import java.util.List;

public class WallCollision implements Collision {
    private final double time;
    private final Direction direction;
    private final Particle particle;

    public WallCollision(double time, Direction direction, Particle particle) {
        this.time = time;
        this.direction = direction;
        this.particle = particle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WallCollision that = (WallCollision) o;

        if (Double.compare(that.time, time) != 0) return false;
        if (direction != that.direction) return false;
        return particle.id() == that.particle.id();
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(time);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        result = 31 * result + (particle != null ? particle.hashCode() : 0);
        return result;
    }

    @Override
    public List<Particle> collide() {

        List<Particle> ret = new LinkedList<>();

        ret.add(ImmutableParticle.builder()
                .from(particle)
                .position(calculateNewPosition())
                .velocity(calculateNewVelocity())
                .build());

        return ret;
    }

    @Override
    public List<Particle> getParticles() {
        List<Particle> particles = new LinkedList<>();
        particles.add(particle);
        return particles;
    }

    @Override
    public double getTime() {
        return time;
    }

    private Point2D calculateNewPosition(){
        double x = particle.position().getX() + particle.velocity().getX() * time;
        double y = particle.position().getY() + particle.velocity().getY() * time;
        return new Point2D(x, y);
    }

    private Point2D calculateNewVelocity(){
        Point2D oldVelocity = particle.velocity();
        if(direction.isHorizontal()){
            return new Point2D(- oldVelocity.getX(),oldVelocity.getY());
        }
        return new Point2D(oldVelocity.getX(),-oldVelocity.getY());
    }
}
