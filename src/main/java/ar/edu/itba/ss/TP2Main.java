package ar.edu.itba.ss;

import ar.edu.itba.ss.automaton.OffLatticeAutomaton;
import ar.edu.itba.ss.generator.RandomParticleGenerator;
import ar.edu.itba.ss.io.AppendFileParticlesWriter;
import ar.edu.itba.ss.io.ParticlesWriter;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Points;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javafx.geometry.Point2D;

public class TP2Main {

  public static void main(final String[] args) {
    final double particlesVelocityMagnitude = 0.3;
    final Particle minParticle = ImmutableParticle.builder()
        .id(1)
        .position(Point2D.ZERO)
        .velocity(Points.magnitudeToPoint2D(particlesVelocityMagnitude))
        .build();
    final Particle maxParticle = ImmutableParticle.builder()
        .id(100)
        .position(new Point2D(100, 100))
        .velocity(Points.magnitudeToPoint2D(particlesVelocityMagnitude))
        .build();
    final List<Particle> randParticles = RandomParticleGenerator
        .generateParticles(minParticle, maxParticle);
    final ParticlesWriter writer = new AppendFileParticlesWriter("simulation");
    final OffLatticeAutomaton automaton =
        new OffLatticeAutomaton(randParticles, 100, 5, 1, 1000, 0.1, writer);

    final long start = System.nanoTime();
    automaton.run();
    final long end = System.nanoTime();
    System.out.println("Took: " + TimeUnit.NANOSECONDS.toMillis(end - start) + "ms");
  }
}
