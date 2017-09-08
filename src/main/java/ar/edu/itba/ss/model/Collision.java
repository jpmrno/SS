package ar.edu.itba.ss.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Point2D;

public class Collision implements Comparable<Collision> {

  private List<Particle> particlesAfterCollision;
  private double elapsedTime;

  private Collision(final List<Particle> particlesAfterCollision,
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

  public static Optional<Collision> withWall(final Particle particle, final Point2D start,
      final Point2D end) {

    if (particle.velocity().equals(Point2D.ZERO)) {
      return Optional.empty();
    }

    double destination, position, velocity;
    Point2D newVelocity;
    boolean vertical;
    if (start.getY() == end.getY() && start.getX() != end.getX()) {
      // Horizontal
      vertical = false;
      position = particle.position().getY();
      velocity = particle.velocity().getY();
      destination = start.getY();
      newVelocity = new Point2D(particle.velocity().getX(), -1 * particle.velocity().getY());
    } else if (start.getX() == end.getX() && start.getY() != end.getY()) {
      // Vertical
      vertical = true;
      position = particle.position().getX();
      velocity = particle.velocity().getX();
      destination = start.getX();
      newVelocity = new Point2D(-1 * particle.velocity().getX(), particle.velocity().getY());
    } else {
      throw new IllegalArgumentException("Unsupported wall");
    }

    final double elapsedTime =
        (destination + (velocity > 0 ? -1 : 1) * particle.radius() - position) / velocity;
    if (elapsedTime < 0) {
      return Optional.empty();
    }

    final Point2D newPosition = Points
        .linearMotion(particle.position(), particle.velocity(), elapsedTime);

    if (vertical) {
      if (newPosition.getY() < start.getY() || newPosition.getY() > end.getY()) {
        return Optional.empty();
      }
    } else {
      if (newPosition.getX() < start.getX() || newPosition.getX() > end.getX()) {
        return Optional.empty();
      }
    }

    final List<Particle> particlesAfterCollision = new LinkedList<>();
    particlesAfterCollision.add(ImmutableParticle.builder()
        .from(particle)
        .position(newPosition)
        .velocity(newVelocity)
        .build());

    return Optional.of(new Collision(particlesAfterCollision, elapsedTime));
  }

  public static Optional<Collision> withParticle(final Particle particle1,
      final Particle particle2) {

    final double elapsedTime = collisionTime(particle1, particle2);
    if (elapsedTime == Double.POSITIVE_INFINITY) {
      return Optional.empty();
    }

    return Optional
        .of(new Collision(collide(particle1, particle2, elapsedTime), elapsedTime));
  }

  private static double collisionTime(final Particle particle1, final Particle particle2) {
    final Point2D deltaV = particle2.velocity().subtract(particle1.velocity());
    final Point2D deltaR = particle2.position().subtract(particle1.position());
    final double deltaVxR = deltaV.dotProduct(deltaR);

    if (deltaVxR >= 0) {
      return Double.POSITIVE_INFINITY;
    }

    final double sigma = particle1.radius() + particle2.radius();
    final double d = deltaVxR * deltaVxR - deltaV.dotProduct(deltaV) *
        (deltaR.dotProduct(deltaR) - sigma * sigma);

    if (d < 0) {
      return Double.POSITIVE_INFINITY;
    }

    return (-1) * (deltaVxR + Math.sqrt(d)) / deltaV.dotProduct(deltaV);
  }

  private static final List<Particle> collide(final Particle particle1, final Particle particle2,
      final double time) {

    final Point2D newPositionP1 = Points
        .linearMotion(particle1.position(), particle1.velocity(), time);
    final Point2D newPositionP2 = Points
        .linearMotion(particle2.position(), particle2.velocity(), time);

    final Point2D deltaV = particle2.velocity().subtract(particle1.velocity());
    final Point2D deltaR = newPositionP2.subtract(newPositionP1);
    final double deltaVxR = deltaV.dotProduct(deltaR);
    final double sigma = particle1.radius() + particle2.radius();

    final double j =
        (2 * particle1.mass() * particle2.mass() * deltaVxR) / (sigma * (particle1.mass()
            + particle2.mass()));
    final Point2D jVector = deltaR.multiply(j / sigma);

    final List<Particle> particlesAfterCollision = new LinkedList<>();
    particlesAfterCollision.add(ImmutableParticle.builder()
        .from(particle1)
        .position(newPositionP1)
        .velocity(particle1.velocity().add(jVector.multiply((double) 1 / particle1.mass())))
        .build());
    particlesAfterCollision.add(ImmutableParticle.builder()
        .from(particle2)
        .position(newPositionP2)
        .velocity(particle2.velocity().subtract(jVector.multiply((double) 1 / particle2.mass())))
        .build());

    return particlesAfterCollision;
  }

  @Override
  public int compareTo(final Collision other) {
    return Double.compare(this.elapsedTime, other.elapsedTime);
  }
}
