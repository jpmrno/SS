package ar.edu.itba.ss.simulator;

import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.criteria.Criteria;
import java.util.Set;

public interface Simulator {

  Set<Particle> simulate(Criteria endCriteria, ParticlesWriter writer);
}
