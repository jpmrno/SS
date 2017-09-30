package ar.edu.itba.ss.simulator;

import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.model.Collision;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Points;
import ar.edu.itba.ss.model.criteria.Criteria;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.geometry.Point2D;

public class GasDiffusionSimulator implements Simulator {

  private final double dt;
  private final Point2D[][] wallsVertical;
  private final Point2D[][] wallsHorizontal;
  private Set<Particle> initialParticles;

  public GasDiffusionSimulator(final List<Particle> initialParticles, final double boxWidth,
      final double boxHeight, final double middleGap, final double dt) {
    this.initialParticles = new HashSet<>(initialParticles);
    this.dt = dt;
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
  public Set<Particle> simulate(Criteria endCriteria, ParticlesWriter writer) {
    Set<Particle> currentParticles = initialParticles;
    double time = 0;

    try {
      writer.write(0, currentParticles);
    } catch (final IOException exception) {
      System.err.println("Can't save state at 0");
    }

    Optional<Collision> nextCollision = nextCollision(currentParticles);
    if (!nextCollision.isPresent()) {
      throw new IllegalStateException("No collision found");
    }
    double nextCollisionTime = time + nextCollision.get().getElapsedTime();
    Set<Particle> particlesAfterCollision = currentParticles;

    while (!endCriteria.test(time, currentParticles)) {
      time += dt;

      if (nextCollisionTime <= time) {
        time = nextCollisionTime;

        try {
          writer.write(time, currentParticles, nextCollision.get());
        } catch (final IOException exception) {
          System.err.println("Can't save state at " + time);
        }

        currentParticles = nextParticles(particlesAfterCollision, nextCollision.get());
        particlesAfterCollision = currentParticles;
        nextCollision = nextCollision(currentParticles);
        if (!nextCollision.isPresent()) {
          throw new IllegalStateException("No collision found");
        }
        nextCollisionTime = time + nextCollision.get().getElapsedTime();
      } else {
        currentParticles = currentParticles.stream()
            .map(p -> ImmutableParticle.builder()
                .from(p)
                .position(Points.linearMotion(p.position(), p.velocity(), dt))
                .build())
            .collect(Collectors.toSet());

        try {
          writer.write(time, currentParticles);
        } catch (final IOException exception) {
          System.err.println("Can't save state at " + time);
        }
      }
    }

    initialParticles = currentParticles;
    return currentParticles;
  }

  private Optional<Collision> nextCollision(final Set<Particle> particles) {
    final Set<Collision> collisions = new HashSet<>();

    for (final Particle particle : particles) {
      nextCollisionOfParticle(particle, particles).ifPresent(collisions::add);
    }

    return collisions.stream().min(Comparator.comparingDouble(Collision::getElapsedTime));
  }

  private Optional<Collision> nextCollisionOfParticle(final Particle particle,
      final Set<Particle> neighbours) {

    final Set<Collision> collisions = new HashSet<>();
    for (final Particle neighbour : neighbours) {
      if (!particle.equals(neighbour)) {
        Collision.withParticle(particle, neighbour).ifPresent(collisions::add);
      }
    }

    for (final Point2D[] wall : wallsVertical) {
      Collision.withWall(particle, wall[0], wall[1]).ifPresent(collisions::add);
    }

    for (final Point2D[] wall : wallsHorizontal) {
      Collision.withWall(particle, wall[0], wall[1]).ifPresent(collisions::add);
    }

    return collisions.stream().min(Comparator.comparingDouble(Collision::getElapsedTime));
  }

  private Set<Particle> nextParticles(final Set<Particle> oldParticles,
      final Collision collision) {

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
