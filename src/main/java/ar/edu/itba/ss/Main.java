package ar.edu.itba.ss;

import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.ParticleWrapper;
import ar.edu.itba.ss.model.Road;
import ar.edu.itba.ss.model.Tendency;
import ar.edu.itba.ss.model.generator.VehicleGenerator;
import ar.edu.itba.ss.simulator.TrafficSimulator;
import ar.edu.itba.ss.util.Either;
import java.util.List;
import java.util.function.BiFunction;

public class Main {

  private static final int N_VEHICLES = 40;
  private static final int LANES = 5;
  private static final int LANES_LENGTH = 60;
  private static final double SLOW_DOWN_P = 0.0;
  private static final BiFunction<Particle, Road, List<Particle>> LANE_CHANGER =
      new Tendency(0.3)::tendencyToAnywhere;
  private static final VehicleGenerator VEHICLE_GENERATOR = VehicleGenerator.perTypeMaxVelocity();

  public static void main(final String[] args) {

    final TrafficSimulator simulator = new TrafficSimulator(N_VEHICLES, LANES, LANES_LENGTH, SLOW_DOWN_P, LANE_CHANGER,
        VEHICLE_GENERATOR);
    simulator.simulate((i, p) -> p.isEmpty(), (it, m, p) -> {
      System.out.println("Iteration: " + it);
      for (int i = 0; i < m.length; i++) {
        final Either<Particle, ParticleWrapper>[] n = m[i];
        for (int j = 0; j < n.length; j++) {
          final Either<Particle, ParticleWrapper> either = n[j];
          if (either == null) {
            System.out.print(".");
          } else if (either.isValuePresent()) {
            System.out.print(either.getValue().velocity());
          } else {
            System.out.print(">");
          }
        }
        System.out.print("\n");
      }
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });
  }
}
