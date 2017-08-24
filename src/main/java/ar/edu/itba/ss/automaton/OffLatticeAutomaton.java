package ar.edu.itba.ss.automaton;

import ar.edu.itba.ss.io.IterativeFiles;
import ar.edu.itba.ss.io.ParticlesFiles;
import ar.edu.itba.ss.method.CellIndexMethod;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Points;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import javafx.geometry.Point2D;

public class OffLatticeAutomaton implements Runnable {

  private final List<Particle> initialParticles;
  private final double rc;
  private final double dt;
  private final double totalTime;
  private final double eta;
  private final double l;
  private final String saveFileName;
  private final CellIndexMethod neighbourFinder;

  public OffLatticeAutomaton(final List<Particle> initialParticles, final double l, final double rc,
      final double dt, final double totalTime, final double eta, final String saveFileName) {
//  TODO: validate parameters
    this.initialParticles = initialParticles;
    this.rc = rc;
    this.dt = dt;
    this.totalTime = totalTime;
    this.eta = eta;
    this.l = l;
    this.neighbourFinder = new CellIndexMethod(l, true);
    this.saveFileName = saveFileName;
  }

  @Override
  public void run() {
    final Path saveFile = IterativeFiles.firstNotExists(saveFileName);
    List<Particle> currentParticles = initialParticles;
    double remainingTime = totalTime;

    try {
      Files.createFile(saveFile);
    } catch (IOException exception) {
      System.err.println("Can't create save file");
    }

    try {
      ParticlesFiles.append(saveFile, totalTime - remainingTime, currentParticles);
    } catch (IOException exception) {
      System.err.println("Can't save state at " + (totalTime - remainingTime));
    }

    while (remainingTime > 0) {
      final Map<Particle, Set<Neighbour>> neighbours = neighbourFinder
          .apply(currentParticles, 0, rc);
      final List<Particle> newParticles = new LinkedList<>();

      for (final Particle particle : currentParticles) {
        final ImmutableParticle.Builder particleBuilder = ImmutableParticle.builder()
            .from(particle);
        particleBuilder.position(calculateNewPosition(particle));
        particleBuilder.velocity(calculateNewVelocity(particle, neighbours.get(particle)));
        newParticles.add(particleBuilder.build());
      }

      currentParticles = newParticles;
      remainingTime -= dt;

      try {
        ParticlesFiles.append(saveFile, totalTime - remainingTime, currentParticles);
      } catch (IOException exception) {
        System.err.println("Can't save state at " + (totalTime - remainingTime));
      }
    }
  }

  private Point2D calculateNewPosition(final Particle particle) {
    double x = particle.position().getX() + particle.velocity().getX() * dt;
    double y = particle.position().getY() + particle.velocity().getY() * dt;

    while (x < 0 || x > l) {
      x += coordinateCorrection(x);
    }

    while (y < 0 || y > l) {
      y += coordinateCorrection(y);
    }

    return new Point2D(x, y);
  }

  private double coordinateCorrection(final double coordinate) {
    if (coordinate > l) {
      return -l;
    }

    if (coordinate < 0) {
      return l;
    }

    return 0;
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
}
