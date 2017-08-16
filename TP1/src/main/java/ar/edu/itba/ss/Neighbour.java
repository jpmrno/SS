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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Neighbour neighbour = (Neighbour) o;

    return neighbour.getDistance() == this.distance &&
            neighbour.getNeighbourParticle() == this.neighbourParticle;
  }

}
