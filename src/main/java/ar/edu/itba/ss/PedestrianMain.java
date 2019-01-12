package ar.edu.itba.ss;

import ar.edu.itba.ss.generator.RandomParticleGenerator;
import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.io.writer.RightGapBoxParticleWriter;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.criteria.Criteria;
import ar.edu.itba.ss.simulator.PedestrianSimulator;
import ar.edu.itba.ss.simulator.Simulator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.geometry.Point2D;

public class PedestrianMain {

  private static final double BOX_HEIGHT = 20;
  private static final double BOX_WIDTH = 20;
  private static final double BOX_GAP = 1.2;
  private static final int N = 200;
  private static final double MASS = 0.01;
  private static final double DT = 0.05;
  private static final int WRITER_ITERATIONS = (int) (1 / DT) / 10;
  private static final double MIN_RADIUS = 0.15;
  private static final double MAX_RADIUS = 0.32;
  private static final double MAX_V = 1.55;
  private static final double BETA = 0.9;
  private static final double TAO = 0.5;

  public static void main(String[] args) {
    final Particle minPedestrian = ImmutableParticle.builder()
        .id(1)
        .radius(MAX_RADIUS)
        .mass(MASS)
        .velocity(Point2D.ZERO) // TODO: Preguntar velocidad inicial
        .position(new Point2D(MAX_RADIUS, MAX_RADIUS))
        .build();
    final Particle maxPedestrian = ImmutableParticle.builder()
        .id(N)
        .radius(MAX_RADIUS)
        .mass(MASS)
        .velocity(Point2D.ZERO) // TODO: Preguntar velocidad inicial
        .position(new Point2D(BOX_WIDTH - MAX_RADIUS, BOX_HEIGHT - MAX_RADIUS))
        .build();
    final List<Particle> initialPedestriansList =
        RandomParticleGenerator.generateParticles(minPedestrian, maxPedestrian);
    final Set<Particle> initialPedestrians = new HashSet<>(initialPedestriansList);

    final ParticlesWriter writer = new RightGapBoxParticleWriter("simulation_p", Point2D.ZERO,
        new Point2D(BOX_WIDTH, BOX_HEIGHT), BOX_GAP);
    final Criteria endCriteria = (t, ps) -> ps.size() == 0;
    final Simulator simulator = new PedestrianSimulator(initialPedestrians, DT, WRITER_ITERATIONS,
        BOX_WIDTH, BOX_HEIGHT, BOX_GAP, MAX_RADIUS, MIN_RADIUS, MAX_V, BETA, TAO);

    simulator.simulate(endCriteria, writer);
  }
}
