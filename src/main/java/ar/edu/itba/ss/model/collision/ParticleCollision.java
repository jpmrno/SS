package ar.edu.itba.ss.model.collision;

import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import javafx.geometry.Point2D;

import java.util.LinkedList;
import java.util.List;

public class ParticleCollision implements Collision {

    private final double time;
    private final Particle particle1;
    private final Particle particle2;

    public ParticleCollision(double time, Particle particle1, Particle particle2) {
        this.time = time;
        this.particle1 = particle1;
        this.particle2 = particle2;
    }

    @Override
    public List<Particle> collide() {
        double sigma = particle1.radius() + particle2.radius();
        Point2D deltaV = particle2.velocity().subtract(particle1.velocity());
        Point2D deltaR = particle2.position().subtract(particle1.position());
        double j = (2 * particle1.mass() * particle2.mass() * (deltaV.dotProduct(deltaR)))
                / (sigma * (particle1.mass() + particle2.mass()));
        double jx = (j * (particle2.position().getX() - particle1.position().getX()))/sigma;
        double jy = (j * (particle2.position().getY() - particle1.position().getY()))/sigma;
        double vx1 = particle1.velocity().getX() + jx/particle1.mass();
        double vx2 = particle2.velocity().getX() - jx/particle2.mass();
        double vy1 = particle1.velocity().getY() + jy/particle1.mass();
        double vy2 = particle2.velocity().getY() - jy/particle2.mass();

        List<Particle> ret = new LinkedList<>();

        ret.add(ImmutableParticle.builder()
                .from(particle1)
                .position(calculateNewPosition(particle1))
                .velocity(new Point2D(vx1,vy1))
                .build());
        ret.add(ImmutableParticle.builder()
                .from(particle2)
                .position(calculateNewPosition(particle2))
                .velocity(new Point2D(vx2,vy2))
                .build());

        return ret;
    }

    @Override
    public List<Particle> getParticles() {
        List<Particle> particles = new LinkedList<>();
        particles.add(particle1);
        particles.add(particle2);
        return particles;
    }

    @Override
    public double getTime() {
        return time;
    }

    private Point2D calculateNewPosition(final Particle particle){
        double x = particle.position().getX() + particle.velocity().getX() * time;
        double y = particle.position().getY() + particle.velocity().getY() * time;
        return new Point2D(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParticleCollision that = (ParticleCollision) o;

        if (Double.compare(that.time, time) != 0) return false;
        if(particle1 == null || particle2 == null) return false;
        return (particle1.id() == that.particle1.id() && particle2.id() == that.particle2.id()) ||
                (particle1.id() == that.particle2.id() && particle2.id() == that.particle1.id());
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(time);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (particle1 != null ? particle1.hashCode() : 0);
        result = 31 * result + (particle2 != null ? particle2.hashCode() : 0);
        return result;
    }

    public Particle getParticle1() {
        return particle1;
    }

    public Particle getParticle2() {
        return particle2;
    }
}
