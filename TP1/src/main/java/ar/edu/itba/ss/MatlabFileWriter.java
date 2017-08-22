package ar.edu.itba.ss;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.geometry.Point2D;

public class MatlabFileWriter {

  public void writeNeighbourParticlesFile(Map<Particle, Point2D> positions,
      Map<Particle, Set<Neighbour>> neighbours) {
    writeNeighbourParticlesFile("./default", positions, neighbours);
  }

  public void writeNeighbourParticlesFile(String filename, Map<Particle, Point2D> positions,
      Map<Particle, Set<Neighbour>> neighbours) {
    positions = positions.entrySet().stream()
        .sorted((o1, o2) -> Integer.valueOf(o1.getKey().getId()).compareTo(o2.getKey().getId()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
            (oldValue, newValue) -> oldValue, LinkedHashMap::new));

    neighbours = neighbours.entrySet().stream()
        .sorted(Comparator.comparingInt(o -> o.getKey().getId()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
            (oldValue, newValue) -> oldValue, LinkedHashMap::new));

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename + ".m"))) {
      String positionsStr = "positions = [";
      for (Point2D position : positions.values()) {
        positionsStr += position.getX() + " " + position.getY() + "; ";
      }
      positionsStr += "];\n";

      String neighboursStr = "neighbours = {";
      for (Set<Neighbour> neighbourList : neighbours.values()) {
        neighboursStr += "[";
        for (Neighbour neighbour : neighbourList) {
          neighboursStr += neighbour.getNeighbourParticle().getId() + 1 + ", ";
        }
        neighboursStr += "], ";
      }
      neighboursStr += "};\n";

      bw.write(positionsStr + neighboursStr);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
