package ar.edu.itba.ss.simulator;

import ar.edu.itba.ss.io.ParticlesWriter;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.ParticleCollision;
import ar.edu.itba.ss.model.Points;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import javafx.geometry.Point2D;

public class GasDiffusionSimulator2 implements Callable<Set<Particle>> {

  private final Set<Particle> initialParticles;
  private final double boxWidth;
  private final double boxHeight;
  private final double middleGap;
  private final long collisions;
  private final ParticlesWriter writer;

  private final Point2D[][] wallsVertical;
  private final Point2D[][] wallsHorizontal;

  public GasDiffusionSimulator2(final List<Particle> initialParticles, final double boxWidth,
      final double boxHeight, final double middleGap, final long collisions,
      final ParticlesWriter writer) {
    this.initialParticles = new HashSet<>(initialParticles);
    this.boxWidth = boxWidth;
    this.boxHeight = boxHeight;
    this.middleGap = middleGap;
    this.writer = writer;
    this.collisions = collisions;
    this.wallsVertical = new Point2D[][]{
        {Point2D.ZERO, new Point2D(0, boxHeight)},
        {new Point2D(boxWidth / 2, 0), new Point2D(boxWidth / 2, boxHeight / 2 - middleGap / 2)},
        {new Point2D(boxWidth / 2, boxHeight / 2 + middleGap / 2),
            new Point2D(boxWidth / 2, boxHeight)},
        {new Point2D(boxWidth, 0), new Point2D(boxWidth, boxHeight)}
    };
    this.wallsHorizontal = new Point2D[][]{
        {Point2D.ZERO, new Point2D(boxWidth, 0)},
        {new Point2D(0, boxHeight), new Point2D(boxWidth, boxHeight)},
    };
  }

  @Override
  public Set<Particle> call() {
    Set<Particle> currentParticles = initialParticles;

    try {
      writer.write(0, currentParticles);
    } catch (final IOException exception) {
      System.err.println("Can't save state after 0 collisions");
    }

    for (int i = 0; i < collisions; i++) {
      final Optional<ParticleCollision> collision = nextCollision(currentParticles);
      if (!collision.isPresent()) {
        System.out.println("No more collisions");
        return currentParticles;
      }

      currentParticles = nextParticles(currentParticles, collision.get());

      try {
        writer.write(i, currentParticles);
      } catch (final IOException exception) {
        System.err.println("Can't save state after " + i + " collisions");
      }
    }

    return currentParticles;
  }

  private Optional<ParticleCollision> nextCollision(final Set<Particle> particles) {
    final Set<ParticleCollision> collisions = new HashSet<>();

    for (final Particle particle : particles) {
      nextCollisionOfParticle(particle, particles).ifPresent(collisions::add);
    }

    return collisions.stream().min(Comparator.comparingDouble(ParticleCollision::getElapsedTime));
  }

  private Optional<ParticleCollision> nextCollisionOfParticle(final Particle particle,
      final Set<Particle> neighbours) {

    final Set<ParticleCollision> collisions = new HashSet<>();
    for (final Particle neighbour : neighbours) {
      if (!particle.equals(neighbour)) {
        ParticleCollision.withParticle(particle, neighbour).ifPresent(collisions::add);
      }
    }

    for (final Point2D[] wall : wallsVertical) {
      ParticleCollision.withWall(particle, wall[0], wall[1]).ifPresent(collisions::add);
    }

    for (final Point2D[] wall : wallsHorizontal) {
      ParticleCollision.withWall(particle, wall[0], wall[1]).ifPresent(collisions::add);
    }

    return collisions.stream().min(Comparator.comparingDouble(ParticleCollision::getElapsedTime));
  }

  private Set<Particle> nextParticles(final Set<Particle> oldParticles,
      final ParticleCollision collision) {

    final Set<Particle> newParticles = oldParticles.stream()
        .map(p -> ImmutableParticle.builder()
            .from(p)
            .position(Points.linearMotion(p.position(), p.velocity(), collision.getElapsedTime()))
            .build())
        .collect(Collectors.toSet());
    newParticles.removeAll(collision.getParticlesAfterCollision());
    newParticles.addAll(collision.getParticlesAfterCollision());

    return newParticles;
  }
}
