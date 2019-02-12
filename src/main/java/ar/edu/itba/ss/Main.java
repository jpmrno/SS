package ar.edu.itba.ss;

import ar.edu.itba.ss.io.writer.AppendFileParticlesWriter;
import ar.edu.itba.ss.io.writer.FlowedParticlesWriter;
import ar.edu.itba.ss.io.writer.MultiWriter;
import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Road;
import ar.edu.itba.ss.model.Tendency;
import ar.edu.itba.ss.model.criteria.StationaryStateCriteria;
import ar.edu.itba.ss.model.generator.VehicleGenerator;
import ar.edu.itba.ss.simulator.TrafficSimulator;
import ar.edu.itba.ss.io.writer.AvgVelocityParticlesWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.BiFunction;

public class Main {

  private static final int N_VEHICLES = 200;
  private static final int LANES = 5;
  private static final int LANES_LENGTH = 400; // 1KM
  private static final double SLOW_DOWN_P = 0.3;
  private static final BiFunction<Particle, Road, List<Particle>> LANE_CHANGER =
      new Tendency(0.3)::tendencyToAnywhere;
  private static final VehicleGenerator VEHICLE_GENERATOR = VehicleGenerator.perTypeMaxVelocity(1);

  public static void main(final String[] args) throws IOException {
    final TrafficSimulator simulator =
        new TrafficSimulator(N_VEHICLES, LANES, LANES_LENGTH, SLOW_DOWN_P, LANE_CHANGER, VEHICLE_GENERATOR);
    final AppendFileParticlesWriter fileWriter = new AppendFileParticlesWriter("traffic_simulation");
    final AvgVelocityParticlesWriter avgVelocityWriter = new AvgVelocityParticlesWriter();
    final FlowedParticlesWriter flowWriter = new FlowedParticlesWriter(20);
    final ParticlesWriter writer = new MultiWriter(avgVelocityWriter, flowWriter);
    simulator.simulate(new StationaryStateCriteria(5 * 60, 0.5), writer);
    avgVelocityWriter.writeToFile("avg_velocity");
    flowWriter.writeToFile("flow");
  }
}
