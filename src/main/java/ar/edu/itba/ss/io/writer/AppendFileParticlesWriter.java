package ar.edu.itba.ss.io.writer;

import ar.edu.itba.ss.io.IterativeFiles;
import ar.edu.itba.ss.io.ParticlesXYZFiles;
import ar.edu.itba.ss.model.Road;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
  public void write(final double time, final List<Road> roads) throws IOException {
    ParticlesXYZFiles.append(filePath, time, roads);
  }
}
