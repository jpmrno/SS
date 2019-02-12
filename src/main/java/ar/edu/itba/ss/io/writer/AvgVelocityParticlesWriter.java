package ar.edu.itba.ss.io.writer;

import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Road;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

public class AvgVelocityParticlesWriter implements ParticlesWriter {

  private final List<Double> avgVelocities = new LinkedList<>();

  @Override
  public void write(final double time, final List<Road> roads) {
    final double avgVelocity =
        roads.get(0).particles().stream().mapToDouble(Particle::velocity).average().orElseThrow(IllegalStateException::new);
    avgVelocities.add(avgVelocity);
  }

  public void writeToFile(final String fileName) throws IOException {
    final Path filePath = FileSystems.getDefault().getPath(fileName);
    Files.deleteIfExists(filePath);
    final Path file = Files.createFile(filePath);
    try (final BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.APPEND)) {
      for (int i = 0; i < avgVelocities.size(); i++) {
        final double avgVelocity =  avgVelocities.get(i);
        writer.write(i + "," + avgVelocity + "\n");
      }
    }
  }
}
