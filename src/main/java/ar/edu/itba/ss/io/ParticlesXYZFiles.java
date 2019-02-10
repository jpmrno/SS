package ar.edu.itba.ss.io;

import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Road;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ParticlesXYZFiles {

  private static final double CELL_SIZE = 10;

  public static void append(final Path path, final double time, final List<Road> roads) throws IOException {
    try (final BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
      final Optional<Integer> size = roads.stream().map(Road::particles).map(Set::size).reduce((s1, s2) -> s1 + s2);
      writer.write(size.orElseThrow(IllegalStateException::new) + "\n");
      writer.write(time + "\n");

      int prevRoadLanesLength = 0;
      for (final Road road : roads) {
        final Set<Particle> roadParticles = road.particles();
        for (final Particle particle : roadParticles) {
          writer.write(
              particle.id() + "  "
                  + particle.length() * CELL_SIZE + "  "
                  + particle.length() * CELL_SIZE + "  "
                  + (prevRoadLanesLength + particle.col()) * CELL_SIZE + "  "
                  + particle.row() * CELL_SIZE * (-1) + "  "
                  + particle.velocity() + "\n");
        }
        prevRoadLanesLength += road.laneLength();
      }
    }
  }
}
