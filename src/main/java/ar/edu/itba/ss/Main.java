package ar.edu.itba.ss;

import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.ParticleWrapper;
import ar.edu.itba.ss.simulator.TrafficSimulator;
import ar.edu.itba.ss.util.Either;

import java.util.HashMap;
import java.util.Map;

public class Main {

  private static final int N_VEHICLES = 40;
  private static final int LANES = 5;
  private static final int LANES_LENGTH = 60;
  private static final int V_MAX = 6;
  private static final double SLOW_DOWN_P = 0.0;

  public static void main(final String[] args) {
    final Map<Integer,Integer> maxVelocities = new HashMap();
    for (int lane = 0; lane < LANES; lane++) {
      maxVelocities.put(lane, V_MAX);
    }


    final TrafficSimulator simulator = new TrafficSimulator(N_VEHICLES, LANES, LANES_LENGTH, maxVelocities, SLOW_DOWN_P);
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
