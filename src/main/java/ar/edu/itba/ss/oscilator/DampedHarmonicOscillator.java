package ar.edu.itba.ss.oscilator;

import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.sqrt;
import static java.util.Objects.requireNonNull;

import ar.edu.itba.ss.method.MovementFunction;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import java.util.Set;
import java.util.function.BiFunction;
import javafx.geometry.Point2D;

public class DampedHarmonicOscillator {

  private final double b;
  private final double k;
  private final double dt;
  private final MovementFunction movementFunction;

  private Particle particle;

  public DampedHarmonicOscillator(final Particle particle, final double k, final double b,
      final double dt, final MovementFunction movementFunction) {

    if (requireNonNull(particle).velocity().getY() != 0) {
      throw new IllegalArgumentException("Movement has to be un 1D");
    }

    this.particle = particle;
    this.k = k;
    this.b = b;
    this.dt = dt;
    this.movementFunction = movementFunction;
  }

  public Particle move(final Particle particle, final double time) {

    final double gamma = b / (2 * particle.mass());
    final double wa = sqrt(k / particle.mass() - gamma * gamma);

    final Point2D newPosition = new Point2D(
        particle.position().getX() + exp(-gamma * time) * cos(wa * time),
        particle.position().getY());

    return ImmutableParticle.builder().from(particle)
        .position(newPosition)
//        .velocity()
        .build();
  }

  public static class UnderdampedOscillatorForceFunction implements
      BiFunction<Particle, Set<Neighbour>, Point2D> {

    private final double k;
    private final double b;

    public UnderdampedOscillatorForceFunction(final double k, final double b) {
      this.k = k;
      this.b = b;
    }

    @Override
    public Point2D apply(final Particle particle, final Set<Neighbour> neighbours) {
      return apply(particle, k, b);
    }

    public static Point2D apply(final Particle particle, final double k, final double b) {
      return particle.position()
          .multiply(-k)
          .subtract(particle.velocity()
              .multiply(b));
    }
  }
}
