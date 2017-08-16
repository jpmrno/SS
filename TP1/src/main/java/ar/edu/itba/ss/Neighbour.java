package ar.edu.itba.ss;

public class Neighbour {

  private final Particle neighbourParticle;
  private final double distance;

  public Neighbour(final Particle neighbourParticle, final double distance) {
    this.neighbourParticle = neighbourParticle;
    this.distance = distance;
  }

  public Particle getNeighbourParticle() {
    return neighbourParticle;
  }

  public double getDistance() {
    return distance;
  }
}
