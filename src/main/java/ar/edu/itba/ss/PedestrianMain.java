package ar.edu.itba.ss;

import ar.edu.itba.ss.generator.RandomParticleGenerator;
import ar.edu.itba.ss.io.writer.AppendFileParticlesWriter;
import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.criteria.TimeCriteria;
import ar.edu.itba.ss.simulator.PedestrianSimulator;
import ar.edu.itba.ss.simulator.Simulator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.geometry.Point2D;

public class PedestrianMain {

  private static final double BOX_HEIGHT = 20;
  private static final double BOX_WIDTH = 20;
  private static final double GAP = 1.2;
  private static final int N = 200;
  private static final double MASS = 0.01;
  private static final double DT = 0.001;
  private static final int WRITER_ITERATIONS = (int) (1 / DT) / 100;
  private static final double MIN_RADIUS = 0.15;
  private static final double MAX_RADIUS = 0.3;
  private static final double MAX_V = 1.5;
  private static final double BETA = 1;
  private static final double TAO = 0.5;

  public static void main(String[] args) {
    final Particle minPedestrian = ImmutableParticle.builder()
        .id(1)
        .radius(MAX_RADIUS)
        .mass(MASS)
        .velocity(Point2D.ZERO)
        .position(new Point2D(0, 0))
        .build();
    final Particle maxPedestrian = ImmutableParticle.builder()
        .id(N)
        .radius(MAX_RADIUS)
        .mass(MASS)
        .velocity(Point2D.ZERO)
        .position(new Point2D(BOX_WIDTH, BOX_HEIGHT))
        .build();

    final List<Particle> initialPedestriansList =
        RandomParticleGenerator.generateParticles(minPedestrian, maxPedestrian);
    Set<Particle> initialPedestrians = new HashSet<>();
    initialPedestrians.addAll(initialPedestriansList);

    ParticlesWriter writer = new AppendFileParticlesWriter("pedestrian");

    Simulator simulator = new PedestrianSimulator(initialPedestrians, DT, WRITER_ITERATIONS,
        BOX_WIDTH, BOX_HEIGHT, GAP, MAX_RADIUS, MIN_RADIUS, MAX_V, BETA, TAO);

    simulator.simulate(new TimeCriteria(100), writer);
  }

}
