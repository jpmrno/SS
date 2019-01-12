package ar.edu.itba.ss.simulator;

import static java.lang.Math.pow;

import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.method.neigbour.CellIndexMethod;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Points;
import ar.edu.itba.ss.model.criteria.Criteria;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.geometry.Point2D;

@SuppressWarnings("Duplicates")
public class PedestrianSimulator implements Simulator {

  private final Set<Particle> initialPedestrians;
  private final double dt;
  private final int writerIteration;
  private final double boxWidth;
  private final double boxHeight;
  private final double gap;
  private final CellIndexMethod cim;
  private final double maxRadius;
  private final double minRadius;
  private final double vdMax;
  private final double beta;
  private final double tao;
  private final Map<Particle, Double> exitTimes;
  private final Point2D finalDestination;

  public PedestrianSimulator(Set<Particle> initialPedestrians, double dt, int writerIteration,
      double boxWidth, double boxHeight, double gap, double maxRadius, double minRadius,
      double vdMax, double beta, double tao) {
    this.initialPedestrians = initialPedestrians;
    this.dt = dt;
    this.writerIteration = writerIteration;
    this.boxWidth = boxWidth;
    this.boxHeight = boxHeight;
    this.gap = gap;
    this.maxRadius = maxRadius;
    this.minRadius = minRadius;
    this.vdMax = vdMax;
    this.beta = beta;
    this.tao = tao;
    this.exitTimes = new HashMap<>();
    this.finalDestination = new Point2D(1.1 * boxWidth, boxHeight / 2);
    this.cim = new CellIndexMethod(
        finalDestination.getX() > boxHeight ? finalDestination.getX() : boxHeight, false);
  }

  @Override
  public Set<Particle> simulate(Criteria endCriteria, ParticlesWriter writer) {
    Set<Particle> currentPedestrians = initialPedestrians;
    int iteration = 1;
    double time = dt;

    while (!endCriteria.test(time, currentPedestrians)) {
      final Map<Particle, Set<Neighbour>> neighbours = cim.apply(currentPedestrians, maxRadius, 0);
      currentPedestrians = movePedestrians(neighbours, time);

      if (iteration == writerIteration) {
        iteration = 0;
        try {
          writer.write(time, neighbours);
        } catch (IOException e) {
          System.err.println("Error while writing iteration");
        }
      }

      time += dt;
      iteration++;
    }

    return currentPedestrians;
  }

  private Set<Particle> movePedestrians(final Map<Particle, Set<Neighbour>> neighbours,
      final double time) {
    final Set<Particle> nextPedestrians = new HashSet<>(neighbours.size());

    for (final Map.Entry<Particle, Set<Neighbour>> entry : neighbours.entrySet()) {
      final Particle movedPedestrian;

      addWallParticles(entry.getKey(), entry.getValue());

      if (entry.getValue().size() == 0) {
        movedPedestrian = movePedestrian(entry.getKey());
      } else {
        movedPedestrian = movePedestrian(entry.getKey(), entry.getValue());
      }

      if (movedPedestrian.position().getX() < finalDestination.getX()) {
        nextPedestrians.add(movedPedestrian);

        if (entry.getKey().position().getX() <= boxWidth
            && movedPedestrian.position().getX() > boxWidth) {
          exitTimes.put(movedPedestrian, time);
        }
      }
    }

    return nextPedestrians;
  }

  private Particle movePedestrian(final Particle pedestrian) {
    final double vd =
        vdMax * pow((pedestrian.radius() - minRadius) / (maxRadius - minRadius), beta);
    final Point2D newVelocity = target(pedestrian)
        .subtract(pedestrian.position())
        .normalize()
        .multiply(vd);

    // TODO: Preguntar si es newVelocity o la actual
    final Point2D newPosition = Points.linearMotion(pedestrian.position(), newVelocity, dt);

    double newRadius = pedestrian.radius() + maxRadius / (tao / dt);
    if (newRadius > maxRadius) {
      newRadius = maxRadius;
    }

    return ImmutableParticle.builder().from(pedestrian)
        .position(newPosition)
        .velocity(newVelocity)
        .radius(newRadius)
        .build();
  }

