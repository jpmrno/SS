package ar.edu.itba.ss.oscilator;

import static ar.edu.itba.ss.oscilator.DampedHarmonicOscillators.acceleration;
import static java.lang.Math.pow;

import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import javafx.geometry.Point2D;

public class GearDHOscillator extends DampedHarmonicOscillator {

  private static final int ORDER = 5;
  private static final double[] alphas = new double[]{
      3.0 / 16.0,
      251.0 / 360.0,
      1.0,
      11.0 / 18.0,
      1.0 / 6.0,
      1.0 / 60.0,
  };
  private static final double[] factorials = new double[]{
      1,
      1,
      2,
      6,
      24,
      120
  };

  private final double r[];
  private final double rp[];

  public GearDHOscillator(final Particle particle, final double k,
      final double b, final double dt) {

    super(particle, k, b, dt);

    r = new double[ORDER + 1];
    rp = new double[ORDER + 1];

    r[0] = particle.position().getX();
    r[1] = particle.velocity().getX();
    for (int i = 2; i < ORDER + 1; i++) {
      r[i] = ((-k * r[i - 2] - b * r[i - 1]) / particle.mass());
    }
  }

  @Override
  protected Particle move(final Particle particle, final double k, final double b,
      final double dt) {

    for (int i = ORDER; i >= 0; i--) {
      rp[i] = r[i];

      for (int j = i + 1, l = 0; j < ORDER + 1; j++, l++) {
        rp[i] += (r[j] * pow(dt, l)) / factorials[l];
      }
    }

    final double deltaR2 = ((acceleration(particle, k, b).getX() - rp[2]) * dt * dt) / 2;

    for (int i = 0; i < ORDER + 1; i++) {
      r[i] = rp[i] + ((alphas[i] * deltaR2 * factorials[i]) / pow(dt, i));
    }

    return ImmutableParticle.builder().from(particle)
        .position(new Point2D(r[0], particle.position().getY()))
        .velocity(new Point2D(r[1], 0))
        .build();
  }
}
