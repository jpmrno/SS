package ar.edu.itba.ss;

import ar.edu.itba.ss.automaton.OffLatticeAutomaton;
import ar.edu.itba.ss.generator.RandomParticleGenerator;
import ar.edu.itba.ss.io.ParticlesWriter;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Points;
import ar.edu.itba.ss.model.Scatter2DChart;
import javafx.application.Platform;
import javafx.geometry.Point2D;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class VaVsDensityMain {
    private final static int N_MAX = 1600;
    private final static int N_STEP = 100;
    private static final long TOTAL_TIME_STEPS = 500;
    private static final double ETA = 2.5;

    public static void main(final String[] args) {
        final double l = 20;
        Scatter2DChart.initialize("Chart", "density", 0, (double)N_MAX/(l*l), 1, "va", 0, 1, 0.1);
        final ParticlesWriter writer = (time, particles) -> {
        };

        final List<Point2D> points = new LinkedList<>();
        final int iterations = 10;
        for (int n = N_STEP; n < N_MAX; n+=N_STEP) {
            final double particlesVelocityMagnitude = 0.03;
            double[] vec = new double[iterations];
            for (int i = 0; i < iterations; i++) {

                final Particle minParticle = ImmutableParticle.builder()
                        .id(1)
                        .position(Point2D.ZERO)
                        .velocity(Points.magnitudeToPoint2D(particlesVelocityMagnitude))
                        .build();
                final Particle maxParticle = ImmutableParticle.builder()
                        .id(n)
                        .position(new Point2D(l, l))
                        .velocity(Points.magnitudeToPoint2D(particlesVelocityMagnitude))
                        .build();
                final List<Particle> randParticles = RandomParticleGenerator
                        .generateParticles(minParticle, maxParticle);
                final OffLatticeAutomaton automaton =
                        new OffLatticeAutomaton(randParticles, l, 1, 1, TOTAL_TIME_STEPS, ETA, writer);
                final List<Particle> lastParticles = automaton.call();
                final List<Point2D> velocities = lastParticles.stream().map(Particle::velocity)
                        .collect(Collectors.toList());
                vec[i] = Math.abs(Points.normalAverage(velocities));
            }
            points.add(new Point2D((double)n/(l*l), Arrays.stream(vec).average().getAsDouble()
            ));

        }
        Platform.runLater(() -> Scatter2DChart.addSeries("?", points));
    }
}
