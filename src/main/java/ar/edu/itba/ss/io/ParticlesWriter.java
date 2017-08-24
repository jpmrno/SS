package ar.edu.itba.ss.io;

import ar.edu.itba.ss.model.Particle;
import java.io.IOException;
import java.util.List;

public interface ParticlesWriter {

  void write(final double time, final List<Particle> particles) throws IOException;
}
