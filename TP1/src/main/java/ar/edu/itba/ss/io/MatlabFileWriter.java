package ar.edu.itba.ss.io;

import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
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

  public void writeNeighbourParticlesFile(Map<Particle, Set<Neighbour>> neighbours) {
    writeNeighbourParticlesFile("./default", neighbours);
  }

  public void writeNeighbourParticlesFile(String filename,
      Map<Particle, Set<Neighbour>> neighbours) {

    neighbours = neighbours.entrySet().stream()
        .sorted(Comparator.comparingInt(o -> o.getKey().id()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
            (oldValue, newValue) -> oldValue, LinkedHashMap::new));

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename + ".m"))) {
      String positionsStr = "positions = [";
      for (Point2D position : neighbours.keySet().stream().map(Particle::position)
          .collect(Collectors.toList())) {
        positionsStr += position.getX() + " " + position.getY() + "; ";
      }
      positionsStr += "];\n";

      String neighboursStr = "neighbours = {";
      for (Set<Neighbour> neighbourList : neighbours.values()) {
        neighboursStr += "[";
        for (Neighbour neighbour : neighbourList) {
          neighboursStr += neighbour.getNeighbourParticle().id() + 1 + ", ";
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
