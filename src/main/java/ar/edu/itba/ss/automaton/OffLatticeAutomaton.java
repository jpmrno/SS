package ar.edu.itba.ss.automaton;

import ar.edu.itba.ss.io.ParticlesWriter;
import ar.edu.itba.ss.method.CellIndexMethod;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Points;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import javafx.geometry.Point2D;

public class OffLatticeAutomaton implements Callable<List<Particle>> {

  private final List<Particle> initialParticles;
  private final double rc;
  private final double dt;
  private final double times;
  private final double eta;
  private final double l;
  private final ParticlesWriter writer;
  private final CellIndexMethod neighbourFinder;

  public OffLatticeAutomaton(final List<Particle> initialParticles, final double l, final double rc,
      final double dt, final long times, final double eta, final ParticlesWriter writer) {
    // TODO: validate parameters
    this.initialParticles = Objects.requireNonNull(initialParticles);
    this.rc = rc;
    this.dt = dt;
    this.times = times;
    this.eta = eta;
    this.l = l;
    this.neighbourFinder = new CellIndexMethod(l, true);
    this.writer = Objects.requireNonNull(writer);
  }

  @Override
  public List<Particle> call() {
    List<Particle> currentParticles = initialParticles;

    try {
      writer.write(0, currentParticles);
    } catch (final IOException exception) {
      System.err.println("Can't save state at 0");
    }

    for (int i = 1; i <= times; i++) {
      currentParticles = nextParticles(currentParticles);

      try {
        writer.write(i * dt, currentParticles);
      } catch (final IOException exception) {
        System.err.println("Can't save state at " + (i * dt));
      }
    }

    return currentParticles;
  }

  private List<Particle> nextParticles(final List<Particle> currentParticles) {
    final Map<Particle, Set<Neighbour>> neighbours = neighbourFinder.apply(currentParticles, 0, rc);
    final List<Particle> nextParticles = new LinkedList<>();

    for (final Particle oldParticle : currentParticles) {
      final Particle newParticle = ImmutableParticle.builder()
          .from(oldParticle)
          .position(calculateNewPosition(oldParticle))
          .velocity(calculateNewVelocity(oldParticle, neighbours.get(oldParticle)))
          .build();

      nextParticles.add(newParticle);
    }

    return nextParticles;
  }

  private Point2D calculateNewPosition(final Particle particle) {
    double x = particle.position().getX() + particle.velocity().getX() * dt;
    double y = particle.position().getY() + particle.velocity().getY() * dt;

    while (x < 0 || x > l) {
      x += coordinateCorrection(x, l);
    }

    while (y < 0 || y > l) {
      y += coordinateCorrection(y, l);
    }

    return new Point2D(x, y);
  }

  private Point2D calculateNewVelocity(final Particle particle, final Set<Neighbour> neighbours) {
    final double xSum = particle.velocity().getX() + neighbours.stream()
        .mapToDouble(n -> n.getNeighbourParticle().velocity().getX()).sum();
    final double ySum = particle.velocity().getY() + neighbours.stream()
        .mapToDouble(n -> n.getNeighbourParticle().velocity().getY()).sum();

    final double newAngle = Math.atan2(ySum, xSum);
    final double noise =
        eta == 0 ? 0 : ThreadLocalRandom.current().nextDouble(-eta / 2, eta / 2);

    return Points.polarToPoint2D(particle.velocity().magnitude(), newAngle + noise);
  }

  private static double coordinateCorrection(final double coordinate, final double limit) {
    if (coordinate > limit) {
      return -limit;
    }

    if (coordinate < 0) {
      return limit;
    }

    return 0;
  }
}
