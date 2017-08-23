package ar.edu.itba.ss.model;

import java.awt.geom.Point2D;
import org.immutables.value.Value;

@Value.Immutable
public interface MovingParticle extends Particle {

  @Value.Auxiliary
  Point2D velocity();
}
