package ar.edu.itba.ss.method;

import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Point2D;

public class BruteForceMethod implements NeighbourFindingMethod {

  @Override
  public Map<Particle, Set<Neighbour>> apply(Map<Particle, Point2D> particlesPositions,
      double rc) {
    if (rc <= 0) {
      throw new IllegalArgumentException("Cutoff distance has to be positive.");
    }

    final Map<Particle, Set<Neighbour>> neighboursParticles = new HashMap<>();
    for (final Particle particle : particlesPositions.keySet()) {
      neighboursParticles.put(particle, new HashSet<>());
    }

    for (final Particle particle1 : particlesPositions.keySet()) {
      for (final Particle particle2 : particlesPositions.keySet()) {
        if (!particle1.equals(particle2)) {
          final Point2D point1 = particlesPositions.get(particle1);
          final Point2D point2 = particlesPositions.get(particle2);
          final double distance =
              point1.distance(point2) - particle1.getRadius() - particle2.getRadius();

          if (distance <= rc) {
            neighboursParticles.get(particle1).add(new Neighbour(particle2, distance));
            neighboursParticles.get(particle2).add(new Neighbour(particle1, distance));
          }
        }
      }
    }

    return neighboursParticles;
  }
}
