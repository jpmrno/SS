package ar.edu.itba.ss.method.neigbour;

import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NeighbourFindingMethod {

  Map<Particle, Set<Neighbour>> apply(final Collection<Particle> particles, final double rc);
}