  private Particle movePedestrian(final Particle pedestrian, final Set<Neighbour> neighbours) {
    Point2D normalUnitVector = Point2D.ZERO;
    for (final Neighbour neighbour : neighbours) {
      normalUnitVector = normalUnitVector
          .add(pedestrian.position()
              .subtract(neighbour.getNeighbourParticle().position()));
    }
    final Point2D newVelocity = normalUnitVector
        .normalize()
        .multiply(vdMax);

    // TODO: Preguntar si es newVelocity o la actual
    final Point2D newPosition = Points.linearMotion(pedestrian.position(), newVelocity, dt);

    return ImmutableParticle.builder().from(pedestrian)
        .radius(minRadius)
        .velocity(newVelocity)
        .position(newPosition)
        .build();
  }

  private Point2D target(final Particle pedestrian) {
    if (pedestrian.position().getX() >= boxWidth) {
      return new Point2D(finalDestination.getX(), pedestrian.position().getY());
    }

    final double gapStart = boxHeight / 2 - gap / 2;
    final double gapEnd = boxHeight - gapStart;

    if (pedestrian.position().getY() + pedestrian.radius() > gapEnd) {
      return new Point2D(boxWidth, gapEnd - minRadius);
    }

    if (pedestrian.position().getY() - pedestrian.radius() < gapStart) {
      return new Point2D(boxWidth, gapStart + minRadius);
    }

    return new Point2D(boxWidth, pedestrian.position().getY());
  }

  private void addWallParticles(final Particle particle, final Set<Neighbour> neighbours) {
    int wallId = -1;

    // up wall
    double distanceToWall = boxHeight - (particle.position().getY() + particle.radius());
    if (distanceToWall < 0) {
      neighbours.add(new Neighbour(ImmutableParticle.builder()
          .id(wallId--)
          .position(new Point2D(particle.position().getX(), boxHeight))
          .mass(Double.POSITIVE_INFINITY)
          .radius(0)
          .velocity(Point2D.ZERO)
          .build(), distanceToWall));
    }

    // down wall
    distanceToWall = particle.position().getY() - particle.radius();
    if (distanceToWall < 0) {
      neighbours.add(new Neighbour(ImmutableParticle.builder()
          .id(wallId--)
          .position(new Point2D(particle.position().getX(), 0))
          .mass(Double.POSITIVE_INFINITY)
          .radius(0)
          .velocity(Point2D.ZERO)
          .build(), distanceToWall));
    }

    // left wall
    distanceToWall = particle.position().getX() - particle.radius();
    if (distanceToWall < 0) {
      neighbours.add(new Neighbour(ImmutableParticle.builder()
          .id(wallId--)
          .position(new Point2D(0, particle.position().getY()))
          .mass(Double.POSITIVE_INFINITY)
          .radius(0)
          .velocity(Point2D.ZERO)
          .build(), distanceToWall));
    }

    // right wall
    double gapStart = boxHeight / 2 - gap / 2;
    double gapEnd = boxHeight - gapStart;
    distanceToWall = boxWidth - (particle.position().getX() + particle.radius());
    if (distanceToWall < 0
        && (particle.position().getY() <= gapStart || particle.position().getY() >= gapEnd)) {
      neighbours.add(new Neighbour(ImmutableParticle.builder()
          .id(wallId--)
          .position(new Point2D(boxWidth, particle.position().getY()))
          .mass(Double.POSITIVE_INFINITY)
          .radius(0)
          .velocity(Point2D.ZERO)
          .build(), distanceToWall));
    }

    // gap
    if (particle.position().getY() > gapStart && particle.position().getY() < gapEnd) {
      final Point2D gapStartPosition = new Point2D(boxWidth, gapStart);
      final Point2D gapEndPosition = new Point2D(boxWidth, gapEnd);
      final double gapStartDistance =
          particle.position().distance(gapStartPosition) - particle.radius();
      final double gapEndDistance =
          particle.position().distance(gapEndPosition) - particle.radius();

      if (gapStartDistance < 0) {
        neighbours.add(new Neighbour(ImmutableParticle.builder()
            .id(wallId--)
            .position(gapStartPosition)
            .mass(Double.POSITIVE_INFINITY)
            .radius(0)
            .velocity(Point2D.ZERO)
            .build(), distanceToWall));
      }

      if (gapEndDistance < 0) {
        neighbours.add(new Neighbour(ImmutableParticle.builder()
            .id(wallId--)
            .position(gapEndPosition)
            .mass(Double.POSITIVE_INFINITY)
            .radius(0)
            .velocity(Point2D.ZERO)
            .build(), distanceToWall));
      }
    }
  }

  public List<Double> getExitTimes() {
    return exitTimes.values().stream().sorted().collect(Collectors.toList());
  }
}
