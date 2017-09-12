package ar.edu.itba.ss;

import ar.edu.itba.ss.generator.RandomParticleGenerator;
import ar.edu.itba.ss.io.writer.PressureWriter;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Points;
import ar.edu.itba.ss.model.StateEquations;
import ar.edu.itba.ss.model.criteria.FractionCriteria;
import ar.edu.itba.ss.simulator.GasDiffusionSimulator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.DoublePredicate;
import java.util.stream.Collectors;
import javafx.geometry.Point2D;

public class TP3Main {

  private static final int N = 100;
  private static final double MASS = 1;
  private static final double RADIUS = 0.0015;
  private static final double INITIAL_VELOCITY_MAGNITUDE = 0.01;
  private static final double DT = 0.1;

  private static final double BOX_HEIGHT = 0.09;
  private static final double BOX_WIDTH = 0.24;
  private static final double BOX_GAP = 0.006;

  private static final double OVITO_PARTICLES_RADIUS = 0;
  private static final double OVITO_PARTICLES_MASS = Double.POSITIVE_INFINITY;

  public static void main(String[] args) {
    
    final Particle minParticle = ImmutableParticle.builder()
        .id(1)
        .position(new Point2D(RADIUS, RADIUS))
        .velocity(Points.magnitudeToPoint2D(INITIAL_VELOCITY_MAGNITUDE))
        .radius(RADIUS)
        .mass(MASS)
        .build();
    final Particle maxParticle = ImmutableParticle.builder()
        .id(N)
        .position(new Point2D(BOX_WIDTH / 2 - RADIUS, BOX_HEIGHT - RADIUS))
        .velocity(Points.magnitudeToPoint2D(INITIAL_VELOCITY_MAGNITUDE))
        .radius(RADIUS)
        .mass(MASS)
        .build();
    final List<Particle> particles = RandomParticleGenerator
        .generateParticles(minParticle, maxParticle);
    addOvitoParticles(particles);
    final GasDiffusionSimulator simulator = new GasDiffusionSimulator(
        particles,
        BOX_WIDTH,
        BOX_HEIGHT,
        BOX_GAP,
        DT);

    final DoublePredicate equilibriumCondition = f -> (0.5 - 0.01) <= f && (0.5 + 0.01) >= f;
    final FractionCriteria equilibriumCriteria = new FractionCriteria(Point2D.ZERO,
        new Point2D(BOX_WIDTH / 2, BOX_HEIGHT), equilibriumCondition);
    final long startTime = System.nanoTime();
    Set<Particle> lastParticles = simulator.call(equilibriumCriteria, (t, p) -> {
    });
    final long endTime = System.nanoTime();
    System.out.println("Elapsed: " + TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + "ms");
    System.out
        .println("How many in first half? " + Points
            .between(lastParticles.stream().filter(p -> p.id() > 0).map(Particle::position).collect(
                Collectors.toList()), Point2D.ZERO, new Point2D(BOX_WIDTH / 2, BOX_HEIGHT)));

    final FractionCriteria nonEquilibriumCriteria = new FractionCriteria(Point2D.ZERO,
        new Point2D(BOX_WIDTH / 2, BOX_HEIGHT), equilibriumCondition.negate());
    final PressureWriter pressureWriter = new PressureWriter();
    final long startTime2 = System.nanoTime();
    lastParticles = simulator.call(nonEquilibriumCriteria, pressureWriter);
    final long endTime2 = System.nanoTime();
    System.out.println("Elapsed: " + TimeUnit.NANOSECONDS.toMillis(endTime2 - startTime2) + "ms");
    System.out
        .println("How many in first half? " + Points
            .between(lastParticles.stream().filter(p -> p.id() > 0).map(Particle::position).collect(
                Collectors.toList()), Point2D.ZERO, new Point2D(BOX_WIDTH / 2, BOX_HEIGHT)));

    double T = StateEquations.temperature(lastParticles);
    double P = pressureWriter.getPressure();
    double V = BOX_HEIGHT * BOX_WIDTH;
    System.out.println(T / (P * V));
  }

  private static void addOvitoParticles(final List<Particle> particles) {
    int i = -1;
    particles.add(ImmutableParticle.builder()
        .position(Point2D.ZERO)
        .velocity(Point2D.ZERO)
        .id(i--)
        .radius(OVITO_PARTICLES_RADIUS)
        .mass(OVITO_PARTICLES_MASS)
        .build());
    particles.add(ImmutableParticle.builder()
        .position(new Point2D(0, BOX_HEIGHT))
        .velocity(Point2D.ZERO)
        .id(i--)
        .radius(OVITO_PARTICLES_RADIUS)
        .mass(OVITO_PARTICLES_MASS)
        .build());
    particles.add(ImmutableParticle.builder()
        .position(new Point2D(BOX_WIDTH, 0))
        .velocity(Point2D.ZERO)
        .id(i--)
        .radius(OVITO_PARTICLES_RADIUS)
        .mass(OVITO_PARTICLES_MASS)
        .build());
    particles.add(ImmutableParticle.builder()
        .position(new Point2D(BOX_WIDTH, BOX_HEIGHT))
        .velocity(Point2D.ZERO)
        .id(i--)
        .radius(OVITO_PARTICLES_RADIUS)
        .mass(OVITO_PARTICLES_MASS)
        .build());
    particles.add(ImmutableParticle.builder()
        .position(new Point2D(BOX_WIDTH / 2, 0))
        .velocity(Point2D.ZERO)
        .id(i--)
        .radius(OVITO_PARTICLES_RADIUS)
        .mass(OVITO_PARTICLES_MASS)
        .build());
    particles.add(ImmutableParticle.builder()
        .position(new Point2D(BOX_WIDTH / 2, BOX_HEIGHT / 2 - BOX_GAP / 2))
        .velocity(Point2D.ZERO)
        .id(i--)
        .radius(OVITO_PARTICLES_RADIUS)
        .mass(OVITO_PARTICLES_MASS)
        .build());
    particles.add(ImmutableParticle.builder()
        .position(new Point2D(BOX_WIDTH / 2, BOX_HEIGHT / 2 + BOX_GAP / 2))
        .velocity(Point2D.ZERO)
        .id(i--)
        .radius(OVITO_PARTICLES_RADIUS)
        .mass(OVITO_PARTICLES_MASS)
        .build());
    particles.add(ImmutableParticle.builder()
        .position(new Point2D(BOX_WIDTH / 2, BOX_HEIGHT))
        .velocity(Point2D.ZERO)
        .id(i--)
        .radius(OVITO_PARTICLES_RADIUS)
        .mass(OVITO_PARTICLES_MASS)
        .build());
  }
}
