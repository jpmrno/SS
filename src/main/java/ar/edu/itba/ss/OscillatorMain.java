package ar.edu.itba.ss;

import ar.edu.itba.ss.method.EulerMovementFunction;
import ar.edu.itba.ss.method.GearMovementFunction;
import ar.edu.itba.ss.method.MovementFunction;
import ar.edu.itba.ss.method.VDBeemanMovementFunction;
import ar.edu.itba.ss.method.VerletMovementFunction;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Scatter2DChart;
import ar.edu.itba.ss.oscilator.AnalyticUDHOscillator;
import ar.edu.itba.ss.oscilator.AnalyticUDHOscillator.UnderdampedOscillatorForceFunction;
import ar.edu.itba.ss.oscilator.NumericalUDHOscillator;
import java.util.LinkedList;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Point2D;

public class OscillatorMain {

  private static final double MASS = 70;
  private static final double K = 10000;
  private static final double B = 100;
  private static final double DT = 0.01;
  private static final double TIMES = 5 / DT;

  private static final UnderdampedOscillatorForceFunction FORCE_FUNCTION =
      new UnderdampedOscillatorForceFunction(K, B);

  public static void main(String[] args) {
    Scatter2DChart.initialize("Oscillator", "Time", 0, TIMES * DT, DT, "Position", -1, 1, 0.1);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    final Particle particle = ImmutableParticle.builder()
        .id(1)
        .position(new Point2D(1, 0))
        .velocity(new Point2D(-B / (2 * MASS), 0))
        .radius(0)
        .mass(MASS)
        .build();

    addAnaliticToGraph(particle);

    final MovementFunction eulerFunction = new EulerMovementFunction(FORCE_FUNCTION);

    final NumericalUDHOscillator eulerUDHOscillator = new NumericalUDHOscillator(particle, K, B,
        eulerFunction);

    addEulerToGraph(particle, eulerUDHOscillator);

    addVerletToGraph(particle, eulerUDHOscillator);

    addBeemanToGraph(particle, eulerUDHOscillator);

    addGear5ToGraph(particle);
  }

  private static void addEulerToGraph(final Particle particle,
      final NumericalUDHOscillator eulerUDHOscillator) {

    Particle nextParticle = particle;
    final List<Point2D> pointsEuler = new LinkedList<>();
    for (int i = 1; i <= TIMES; i++) {
      nextParticle = eulerUDHOscillator.move(nextParticle, DT);
      pointsEuler.add(new Point2D(DT * i, nextParticle.position().getX()));
    }

    Platform.runLater(() -> Scatter2DChart.addSeries("Euler", pointsEuler));
  }

  private static void addAnaliticToGraph(final Particle particle) {
    final AnalyticUDHOscillator analyticUDHOscillator = new AnalyticUDHOscillator(particle, K, B);

    Particle nextParticle = particle;
    final List<Point2D> pointsAnalytic = new LinkedList<>();
    for (int i = 1; i <= TIMES; i++) {
      nextParticle = analyticUDHOscillator.move(nextParticle, DT);
      pointsAnalytic.add(new Point2D(DT * i, nextParticle.position().getX()));
    }

    Platform.runLater(() -> Scatter2DChart.addSeries("Analytic", pointsAnalytic));
  }

  private static void addVerletToGraph(final Particle particle,
      final NumericalUDHOscillator eulerUDHOscillator) {
    final MovementFunction verletFunction = new VerletMovementFunction(FORCE_FUNCTION,
        particle.position());

    final NumericalUDHOscillator verletUDHOscillator = new NumericalUDHOscillator(particle, K, B,
        verletFunction);

    Particle nextParticle = eulerUDHOscillator.move(particle, DT);
    final List<Point2D> pointsVerlet = new LinkedList<>();
    for (int i = 1; i <= TIMES; i++) {
      nextParticle = verletUDHOscillator.move(nextParticle, DT);
      pointsVerlet.add(new Point2D(DT * i, nextParticle.position().getX()));
    }

    Platform.runLater(() -> Scatter2DChart.addSeries("Verlet", pointsVerlet));
  }

  private static void addBeemanToGraph(final Particle particle,
      final NumericalUDHOscillator eulerUDHOscillator) {
    final MovementFunction beemanFunction = new VDBeemanMovementFunction(FORCE_FUNCTION,
        FORCE_FUNCTION.apply(particle, null).multiply(1.0 / particle.mass()));

    final NumericalUDHOscillator beemanUDHOscillator = new NumericalUDHOscillator(particle, K, B,
        beemanFunction);

    Particle nextParticle = eulerUDHOscillator.move(particle, DT);
    final List<Point2D> pointsBeeman = new LinkedList<>();
    for (int i = 1; i <= TIMES; i++) {
      nextParticle = beemanUDHOscillator.move(nextParticle, DT);
      pointsBeeman.add(new Point2D(DT * i, nextParticle.position().getX()));
    }

//    Platform.runLater(() -> Scatter2DChart.addSeries("Beeman", pointsBeeman));
  }

  private static void addGear5ToGraph(final Particle particle) {
    final Point2D[] r = new Point2D[5 + 1];
    r[0] = particle.position();
    r[1] = particle.velocity();
    for (int i = 2; i < 5 + 1; i++) {
      r[i] = r[i - 2].multiply(-K).subtract(r[i - 1].multiply(B)).multiply(1.0 / particle.mass());
    }

    final MovementFunction gearFunction = new GearMovementFunction(FORCE_FUNCTION,
        GearMovementFunction.GEAR_5_VD_ALPHAS, r);

    final NumericalUDHOscillator gearUDHOscillator = new NumericalUDHOscillator(particle, K, B,
        gearFunction);

    Particle nextParticle = particle;
    final List<Point2D> pointsGear = new LinkedList<>();
    for (int i = 1; i <= TIMES; i++) {
      nextParticle = gearUDHOscillator.move(nextParticle, DT);
      pointsGear.add(new Point2D(DT * i, nextParticle.position().getX()));
    }

    Platform.runLater(() -> Scatter2DChart.addSeries("Gear", pointsGear));
  }
}
