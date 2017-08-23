package ar.edu.itba.ss.model;

import javafx.geometry.Point2D;
import org.immutables.value.Value;

@Value.Immutable
public interface Particle {

  int id();

  double radius();

  @Value.Auxiliary
  Point2D position();
}
