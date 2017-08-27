package ar.edu.itba.ss;

import ar.edu.itba.ss.automaton.OffLatticeAutomaton;
import ar.edu.itba.ss.generator.RandomParticleGenerator;
import ar.edu.itba.ss.io.ParticlesWriter;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Points;
import ar.edu.itba.ss.model.Scatter2DChart;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.geometry.Point2D;

public class TP2Main {

  private static final int RUNS = 5;

  public static void main(final String[] args) {
    final int[] ns = new int[]{40, 100};
    final double[] ls = new double[]{3.1, 5};
    Scatter2DChart.initialize("Chart", "eta", 0, 5, 0.1, "va", 0, 1, 0.1);
    final ParticlesWriter writer = (time, particles) -> {};

    for (int i = 0; i < ns.length; i++) {
      final double particlesVelocityMagnitude = 0.03;
      final Particle minParticle = ImmutableParticle.builder()
          .id(1)
          .position(Point2D.ZERO)
          .velocity(Points.magnitudeToPoint2D(particlesVelocityMagnitude))
          .build();
      final Particle maxParticle = ImmutableParticle.builder()
          .id(ns[i])
          .position(new Point2D(ls[i], ls[i]))
          .velocity(Points.magnitudeToPoint2D(particlesVelocityMagnitude))
          .build();
      final List<Particle> randParticles = RandomParticleGenerator
          .generateParticles(minParticle, maxParticle);

      final List<Point2D> points = new LinkedList<>();
      for (double eta = 0; eta < 5; eta += 0.1) {
        final OffLatticeAutomaton automaton =
            new OffLatticeAutomaton(randParticles, ls[i], 1, 1, 10000, eta, writer);
        double averageVelocity = 0;

        for (int run = 0; run < RUNS; run++) {
          final long start = System.nanoTime();
          final List<Particle> lastParticles = automaton.call();
          final long end = System.nanoTime();
//        System.out.println("Took: " + TimeUnit.NANOSECONDS.toMillis(end - start) + "ms");

          final List<Point2D> velocities = lastParticles.stream().map(Particle::velocity)
              .collect(Collectors.toList());
          averageVelocity += Math.abs(Points.normalAverageVeloxity(velocities));
        }

        points.add(new Point2D(eta, averageVelocity / RUNS));
      }

      final String seriesName = String.valueOf(ns[i]);
      Platform.runLater(() -> Scatter2DChart.addSeries(seriesName, points));
    }
  }
}
