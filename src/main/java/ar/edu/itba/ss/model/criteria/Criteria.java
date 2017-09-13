package ar.edu.itba.ss.model.criteria;

import ar.edu.itba.ss.model.Particle;
import java.util.Set;

public interface Criteria {

  boolean test(final double time, final Set<Particle> particles);
}
