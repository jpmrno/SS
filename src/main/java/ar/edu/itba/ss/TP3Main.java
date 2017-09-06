package ar.edu.itba.ss;

import ar.edu.itba.ss.generator.RandomParticleGenerator;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Points;
import ar.edu.itba.ss.simulator.GasDiffusionSimulator;
import javafx.geometry.Point2D;

public class TP3Main {

    public static void main(String[] args) {
        Particle minParticle = ImmutableParticle.builder()
                .id(0)
                .position(Point2D.ZERO)
                .velocity(Point2D.ZERO)
                .radius(0.0015)
                .build();
        Particle maxParticle = ImmutableParticle.builder()
                .id(200)
                .position(new Point2D(0.24,0.09))
                .velocity(Points.magnitudeToPoint2D(0.01))
                .radius(0.0015)
                .build();
        GasDiffusionSimulator simulator = new GasDiffusionSimulator(RandomParticleGenerator.generateParticles(minParticle,maxParticle),
                0.09,0.24);
        simulator.simulate();
    }
}
