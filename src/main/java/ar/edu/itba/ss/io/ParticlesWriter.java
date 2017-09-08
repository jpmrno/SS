package ar.edu.itba.ss.io;

import ar.edu.itba.ss.model.Particle;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface ParticlesWriter {

  void write(final double time, final List<Particle> particles) throws IOException;

  void write(final double time, final Set<Particle> particles) throws IOException;
}
