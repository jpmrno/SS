package ar.edu.itba.ss.io.writer;

import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.ParticleWrapper;
import ar.edu.itba.ss.util.Either;
import java.io.IOException;
import java.util.Collection;

public interface ParticlesWriter {

  void write(final long time, final Either<Particle, ParticleWrapper>[][] roads, final Collection<Particle> particles)
      throws IOException;
}
