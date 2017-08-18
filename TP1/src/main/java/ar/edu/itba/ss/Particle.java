package ar.edu.itba.ss;

public class Particle {

  private static int total = 0;

  private final double radius;
  private final int id;

  public Particle(final double radius) {
    this.radius = radius;
    this.id = total++;
  }

  public double getRadius() {
    return radius;
  }

  public int getId() {
    return id;
  }

  @Override
  public String toString() {
    return "Particle#" + id + "{" +
        "radius=" + radius +
        '}';
  }
}
