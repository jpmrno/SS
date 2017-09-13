package ar.edu.itba.ss.model;

import java.util.List;
import javafx.geometry.Point2D;

public abstract class Points {

  public static Point2D polarToPoint2D(final double r, final double theta) {
    if (r < 0) {
      throw new IllegalArgumentException("Radius must be positive");
    }

    final double x = r * Math.cos(theta);
    final double y = r * Math.sin(theta);

    return new Point2D(x, y);
  }

  public static Point2D magnitudeToPoint2D(final double magnitude) {
    if (magnitude < 0) {
      throw new IllegalArgumentException("Magnitude must be positive");
    }

    final double xy = Math.sqrt(magnitude * magnitude / 2);

    return new Point2D(xy, xy);
  }

  public static double normalAverage(final List<Point2D> points) {
    return points.stream().map(Point2D::normalize).reduce(Point2D::add).get().magnitude()
        / (double) points.size();
  }

  public static Point2D linearMotion(final Point2D position, final Point2D velocity,
      final double dt) {
    double x = position.getX() + velocity.getX() * dt;
    double y = position.getY() + velocity.getY() * dt;

    return new Point2D(x, y);
  }

  public static int between(final List<Point2D> points, final Point2D start, final Point2D end) {
    int ret = 0;

    for (final Point2D point : points) {
      if (point.getX() >= start.getX() && point.getY() >= start.getY() && point.getX() <= end.getX()
          && point.getY() <= end.getY()) {
        ret++;
      }
    }

    return ret;
  }
}
