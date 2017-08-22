package ar.edu.itba.ss.model;

public class Particle {

  private static int total = 0;

  private final double radius;
  private final int id;

  public Particle(final double radius) {
    this(total++, radius);
  }

  public Particle(final int id, final double radius) {
    if (radius < 0) {
      throw new IllegalArgumentException("Radius can't be less than 0");
    }

    this.radius = radius;
    this.id = id;
  }

  public double getRadius() {
    return radius;
  }

  public int getId() {
    return id;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof Particle)) {
      return false;
    }

    final Particle particle = (Particle) o;

    if (Double.compare(particle.radius, radius) != 0) {
      return false;
    }

    return id == particle.id;
  }

  @Override
  public int hashCode() {
    int result;
    long temp = Double.doubleToLongBits(radius);

    result = (int) (temp ^ (temp >>> 32));
    result = 31 * result + id;

    return result;
  }

  @Override
  public String toString() {
    return "Particle#" + id + "{" +
        "radius=" + radius +
        '}';
  }
}
