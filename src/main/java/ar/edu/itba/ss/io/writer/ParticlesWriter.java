package ar.edu.itba.ss.io.writer;

import ar.edu.itba.ss.model.Particle;
import java.io.IOException;
import java.util.Collection;

public interface ParticlesWriter {

  void write(final double time, final Collection<Particle> particles) throws IOException;
}
