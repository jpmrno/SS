package ar.edu.itba.ss.io.writer;

import ar.edu.itba.ss.model.Road;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

public class FlowedParticlesWriter implements ParticlesWriter {

  private final List<Integer> flowedParticlesPerTime = new LinkedList<>();
  private int flowedParticles = 0;

  @Override
  public void write(final double time, final List<Road> roads) {
    final Road lastRoad = roads.get(roads.size() - 1);
    flowedParticles += lastRoad.getParticlesFlowed().size();
    flowedParticlesPerTime.add(flowedParticles);
  }

  public void writeToFile(final String fileName) throws IOException {
    final Path filePath = FileSystems.getDefault().getPath(fileName);
    Files.deleteIfExists(filePath);
    final Path file = Files.createFile(filePath);
    try (final BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.APPEND)) {
      for (int i = 0; i < flowedParticlesPerTime.size(); i++) {
        final int flowedParticles =  flowedParticlesPerTime.get(i);
        writer.write(i + "," + flowedParticles + "\n");
      }
    }
  }
}
