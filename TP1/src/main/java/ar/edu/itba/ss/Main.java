package ar.edu.itba.ss;

import ar.edu.itba.ss.io.MatlabFileWriter;
import ar.edu.itba.ss.method.CellIndexMethod;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Point2D;

public class Main {

  public static void main(String[] args) {
    final CellIndexMethod cellIndexMethod = new CellIndexMethod(20, true);
    final Map<Particle, Point2D> positions = new HashMap<>();
    for (double i = 0.5; i < 10; i++) {
      for (double j = 0.5; j < 10; j++) {
        positions.put(new Particle(0.4), new Point2D(i, j));
      }
    }

    final Map<Particle, Set<Neighbour>> neighbours = cellIndexMethod.apply(positions, 0.9);
    for (Map.Entry<Particle, Set<Neighbour>> entry : neighbours.entrySet()) {
      System.out.println(entry.getKey());
      for (final Neighbour neighbour : entry.getValue()) {
        System.out.println("\t" + neighbour.getNeighbourParticle());
      }
    }

    final MatlabFileWriter matlabFileWriter = new MatlabFileWriter();
    matlabFileWriter.writeNeighbourParticlesFile(positions, neighbours);
  }
}
