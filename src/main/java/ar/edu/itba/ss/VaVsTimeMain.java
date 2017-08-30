package ar.edu.itba.ss;

import ar.edu.itba.ss.automaton.OffLatticeAutomaton;
import ar.edu.itba.ss.generator.RandomParticleGenerator;
import ar.edu.itba.ss.model.*;
import javafx.application.Platform;
import javafx.geometry.Point2D;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VaVsTimeMain {

    public static void main(String[] args) {
        final double particlesVelocityMagnitude = 0.03;
        final double l = 5;
        final double[] etas = new double[]{3,4};
        final int particleAmount = 100;

        Scatter2DChart.initialize("Chart", "time", 0,500,
                100,"Va",0,1,0.2);

        Particle minParticle = ImmutableParticle.builder()
                .id(1)
                .position(Point2D.ZERO)
                .velocity(Points.magnitudeToPoint2D(particlesVelocityMagnitude))
                .build();
        Particle maxParticle = ImmutableParticle.builder()
                .id(particleAmount)
                .position(new Point2D(l,l))
                .velocity(Points.magnitudeToPoint2D(particlesVelocityMagnitude))
                .build();

        for(double eta : etas){
            List<Particle> particles = RandomParticleGenerator.generateParticles(minParticle,maxParticle);
            final OffLatticeAutomaton automaton =
                    new OffLatticeAutomaton(particles,l,1,1,1000,eta,(t, ps) -> {});

            List<Point2D> points = new LinkedList<>();
            for (int i = 1; i <= 1000; i++) {
                points.add(new Point2D(i,Points.normalAverage(particles.stream().map(Particle::velocity).collect(Collectors.toList()))));
                particles = automaton.nextParticles(particles);
            }
            Platform.runLater(() -> Scatter2DChart.addSeries(Double.toString(eta), points));
        }



    }
}
