package ar.edu.itba.ss.oscilator;

import ar.edu.itba.ss.model.Particle;
import javafx.geometry.Point2D;

public abstract class DampedHarmonicOscillators {

  public static Point2D force(final Particle particle, final double k, final double b) {
    return particle.position()
        .multiply(-k)
        .subtract(particle.velocity()
            .multiply(b));
  }

  public static Point2D acceleration(final Particle particle, final double k, final double b) {
    return force(particle, k, b)
        .multiply(1.0 / particle.mass());
  }
}
