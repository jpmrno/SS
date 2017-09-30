package ar.edu.itba.ss;

import ar.edu.itba.ss.generator.RandomParticleGenerator;
import ar.edu.itba.ss.io.writer.AppendFileParticlesWriter;
import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.method.BeemanMovementFunction;
import ar.edu.itba.ss.method.EulerMovementFunction;
import ar.edu.itba.ss.method.LennardJonesForceFunction;
import ar.edu.itba.ss.method.MovementFunction;
import ar.edu.itba.ss.method.neigbour.CellIndexMethod;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Points;
import ar.edu.itba.ss.model.criteria.Criteria;
import ar.edu.itba.ss.model.criteria.FractionCriteria;
import ar.edu.itba.ss.simulator.LennardJonesGasSimulator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import javafx.geometry.Point2D;

public class GasMain {

  private static final int N = 10;
  private static final double MASS = 0.1;
  private static final double RADIUS = 5;
  private static final double INITIAL_VELOCITY_MAGNITUDE = 10;
  private static final double DT = 0.1;
  private static final int WRITER_ITERATION = 1;

  private static final double BOX_HEIGHT = 200;
  private static final double BOX_WIDTH = 400;
  private static final double BOX_GAP = 10;

  private static final double RC = 20;
  private static final double EPSILON = 2;
  private static final double RM = 1;

  public static final BiFunction<Particle, Set<Neighbour>, Point2D> FORCE_FUNCTION =
      new LennardJonesForceFunction(EPSILON, RM);

  private static final CellIndexMethod cellIndexMethod = new CellIndexMethod(
      Math.max(BOX_HEIGHT, BOX_WIDTH), false);

  public static void main(final String[] args) {
    final List<Particle> previousParticles = randomParticles();
    final Map<Particle, Set<Neighbour>> previousNeighbours = cellIndexMethod
        .apply(previousParticles, RADIUS, RC);

    final List<Particle> currentParticles = new ArrayList<>(previousParticles.size());
    final Map<Particle, MovementFunction> movementFunctions = new HashMap<>(
        previousParticles.size());

    euler_addCurrentParticlesAndMovementFunctions(previousParticles, previousNeighbours, currentParticles,
        movementFunctions);

    final LennardJonesGasSimulator simulator = new LennardJonesGasSimulator(currentParticles,
        BOX_WIDTH, BOX_HEIGHT, BOX_GAP, DT, WRITER_ITERATION, RC, movementFunctions);

    final Criteria criteria = new FractionCriteria(Point2D.ZERO,
        new Point2D(BOX_WIDTH / 2, BOX_HEIGHT), f -> f < 0.9);

    final ParticlesWriter particlesWriter = new AppendFileParticlesWriter("ljg_simulation");

    simulator.simulate(criteria, particlesWriter);
  }

  private static List<Particle> randomParticles() {

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

    return RandomParticleGenerator.generateParticles(minParticle, maxParticle);
  }

  private static void addCurrentParticlesAndMovementFunctions(
      final List<Particle> previousParticles,
      final Map<Particle, Set<Neighbour>> previousNeighbours, final List<Particle> currentParticles,
      final Map<Particle, MovementFunction> movementFunctions) {
    for (final Particle previousParticle : previousParticles) {
      final Point2D previousAcceleration = FORCE_FUNCTION
          .apply(previousParticle, previousNeighbours.get(previousParticle))
          .multiply(1.0 / previousParticle.mass());

      final Particle currentParticle = EulerMovementFunction
          .move(previousParticle, previousNeighbours.get(previousParticle), DT, FORCE_FUNCTION);

      final MovementFunction movementFunction = new BeemanMovementFunction(FORCE_FUNCTION,
          previousAcceleration);

      currentParticles.add(currentParticle);
      movementFunctions.put(currentParticle, movementFunction);
    }
  }

  private static void euler_addCurrentParticlesAndMovementFunctions(
      final List<Particle> previousParticles,
      final Map<Particle, Set<Neighbour>> previousNeighbours, final List<Particle> currentParticles,
      final Map<Particle, MovementFunction> movementFunctions) {

    for (final Particle previousParticle : previousParticles) {
      final MovementFunction movementFunction = new EulerMovementFunction(FORCE_FUNCTION );

      currentParticles.add(previousParticle);
      movementFunctions.put(previousParticle, movementFunction);
    }
  }
}
