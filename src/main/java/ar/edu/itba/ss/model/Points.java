package ar.edu.itba.ss.model;

import javafx.geometry.Point2D;

public abstract class Points {

  public static Point2D polarToPoint2D(final double magnitude, final double angle) {
    final double x = magnitude * Math.cos(angle);
    final double y = magnitude * Math.sin(angle);

    return new Point2D(x, y);
  }
}
