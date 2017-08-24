package ar.edu.itba.ss;

import ar.edu.itba.ss.automaton.OffLatticeAutomaton;
import ar.edu.itba.ss.generator.RandomParticleGenerator;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import java.util.List;
import javafx.geometry.Point2D;

public class TP2Main {

  public static void main(final String[] args) {
    final Particle minParticle = ImmutableParticle.builder()
        .id(1)
        .position(Point2D.ZERO)
        .velocity(new Point2D(2, 2))
        .build();
    final Particle maxParticle = ImmutableParticle.builder()
        .id(100)
        .position(new Point2D(100, 100))
        .velocity(new Point2D(2, 2))
        .build();
    final List<Particle> particles = RandomParticleGenerator
        .generateParticles(minParticle, maxParticle);
    final OffLatticeAutomaton automaton =
        new OffLatticeAutomaton(particles, 100, 10, 1, 1000, 0.1, "simulation");

    automaton.run();
  }
}
