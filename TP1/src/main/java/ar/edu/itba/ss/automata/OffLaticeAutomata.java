package ar.edu.itba.ss.automata;

import ar.edu.itba.ss.method.CellIndexMethod;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import javafx.geometry.Point2D;

public class OffLaticeAutomata implements Runnable {

  private final List<Particle> initialParticles;
  private final double rc;
  private final double dt;
  private final double totalTime;
  private final double etha;
  private final double l;

  public OffLaticeAutomata(List<Particle> initialParticles, double rc, double dt,
      double totalTime, double etha, double l) {
//  TODO: validate parameters
    this.initialParticles = initialParticles;
    this.rc = rc;
    this.dt = dt;
    this.totalTime = totalTime;
    this.etha = etha;
    this.l = l;
  }

  @Override
  public void run() {
    List<Particle> currentParticles = initialParticles;
    double remainingTime = totalTime;
    CellIndexMethod cellIndexMethod = new CellIndexMethod(l, true);

    while (remainingTime >= 0) {
      Map<Particle, Set<Neighbour>> neighbours = cellIndexMethod.apply(currentParticles, 0, rc);
      List<Particle> newParticles = new LinkedList<>();

      for (Particle particle : currentParticles) {
        ImmutableParticle.Builder particleBuilder = ImmutableParticle.builder().from(particle);
        particleBuilder.position(calculateNewPosition(particle));
        particleBuilder.velocity(calculateNewVelocity(particle, neighbours.get(particle)));
        newParticles.add(particleBuilder.build());
      }

//    TODO: write in file

      currentParticles = newParticles;
      remainingTime -= dt;
    }
  }

//  TODO: validate position
  private Point2D calculateNewPosition(Particle particle) {
    double x = particle.position().getX() + particle.velocity().getX() * dt;
    double y = particle.position().getY() + particle.velocity().getY() * dt;
    return new Point2D(x, y);
  }

  //  TODO: check angle conversion
  private Point2D calculateNewVelocity(Particle particle, Set<Neighbour> neighbours) {
    double sines = Math.sin(particle.velocity().angle(Point2D.ZERO)) + neighbours.stream()
        .mapToDouble(n -> Math.sin(n.getNeighbourParticle().velocity().angle(Point2D.ZERO))).sum();
    double cosines = Math.cos(particle.velocity().angle(Point2D.ZERO)) + neighbours.stream()
        .mapToDouble(n -> Math.cos(n.getNeighbourParticle().velocity().angle(Point2D.ZERO))).sum();

    double angle = Math.atan2(sines, cosines);
    angle += ThreadLocalRandom.current().nextDouble(-etha / 2, etha / 2);

    return new Point2D(Math.cos(angle), Math.sin(angle));
  }
}
