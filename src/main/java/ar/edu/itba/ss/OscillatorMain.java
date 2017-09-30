package ar.edu.itba.ss;

import ar.edu.itba.ss.method.EulerMovementFunction;
import ar.edu.itba.ss.method.MovementFunction;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Scatter2DChart;
import ar.edu.itba.ss.oscilator.DampedHarmonicOscillator;
import ar.edu.itba.ss.oscilator.DampedHarmonicOscillator.UnderdampedOscillatorForceFunction;
import java.util.LinkedList;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Point2D;

public class OscillatorMain {

  private static final double MASS = 70;
  private static final double K = 10000;
  private static final double B = 100;
  private static final double DT = 0.1;
  private static final double TIMES = 50;

  public static void main(String[] args) {
    Scatter2DChart.initialize("Oscillator", "Time", 0, TIMES * DT, DT, "Position", -1, 1, 0.1);

    final Particle particle = ImmutableParticle.builder()
        .id(1)
        .position(new Point2D(1, 0))
        .velocity(new Point2D(-B / (2 * MASS), 0))
        .radius(0)
        .mass(MASS)
        .build();

    final UnderdampedOscillatorForceFunction forceFunction =
        new UnderdampedOscillatorForceFunction(K, B);

    final MovementFunction movementFunction = new EulerMovementFunction(forceFunction);

    final DampedHarmonicOscillator oscillator = new DampedHarmonicOscillator(particle, K, B, DT,
        movementFunction);

    final List<Point2D> points = new LinkedList<>();
    for (int i = 1; i <= TIMES; i++) {
      points.add(new Point2D(DT * i, oscillator.move(particle, i * DT).position().getX()));
    }

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Platform.runLater(() -> Scatter2DChart.addSeries("Analytic", points));
  }
}
