package ar.edu.itba.ss.oscilator;

import static ar.edu.itba.ss.oscilator.DampedHarmonicOscillators.eulerMovementPrediction;
import static ar.edu.itba.ss.oscilator.DampedHarmonicOscillators.force;

import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import javafx.geometry.Point2D;

public class OriginalVerletDHOscillator extends DampedHarmonicOscillator {

  private Particle previousParticle;

  public OriginalVerletDHOscillator(final Particle particle, final double k,
      final double b, final double dt) {

    super(particle, k, b, dt);
    this.previousParticle = eulerMovementPrediction(particle, k, b, -dt);
  }

  @Override
  protected Particle move(final Particle currentParticle, final double k, final double b,
      final double dt) {

    final double previousX = previousParticle.position().getX();
    final double currentX = currentParticle.position().getX();
    final double currentF = force(currentParticle, k, b).getX();

    final double newX = 2.0 * currentX - previousX + currentF * dt * dt / currentParticle.mass();
    final double newV = (newX - previousX) / (2.0 * dt);

    return ImmutableParticle.builder().from(currentParticle)
        .position(new Point2D(newX, currentParticle.position().getY()))
        .velocity(new Point2D(newV, 0))
        .build();
  }
}
