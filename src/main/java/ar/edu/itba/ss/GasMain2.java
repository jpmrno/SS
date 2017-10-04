package ar.edu.itba.ss;

import static java.lang.Math.pow;

import ar.edu.itba.ss.generator.RandomParticleGenerator;
import ar.edu.itba.ss.io.writer.BoxParticleWritter;
import ar.edu.itba.ss.method.BeemanMovementFunction;
import ar.edu.itba.ss.method.EulerMovementFunction;
import ar.edu.itba.ss.method.GearMovementFunction;
import ar.edu.itba.ss.method.LennardJonesForceFunction;
import ar.edu.itba.ss.method.MovementFunction;
import ar.edu.itba.ss.method.neigbour.CellIndexMethod;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Points;
import ar.edu.itba.ss.model.criteria.Criteria;
import ar.edu.itba.ss.model.criteria.EquilibriumOscilationCriteria;
import ar.edu.itba.ss.simulator.LennardJonesGasSimulator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javafx.geometry.Point2D;

public class GasMain2 {

  private static final int N = 90 * 1;
  private static final double MASS = 0.1;
  private static final double RADIUS = 1.5;
  private static final double INITIAL_VELOCITY_MAGNITUDE = 10;
//  private static final int WRITER_ITERATION = 1;

  private static final double BOX_HEIGHT = 60;
  private static final double BOX_WIDTH = 120;
  private static final double BOX_GAP = 5;

  private static final double RC = 5;
  private static final double EPSILON = 2;
  private static final double RM = 1;

  private static final BiFunction<Particle, Set<Neighbour>, Point2D> FORCE_FUNCTION =
      new LennardJonesForceFunction(EPSILON, RM);

  private static final CellIndexMethod cellIndexMethod = new CellIndexMethod(
      Math.max(BOX_HEIGHT, BOX_WIDTH), false);

