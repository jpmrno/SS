package ar.edu.itba.ss.io.writer;

import ar.edu.itba.ss.model.Road;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MultiWriter implements ParticlesWriter {

  private final Collection<ParticlesWriter> writers;

  public MultiWriter(final Collection<ParticlesWriter> writers) {
    this.writers = writers;
  }

  public MultiWriter(final ParticlesWriter... writers) {
    this.writers = Arrays.stream(writers).collect(Collectors.toList());
  }

  @Override
  public void write(final double time, final List<Road> roads) throws IOException {
    for (final ParticlesWriter writer : writers) {
      writer.write(time, roads);
    }
  }
}
