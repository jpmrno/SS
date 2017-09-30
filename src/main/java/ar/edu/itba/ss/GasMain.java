package ar.edu.itba.ss;

import ar.edu.itba.ss.generator.RandomParticleGenerator;
import ar.edu.itba.ss.method.BeemanMovementFunction;
import ar.edu.itba.ss.method.EulerMovementFunction;
import ar.edu.itba.ss.method.MovementFunction;
import ar.edu.itba.ss.method.neigbour.CellIndexMethod;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Points;
import ar.edu.itba.ss.simulator.LennardJonesGasSimulator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Point2D;

public class GasMain {

  private static final int N = 1000;
  private static final double MASS = 0.1;
  private static final double RADIUS = 5;
  private static final double INITIAL_VELOCITY_MAGNITUDE = 10;
  private static final double DT = 0.1;

  private static final double BOX_HEIGHT = 200;
  private static final double BOX_WIDTH = 400;
  private static final double BOX_GAP = 10;

  private static final double RC = 20;
  private static final double EPSILON = 2;
  private static final double RM = 1;

  private static final CellIndexMethod cellIndexMethod = new CellIndexMethod(
      Math.max(BOX_HEIGHT, BOX_WIDTH), false);

  public static void main(final String[] args) {
    final Particle minParticle = ImmutableParticle.builder()
        .id(1)
        .position(new Point2D(RADIUS, RADIUS))
        .velocity(Points.magnitudeToPoint2D(INITIAL_VELOCITY_MAGNITUDE))
        .radius(RADIUS)
        .mass(MASS)
        .build();
    final Particle maxParticle = ImmutableParticle.builder()
        .id(N)
        .position(new Point2D(BOX_WIDTH / 2 - RADIUS, BOX_HEIGHT - RADIUS))
        .velocity(Points.magnitudeToPoint2D(INITIAL_VELOCITY_MAGNITUDE))
        .radius(RADIUS)
        .mass(MASS)
        .build();

    final List<Particle> previousParticles = RandomParticleGenerator
        .generateParticles(minParticle, maxParticle);
    final Map<Particle, Set<Neighbour>> previousNeighbours = cellIndexMethod
        .apply(previousParticles, RADIUS, RC);

    final List<Particle> currentParticles = new ArrayList<>(previousParticles.size());
    final Map<Particle, MovementFunction> movementFunctions = new HashMap<>();
    for (final Particle previousParticle : previousParticles) {
      final Point2D previousAcceleration = LennardJonesGasSimulator.FORCE_FUNCTION
          .apply(previousParticle, previousNeighbours.get(previousParticle))
          .multiply(1.0 / previousParticle.mass());

      final Particle currentParticle = EulerMovementFunction
          .move(previousParticle, previousNeighbours.get(previousParticle), DT,
              LennardJonesGasSimulator.FORCE_FUNCTION);

      final MovementFunction movementFunction = new BeemanMovementFunction(
          LennardJonesGasSimulator.FORCE_FUNCTION,
          previousAcceleration);

      currentParticles.add(currentParticle);
      movementFunctions.put(currentParticle, movementFunction);
    }

    final LennardJonesGasSimulator simulator = new LennardJonesGasSimulator(currentParticles,
        BOX_WIDTH, BOX_HEIGHT, BOX_GAP, DT, EPSILON, RM, RC, movementFunctions);

    // TODO: Agregar logica de movementFunctions dentro de LennardJonesGasSimulator
  }
}