  public static void main(final String[] args) {
//    Scatter2DChart.initialize("Energy vs Time");

    List<Particle> previousParticles = randomParticles();
    previousParticles = previousParticles.stream()
        .map(p -> ImmutableParticle.builder().from(p).radius(0).build())
        .collect(Collectors.toList());
    final Map<Particle, Set<Neighbour>> previousNeighbours = cellIndexMethod
        .apply(previousParticles, previousParticles.get(0).radius(), RC);

    System.out.println("Start");

    for (final double dt : new double[]{0.00001}) {
      final List<Particle> currentParticles = new ArrayList<>(previousParticles.size());
      final Map<Particle, MovementFunction> movementFunctions = new HashMap<>(
          previousParticles.size());

      beemanAddCurrentParticlesAndMovementFunctions(previousParticles, previousNeighbours,
          currentParticles, movementFunctions, dt);

      final LennardJonesGasSimulator simulator = new LennardJonesGasSimulator(currentParticles,
          BOX_WIDTH, BOX_HEIGHT, BOX_GAP, dt, (int) (1 / dt) / 10, RC, movementFunctions);

//      final Criteria criteria = new TimeCriteria(25);
      final Criteria criteria = new EquilibriumOscilationCriteria(Point2D.ZERO,
          new Point2D(BOX_WIDTH / 2, BOX_HEIGHT), 10, 0.03);

//      final EnergyWriter energyWriter = new EnergyWriter(EPSILON, RM);

      simulator.simulate(criteria,
          new BoxParticleWritter("simulation_imp90", BOX_WIDTH, BOX_HEIGHT, BOX_GAP));

//      final List<Point2D> points = energyWriter.getPoints();
//      Platform.runLater(() -> Scatter2DChart.addSeries("dt = " + dt, points));
//
//      System.out.println("Listo dt = " + dt);
    }
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

  private static void beemanAddCurrentParticlesAndMovementFunctions(
      final List<Particle> previousParticles,
      final Map<Particle, Set<Neighbour>> previousNeighbours, final List<Particle> currentParticles,
      final Map<Particle, MovementFunction> movementFunctions, final double dt) {
    for (final Particle previousParticle : previousParticles) {
      final Point2D previousAcceleration = FORCE_FUNCTION
          .apply(previousParticle, previousNeighbours.get(previousParticle))
          .multiply(1.0 / previousParticle.mass());

      final Particle currentParticle = EulerMovementFunction
          .move(previousParticle, previousNeighbours.get(previousParticle), dt, FORCE_FUNCTION);

      final MovementFunction movementFunction = new BeemanMovementFunction(FORCE_FUNCTION,
          previousAcceleration);

      currentParticles.add(currentParticle);
      movementFunctions.put(currentParticle, movementFunction);
    }
  }

  private static void eulerAddCurrentParticlesAndMovementFunctions(
      final List<Particle> previousParticles,
      final Map<Particle, Set<Neighbour>> previousNeighbours, final List<Particle> currentParticles,
      final Map<Particle, MovementFunction> movementFunctions) {

    final MovementFunction movementFunction = new EulerMovementFunction(FORCE_FUNCTION);
    for (final Particle previousParticle : previousParticles) {
      currentParticles.add(previousParticle);
      movementFunctions.put(previousParticle, movementFunction);
    }
  }

  private static void gearAddCurrentParticlesAndMovementFunctions(
      final List<Particle> previousParticles,
      final Map<Particle, Set<Neighbour>> previousNeighbours, final List<Particle> currentParticles,
      final Map<Particle, MovementFunction> movementFunctions) {
    for (final Particle previousParticle : previousParticles) {
      currentParticles.add(previousParticle);

      final Point2D[] r = new Point2D[5 + 1];
      r[0] = previousParticle.position();
      r[1] = previousParticle.velocity();
      r[2] = FORCE_FUNCTION.apply(previousParticle, previousNeighbours.get(previousParticle))
          .multiply(1.0 / previousParticle.mass());
      r[3] = accelerationDerivate1(previousParticle, previousNeighbours.get(previousParticle));
      r[4] = accelerationDerivate2(previousParticle, previousNeighbours.get(previousParticle));
      r[5] = accelerationDerivate3(previousParticle, previousNeighbours.get(previousParticle));

      final MovementFunction gearFunction = new GearMovementFunction(FORCE_FUNCTION,
          GearMovementFunction.GEAR_5_ALPHAS, r);

      movementFunctions.put(previousParticle, gearFunction);
    }
  }

  private static Point2D accelerationDerivate1(final Particle particle,
      final Set<Neighbour> neighbours) {
    double accelerationDerivate1X = 0;
    double accelerationDerivate1Y = 0;

    for (final Neighbour neighbour : neighbours) {
      final double magnitude =
          -12 * EPSILON * pow(RM, 6)
              * (13 * pow(RM, 6) - 7 * pow(neighbour.getDistance(), 6))
              / (particle.mass() * pow(neighbour.getDistance(), 14));
      final Point2D distanceVector = particle.position()
          .subtract(neighbour.getNeighbourParticle().position());
      accelerationDerivate1X += magnitude * (distanceVector.getX()) / distanceVector.magnitude();
      accelerationDerivate1Y += magnitude * (distanceVector.getY()) / distanceVector.magnitude();
    }

    return new Point2D(accelerationDerivate1X, accelerationDerivate1Y);
  }

  private static Point2D accelerationDerivate2(final Particle particle,
      final Set<Neighbour> neighbours) {
    double accelerationDerivate2X = 0;
    double accelerationDerivate2Y = 0;

    for (final Neighbour neighbour : neighbours) {
      final double magnitude =
          168 * EPSILON * pow(RM, 6)
              * (13 * pow(RM, 6) - 4 * pow(neighbour.getDistance(), 6))
              / (particle.mass() * pow(neighbour.getDistance(), 15));
      final Point2D distanceVector = particle.position()
          .subtract(neighbour.getNeighbourParticle().position());
      accelerationDerivate2X += magnitude * (distanceVector.getX()) / distanceVector.magnitude();
      accelerationDerivate2Y += magnitude * (distanceVector.getY()) / distanceVector.magnitude();
    }

    return new Point2D(accelerationDerivate2X, accelerationDerivate2Y);
  }

  private static Point2D accelerationDerivate3(final Particle particle,
      final Set<Neighbour> neighbours) {
    double accelerationDerivate3X = 0;
    double accelerationDerivate3Y = 0;

    for (final Neighbour neighbour : neighbours) {
      final double magnitude =
          -504 * EPSILON * pow(RM, 6)
              * (65 * pow(RM, 6) - 12 * pow(neighbour.getDistance(), 6))
              / (particle.mass() * pow(neighbour.getDistance(), 16));
      final Point2D distanceVector = particle.position()
          .subtract(neighbour.getNeighbourParticle().position());
      accelerationDerivate3X += magnitude * (distanceVector.getX()) / distanceVector.magnitude();
      accelerationDerivate3Y += magnitude * (distanceVector.getY()) / distanceVector.magnitude();
    }

    return new Point2D(accelerationDerivate3X, accelerationDerivate3Y);
  }
}
