package ar.edu.itba.ss;

import ar.edu.itba.ss.io.MatlabFileWriter;
import ar.edu.itba.ss.method.CellIndexMethod;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javafx.geometry.Point2D;

public class Main {

  public static void main(String[] args) {
    CellIndexMethod cellIndexMethod = new CellIndexMethod(5, 50, true);
    final Map<Particle, Point2D> positions = new HashMap<>();
    Random r = new Random();
    for (int i = 0; i < 25; i++) {
      for (int j = 0; j < 25; j++) {
        positions.put(new Particle(0.2), new Point2D(i * 2, j * 2));
      }
    }

    Map<Particle, Set<Neighbour>> neighbours = cellIndexMethod.apply(positions, 2);
    for (Map.Entry<Particle, Set<Neighbour>> entry : neighbours.entrySet()) {
      System.out.println(entry.getKey());
      for (Neighbour neighbour : entry.getValue()) {
        System.out.println("\t" + neighbour.getNeighbourParticle());
      }
    }

    MatlabFileWriter matlabFileWriter = new MatlabFileWriter();
    matlabFileWriter.writeNeighbourParticlesFile(positions, neighbours);

  }
}
