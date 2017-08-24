package ar.edu.itba.ss;

import ar.edu.itba.ss.automata.OffLatticeAutomaton;
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
        .velocity(new Point2D(4, 4))
        .build();
    final Particle maxParticle = ImmutableParticle.builder()
        .id(10)
        .position(new Point2D(10, 10))
        .velocity(new Point2D(4, 4))
        .build();
    final List<Particle> particles = RandomParticleGenerator
        .generateParticles(minParticle, maxParticle);
    final OffLatticeAutomaton automaton =
        new OffLatticeAutomaton(particles, 0.1, 1, 100, 0.1, 100);

    automaton.run();
  }
}
