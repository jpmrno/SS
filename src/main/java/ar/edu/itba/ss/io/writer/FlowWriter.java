package ar.edu.itba.ss.io.writer;

import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Road;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class FlowWriter implements ParticlesWriter {

  private final int dN;
  private final Deque<Double> lastFlowsTimes;
  private final Map<Integer, Double> flows;

  public FlowWriter(final int dN) {
    this.dN = dN;
    this.lastFlowsTimes = new ArrayDeque<>(dN);
    this.flows = new HashMap<>();
  }

  @Override
  public void write(final double time, final List<Road> roads) throws IOException {
    final Road lastRoad = roads.get(roads.size() - 1);
    final List<Particle> particlesFlowed = lastRoad.getParticlesFlowed();

    for (final Particle ignored : particlesFlowed) {
      lastFlowsTimes.addLast(time);
      if (lastFlowsTimes.size() == dN) {
        flows.put((int) time, dN / (lastFlowsTimes.getLast() - lastFlowsTimes.getFirst()));
        lastFlowsTimes.removeFirst();
      }
    }
  }

  public void writeToFile(final String fileName) throws IOException {
    final Path filePath = FileSystems.getDefault().getPath(fileName);
    Files.deleteIfExists(filePath);
    final Path file = Files.createFile(filePath);
    try (final BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.APPEND)) {
      for (final Entry<Integer, Double> flows : flows.entrySet()) {
        writer.write(flows.getKey() + ", " + flows.getValue() + "\n");
      }
    }
  }
}
