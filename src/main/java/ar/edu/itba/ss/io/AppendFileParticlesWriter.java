package ar.edu.itba.ss.io;

import ar.edu.itba.ss.model.Particle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AppendFileParticlesWriter implements ParticlesWriter {

  private final Path filePath;

  public AppendFileParticlesWriter(final String fileName) {
    this.filePath = IterativeFiles.firstNotExists(fileName);

    try {
      Files.createFile(filePath);
    } catch (final IOException exception) {
      throw new IllegalStateException("Can't create file");
    }
  }

  @Override
  public void write(final double time, final List<Particle> particles) throws IOException {
    ParticlesXYZFiles.append(filePath, time, particles);
  }

  @Override
  public void write(double time, Set<Particle> particles) throws IOException {
    ParticlesXYZFiles.append(filePath, time, new LinkedList<>(particles));
  }
}
