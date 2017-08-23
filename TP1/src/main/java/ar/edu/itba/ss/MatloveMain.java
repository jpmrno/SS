package ar.edu.itba.ss;

import ar.edu.itba.ss.generator.RandomParticleGenerator;
import ar.edu.itba.ss.io.MatlabFileWriter;
import ar.edu.itba.ss.method.BruteForceMethod;
import ar.edu.itba.ss.method.CellIndexMethod;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;

import java.util.*;

import javafx.geometry.Point2D;

public class MatloveMain {

  private static final int SIZE = 1000;

  public static void main(final String[] args) {

    final CellIndexMethod cellIndexMethod = new CellIndexMethod(SIZE, true);
//    final List<Particle> particles = new LinkedList<>();
//    for (double i = 0.5, id = 1; i < SIZE; i++) {
//      for (double j = 0.5; j < SIZE; j++, id++) {
//        particles.add(
//            ImmutableParticle.builder().id((int) id).radius(0.5).position(new Point2D(i, j))
//                .build());
//      }
//    }

    Particle minParticle = ImmutableParticle.builder().id(1).radius(0.1).position(Point2D.ZERO).build();
    Particle maxParticle = ImmutableParticle.builder().id(100).radius(0.1).position(new Point2D(10,10)).build();
    final List<Particle> particles = RandomParticleGenerator.generate(minParticle, maxParticle);

    long time = System.currentTimeMillis();
    final Map<Particle, Set<Neighbour>> neighbours = cellIndexMethod.apply(particles, 0.9);
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
