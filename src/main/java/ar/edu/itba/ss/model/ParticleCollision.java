package ar.edu.itba.ss.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Point2D;

public class ParticleCollision implements Comparable<ParticleCollision> {

  private List<Particle> particlesAfterCollision;
  private double elapsedTime;

  private ParticleCollision(final List<Particle> particlesAfterCollision,
      final double elapsedTime) {
    this.particlesAfterCollision = particlesAfterCollision;
    this.elapsedTime = elapsedTime;
  }

  public List<Particle> getParticlesAfterCollision() {
    return particlesAfterCollision;
  }

  public double getElapsedTime() {
    return elapsedTime;
  }

  public static Optional<ParticleCollision> withWall(final Particle particle, final Point2D start,
      final Point2D end) {

    if (particle.velocity().equals(Point2D.ZERO)) {
      return Optional.empty();
    }

    double d, p, v;
    Point2D newVelocity;
    // Horizontal
    if (start.getY() == end.getY() && start.getX() != end.getX()) {
      if (particle.position().getX() > end.getX() || particle.position().getX() < start.getX()) {
        return Optional.empty();
      }

      p = particle.position().getY();
      v = particle.velocity().getY();
      d = start.getY();
      newVelocity = new Point2D(particle.velocity().getX(), -1 * particle.velocity().getY());
      // Vertical
    } else if (start.getX() == end.getX() && start.getY() != end.getY()) {
      if (particle.position().getY() > end.getY() || particle.position().getY() < start.getY()) {
        return Optional.empty();
      }

      p = particle.position().getX();
      v = particle.velocity().getX();
      d = start.getX();
      newVelocity = new Point2D(-1 * particle.velocity().getX(), particle.velocity().getY());
    } else {
      throw new IllegalArgumentException("Unsupported wall");
    }

    final double elapsedTime = (d + (v > 0 ? -1 : 1) * particle.radius() - p) / v;

    if (elapsedTime < 0) {
      return Optional.empty();
    }

    final List<Particle> particlesAfterCollision = new LinkedList<>();
    particlesAfterCollision.add(ImmutableParticle.builder()
        .from(particle)
        .position(Points.linearMotion(particle.position(), particle.velocity(), elapsedTime))
        .velocity(newVelocity)
        .build());

    return Optional.of(new ParticleCollision(particlesAfterCollision, elapsedTime));
  }

  public static Optional<ParticleCollision> withParticle(final Particle particle1,
      final Particle particle2) {

    final Point2D deltaV = particle2.velocity().subtract(particle1.velocity());
    final Point2D deltaR = particle2.position().subtract(particle1.position());
    final double deltaVxR = deltaV.dotProduct(deltaR);

    if (deltaVxR >= 0) {
      return Optional.empty();
    }

    final double sigma = particle1.radius() + particle2.radius();

    final double d = deltaVxR * deltaVxR - deltaV.dotProduct(deltaV) *
        (deltaR.dotProduct(deltaR) - sigma * sigma);

    if (d < 0) {
      return Optional.empty();
    }

    final double elapsedTime = (-1) * (deltaVxR + Math.sqrt(d)) / deltaV.dotProduct(deltaV);

    final double j =
        (2 * particle1.mass() * particle2.mass() * deltaVxR) / (sigma * (particle1.mass()
            + particle2.mass()));
    final Point2D jxy = deltaR.multiply(j / sigma);

    final List<Particle> particlesAfterCollision = new LinkedList<>();
    particlesAfterCollision.add(ImmutableParticle.builder()
        .from(particle1)
        .position(Points.linearMotion(particle1.position(), particle1.velocity(), elapsedTime))
        .velocity(particle1.velocity().add(jxy.multiply((double) 1 / particle1.mass())))
        .build());
    particlesAfterCollision.add(ImmutableParticle.builder()
        .from(particle2)
        .position(Points.linearMotion(particle2.position(), particle2.velocity(), elapsedTime))
        .velocity(particle2.velocity().subtract(jxy.multiply((double) 1 / particle2.mass())))
        .build());

    return Optional.of(new ParticleCollision(particlesAfterCollision, elapsedTime));
  }

  @Override
  public int compareTo(final ParticleCollision other) {
    return Double.compare(this.elapsedTime, other.elapsedTime);
  }
}
