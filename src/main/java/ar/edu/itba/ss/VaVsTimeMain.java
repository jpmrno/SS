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
        final double l = 10;
        final double eta = 0.5;

        Scatter2DChart.initialize("Chart", "time", 0,500,
                0.1,"Va",0,1,0.2);

        Particle minParticle = ImmutableParticle.builder()
                .id(1)
                .position(Point2D.ZERO)
                .velocity(Points.magnitudeToPoint2D(particlesVelocityMagnitude))
                .build();
        Particle maxParticle = ImmutableParticle.builder()
                .id(100)
                .position(new Point2D(l,l))
                .velocity(Points.magnitudeToPoint2D(particlesVelocityMagnitude))
                .build();

        List<Particle> particles = RandomParticleGenerator.generateParticles(minParticle,maxParticle);
        System.out.println("Va: " + Points.normalAverage(particles.stream().map(Particle::velocity).collect(Collectors.toList())));
        final OffLatticeAutomaton automaton =
                new OffLatticeAutomaton(particles,l,1,1,1000,eta,(t, ps) -> {});

        List<Point2D> points = new LinkedList<>();
        for (int i = 1; i <= 1000; i++) {
            points.add(new Point2D(i,Points.normalAverage(particles.stream().map(Particle::velocity).collect(Collectors.toList()))));
            particles = automaton.nextParticles(particles);
        }
        Platform.runLater(() -> Scatter2DChart.addSeries("??", points));


    }
}
