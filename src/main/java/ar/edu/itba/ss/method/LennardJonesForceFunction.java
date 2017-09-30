package ar.edu.itba.ss.method;

import static java.lang.Math.pow;

import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import java.util.Set;
import java.util.function.BiFunction;
import javafx.geometry.Point2D;

public class LennardJonesForceFunction implements BiFunction<Particle, Set<Neighbour>, Point2D> {

  private final double epsilon;
  private final double rm;

  public LennardJonesForceFunction(final double epsilon, final double rm) {
    this.epsilon = epsilon;
    this.rm = rm;
  }

  @Override
  public Point2D apply(final Particle particle, final Set<Neighbour> neighbours) {

    double totalForceX = 0;
    double totalForceY = 0;

    for (final Neighbour neighbour : neighbours) {
      final double rX =
          neighbour.getNeighbourParticle().position().getX() - particle.position().getX();
      final double rY =
          neighbour.getNeighbourParticle().position().getY() - particle.position().getY();

      totalForceX += rX != 0 ? force(epsilon, rm, rX) : 0;
      totalForceY += rY != 0 ? force(epsilon, rm, rY) : 0;
    }

    return new Point2D(totalForceX, totalForceY);
  }

  private static double force(final double epsilon, final double rm, final double r) {
    return 12 * epsilon * (pow(rm / r, 13) - pow(rm / r, 7)) / rm;
  }
}
