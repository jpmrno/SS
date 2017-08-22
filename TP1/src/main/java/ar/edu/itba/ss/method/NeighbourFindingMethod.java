package ar.edu.itba.ss.method;

import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Point2D;

public interface NeighbourFindingMethod {

  Map<Particle, Set<Neighbour>> apply(final Map<Particle, Point2D> particlesPositions,
      final double rc);
}
