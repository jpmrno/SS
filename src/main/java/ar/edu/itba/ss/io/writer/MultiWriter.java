package ar.edu.itba.ss.io.writer;

import ar.edu.itba.ss.model.Particle;
import java.io.IOException;
import java.util.Collection;

public class MultiWriter implements ParticlesWriter {

  private final Collection<ParticlesWriter> writers;

  public MultiWriter(final Collection<ParticlesWriter> writers) {
    this.writers = writers;
  }

  @Override
  public void write(final double time, final Collection<Particle> particles) throws IOException {
    for (final ParticlesWriter writer : writers) {
      writer.write(time, particles);
    }
  }
}
