package ar.edu.itba.ss.method;

import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Point2D;

public interface NeighbourFindingMethod {

  Map<Particle, Set<Neighbour>> apply(final Map<Particle, Point2D> particlesPositions,
      final double rc);

  static double distance(final Particle particle1, final Point2D point1, final Particle particle2,
      final Point2D point2) {

    final double distance = point1.distance(point2) - particle1.getRadius() - particle2.getRadius();

    return distance < 0 ? 0 : distance;
  }
}
