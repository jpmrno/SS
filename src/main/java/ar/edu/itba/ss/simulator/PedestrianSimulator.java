package ar.edu.itba.ss.simulator;

import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.method.neigbour.CellIndexMethod;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.criteria.Criteria;
import java.io.IOException;
import java.util.HashSet;
import javafx.geometry.Point2D;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.pow;

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
  private final List<Double> exitTimes;
  private int currentTimeExits = 0;
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
    this.exitTimes = new LinkedList<>();
    this.finalDestination = new Point2D(1.1 * boxWidth, boxHeight / 2);
    this.cim = new CellIndexMethod(
        finalDestination.getX() > boxHeight ? finalDestination.getX() : boxHeight, false);
  }

  @Override
  public Set<Particle> simulate(Criteria endCriteria, ParticlesWriter writer) {
    Set<Particle> currentPedestrians = initialPedestrians;
    int iteration = 1;
    double time = 0;

    while (!endCriteria.test(time, currentPedestrians)) {
      final Map<Particle, Set<Neighbour>> neighbours =
          cim.apply(currentPedestrians, maxRadius, maxRadius);
      currentPedestrians = movePedestrians(neighbours);

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

  private Set<Particle> movePedestrians(final Map<Particle, Set<Neighbour>> neighbours) {
    final Set<Particle> nextPedestrians = new HashSet<>(neighbours.size());

    for (final Map.Entry<Particle, Set<Neighbour>> entry : neighbours.entrySet()) {
      final Particle movedPedestrian;
      addWallParticles(entry.getKey(), entry.getValue());
      entry.getValue().stream().filter(n -> !n.getNeighbourParticle().collides(entry.getKey()));

      if (entry.getValue().size() == 0) {
        movedPedestrian = movePedestrian(entry.getKey());
      } else {
        movedPedestrian = movePedestrian(entry.getKey(), entry.getValue());
      }

      if(movedPedestrian.position().getX() < finalDestination.getX()){
        nextPedestrians.add(movedPedestrian);
      }
    }

    return nextPedestrians;
  }

  private Particle movePedestrian(final Particle pedestrian) {
    final double velocityMagnitude =
        vdMax * pow((pedestrian.radius() - minRadius) / (maxRadius - minRadius), beta);
    final Point2D target = target(pedestrian);
    final Point2D newVelocity = target.subtract(pedestrian.position())
        .normalize().multiply(velocityMagnitude);

    final double newX = pedestrian.position().getX() + velocityMagnitude * dt;
    final double newY = pedestrian.position().getY() + velocityMagnitude * dt;

    double newRadius = pedestrian.radius() + maxRadius / (tao / dt);
    if(newRadius > maxRadius){
      newRadius = maxRadius;
    }

    return ImmutableParticle.builder().from(pedestrian)
        .position(new Point2D(newX, newY))
        .velocity(newVelocity)
        .radius(newRadius)
        .build();
  }

  private Particle movePedestrian(final Particle pedestrian, final Set<Neighbour> neighbours) {
    Point2D normalUnitVector = Point2D.ZERO;

    for(Neighbour neighbour : neighbours){
      normalUnitVector.add(pedestrian.position().subtract(neighbour.getNeighbourParticle().position()));
    }
    normalUnitVector.normalize();

    final Point2D newVelocity = normalUnitVector.multiply(vdMax);
    final Point2D newPosition = pedestrian.position().add(newVelocity.multiply(dt));

    return ImmutableParticle.builder().from(pedestrian)
        .radius(minRadius)
        .velocity(newVelocity)
        .position(newPosition)
        .build();
  }

  private Point2D target(final Particle pedestrian) {
    if (isOutOfBox(pedestrian)) {
      return finalDestination;
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

  private boolean isOutOfBox(final Particle pedestrian) {
    return pedestrian.position().getX() >= boxWidth; // TODO: Test
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


}
