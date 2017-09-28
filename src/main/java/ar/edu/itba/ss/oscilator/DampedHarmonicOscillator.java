package ar.edu.itba.ss.oscilator;

import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.sqrt;
import static java.util.Objects.requireNonNull;

import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import javafx.geometry.Point2D;

public class DampedHarmonicOscillator {

  private final double b;
  private final double k;
  private final double dt;
  private Particle particle;

  public DampedHarmonicOscillator(final Particle particle, final double k, final double b,
      final double dt) {

    if (requireNonNull(particle).velocity().getY() != 0) {
      throw new IllegalArgumentException("Movement has to be un 1D");
    }

    this.particle = particle;
    this.k = k;
    this.b = b;
    this.dt = dt;
  }

  public void forwardTime(final long times) {
    for (int i = 0; i < times; i++) {
      particle = move(particle, k, b, dt);
    }
  }

  protected Particle move(final Particle particle, final double k, final double b,
      final double dt) {

    final double gamma = b / (2 * particle.mass());
    final double wa = sqrt(k / particle.mass() - gamma * gamma);

    final Point2D newPosition = new Point2D(
        particle.position().getX() + exp(-gamma * dt) * cos(wa * dt),
        particle.position().getY());

    return ImmutableParticle.builder().from(particle)
        .position(newPosition)
//        .velocity()
        .build();
  }
}
