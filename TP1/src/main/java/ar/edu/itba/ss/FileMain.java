package ar.edu.itba.ss;

import ar.edu.itba.ss.io.MatlabFileWriter;
import ar.edu.itba.ss.io.SSParticlesFileReader;
import ar.edu.itba.ss.method.BruteForceMethod;
import ar.edu.itba.ss.method.CellIndexMethod;
import ar.edu.itba.ss.method.NeighbourFindingMethod;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileMain {

  public static void main(final String[] args) {
    List<Particle> particles = null;
    try {
      particles = SSParticlesFileReader.read(
          FileSystems.getDefault().getPath("example.txt"));
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    final NeighbourFindingMethod method = new CellIndexMethod(100, true);

    long time = System.currentTimeMillis();
    final Map<Particle, Set<Neighbour>> neighbours = method.apply(particles, 1);
    time = System.currentTimeMillis() - time;

    for (Map.Entry<Particle, Set<Neighbour>> entry : neighbours.entrySet()) {
      System.out.println(entry.getKey());
      for (final Neighbour neighbour : entry.getValue()) {
        System.out.println("\t" + neighbour.getNeighbourParticle());
      }
    }

    System.out.println("Time:   " + time);
    final MatlabFileWriter matlabFileWriter = new MatlabFileWriter();
    matlabFileWriter.writeNeighbourParticlesFile(neighbours);

    time = System.currentTimeMillis();
    final BruteForceMethod bruteForceMethod = new BruteForceMethod();
    final Map<Particle, Set<Neighbour>> neighbours2 = bruteForceMethod.apply(particles, 0.9);
    time = System.currentTimeMillis() - time;
    System.out.println("Time2:   " + time);

    if (neighbours.equals(neighbours2)) {
      System.out.println("OK");
    }

  }
}
