package ar.edu.itba.ss;

import ar.edu.itba.ss.io.writer.AppendFileParticlesWriter;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Road;
import ar.edu.itba.ss.model.Tendency;
import ar.edu.itba.ss.model.criteria.StationaryStateCriteria;
import ar.edu.itba.ss.model.generator.VehicleGenerator;
import ar.edu.itba.ss.simulator.TrafficSimulator;
import java.util.List;
import java.util.function.BiFunction;

public class Main {

  private static final int N_VEHICLES = 80;
  private static final int LANES = 5;
  private static final int LANES_LENGTH = 150;
  private static final double SLOW_DOWN_P = 0.3;
  private static final BiFunction<Particle, Road, List<Particle>> LANE_CHANGER =
      new Tendency(0.3)::tendencyToAnywhere;
  private static final VehicleGenerator VEHICLE_GENERATOR = VehicleGenerator.perTypeMaxVelocity(1);

  public static void main(final String[] args) {
    final TrafficSimulator simulator =
        new TrafficSimulator(N_VEHICLES, LANES, LANES_LENGTH, SLOW_DOWN_P, LANE_CHANGER, VEHICLE_GENERATOR);
    simulator.simulate(new StationaryStateCriteria(40, 0.2), new AppendFileParticlesWriter("traffic_simulation"));
  }
}
