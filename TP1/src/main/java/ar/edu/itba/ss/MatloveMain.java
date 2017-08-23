package ar.edu.itba.ss;

import ar.edu.itba.ss.io.MatlabFileWriter;
import ar.edu.itba.ss.method.CellIndexMethod;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Point2D;

public class MatloveMain {

  public static void main(final String[] args) {

    final CellIndexMethod cellIndexMethod = new CellIndexMethod(20, true);
    final List<Particle> particles = new LinkedList<>();
    for (double i = 0.5, id = 0; i < 10; i++) {
      for (double j = 0.5; j < 10; j++, id++) {
        particles.add(
            ImmutableParticle.builder().id((int) id).radius(0.5).position(new Point2D(i, j))
                .build());
      }
    }

    final Map<Particle, Set<Neighbour>> neighbours = cellIndexMethod.apply(particles, 0.9);
    for (Map.Entry<Particle, Set<Neighbour>> entry : neighbours.entrySet()) {
      System.out.println(entry.getKey());
      for (final Neighbour neighbour : entry.getValue()) {
        System.out.println("\t" + neighbour.getNeighbourParticle());
      }
    }

    final MatlabFileWriter matlabFileWriter = new MatlabFileWriter();
    matlabFileWriter.writeNeighbourParticlesFile(neighbours);
  }
}
