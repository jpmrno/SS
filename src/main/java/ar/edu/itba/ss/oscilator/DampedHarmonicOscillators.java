package ar.edu.itba.ss.oscilator;

import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import javafx.geometry.Point2D;

public abstract class DampedHarmonicOscillators {

  public static Point2D force(final Particle particle, final double k, final double b) {
    return particle.position().multiply(-k).subtract(particle.velocity().multiply(b));
  }

  public static Point2D acceleration(final Particle particle, final double k, final double b) {
    return force(particle, k, b).multiply(1.0 / particle.mass());
  }

  public static Particle eulerMovementPrediction(final Particle particle, final double k,
      final double b, final double dt) {

    final Point2D force = force(particle, k, b);
    final Point2D newVelocity = particle.velocity()
        .add(force.multiply(dt / particle.mass()));
    final Point2D newPosition = particle.position()
        .add(newVelocity.multiply(dt))
        .add(force.multiply((dt * dt) / (2 * particle.mass())));

    return ImmutableParticle.builder().from(particle)
        .position(newPosition)
        .velocity(newVelocity)
        .build();
  }
}
