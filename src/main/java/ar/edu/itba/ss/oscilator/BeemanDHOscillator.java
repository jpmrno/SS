package ar.edu.itba.ss.oscilator;

import static ar.edu.itba.ss.oscilator.DampedHarmonicOscillators.acceleration;
import static ar.edu.itba.ss.oscilator.DampedHarmonicOscillators.eulerMovementPrediction;

import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import javafx.geometry.Point2D;

public class BeemanDHOscillator extends DampedHarmonicOscillator {

  private double previousA;

  public BeemanDHOscillator(final Particle particle, final double k,
      final double b, final double dt) {

    super(particle, k, b, dt);
    this.previousA = acceleration(eulerMovementPrediction(particle, k, b, -dt), k, b).getX();
  }

  @Override
  protected Particle move(final Particle currentParticle, final double k, final double b,
      final double dt) {

    final double currentX = currentParticle.position().getX();
    final double currentV = currentParticle.position().getX();
    final double currentA = acceleration(currentParticle, k, b).getX();

    final double predictedX =
        currentX + currentV * dt + 2.0 / 3.0 * currentA * dt * dt - 1.0 / 6.0 * previousA * dt * dt;
    final double predictedV = currentV + 3.0 / 2.0 * currentA * dt - 1.0 / 2.0 * previousA * dt;
    final Particle predictedParticle = ImmutableParticle.builder().from(currentParticle)
        .position(new Point2D(predictedX, currentParticle.position().getY()))
        .velocity(new Point2D(predictedV, 0))
        .build();
    final double predictedA = acceleration(predictedParticle, k, b).getX();

    final double correctedV = currentV + 1.0 / 3.0 * predictedA * dt + 5.0 / 6.0 * currentA * dt
        - 1.0 / 6.0 * previousA * dt;

    previousA = currentA;

    return ImmutableParticle.builder().from(predictedParticle)
        .velocity(new Point2D(correctedV, 0))
        .build();
  }
}
