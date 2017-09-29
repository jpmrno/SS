package ar.edu.itba.ss.method.neigbour;

import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Point2D;

public class BruteForceMethod implements NeighbourFindingMethod {

  @Override
  public Map<Particle, Set<Neighbour>> apply(final List<Particle> particles,
      final double rc) {
    if (rc <= 0) {
      throw new IllegalArgumentException("Cutoff distance has to be positive.");
    }

    final Map<Particle, Set<Neighbour>> neighboursParticles = new HashMap<>();
    for (final Particle particle : particles) {
      neighboursParticles.put(particle, new HashSet<>());
    }

    for (final Particle particle1 : particles) {
      for (final Particle particle2 : particles) {
        if (!particle1.equals(particle2)) {
          final Point2D point1 = particle1.position();
          final Point2D point2 = particle2.position();
          final double distance = point1.distance(point2) - particle1.radius() - particle2.radius();

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
