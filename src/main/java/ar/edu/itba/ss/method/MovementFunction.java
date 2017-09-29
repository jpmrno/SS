package ar.edu.itba.ss.method;

import ar.edu.itba.ss.model.Particle;
import java.util.Set;

public interface MovementFunction {

  Particle move(final Particle currentParticle, final Set<Particle> neighbours, final double dt);
}
