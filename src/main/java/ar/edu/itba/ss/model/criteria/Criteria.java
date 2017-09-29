package ar.edu.itba.ss.model.criteria;

import ar.edu.itba.ss.model.Particle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface Criteria {

  boolean test(final double time, final Set<Particle> particles);

  default boolean test(final double time, final List<Particle> particles){
    return test(time, new HashSet<>(particles));
  }
}
