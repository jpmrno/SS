package ar.edu.itba.ss.method;

import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import java.util.Set;
import java.util.function.BiFunction;
import javafx.geometry.Point2D;

public class LennardJonesForceFunction implements BiFunction<Particle, Set<Neighbour>, Point2D> {

  @Override
  public Point2D apply(final Particle particle, final Set<Neighbour> neighbours) {
    // TODO: Calcular por cada vecino la fuerza, sumarlas y devolverlas
    return null;
  }
}
