package ar.edu.itba.ss.simulator;

import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.ParticleWrapper;
import ar.edu.itba.ss.model.Road;
import ar.edu.itba.ss.model.TrafficLight;
import ar.edu.itba.ss.model.criteria.Criteria;
import ar.edu.itba.ss.model.generator.VehicleGenerator;
import ar.edu.itba.ss.util.Either;
import ar.edu.itba.ss.util.VehicleType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public class TrafficSimulator implements Simulator {

  private static final double CELL_LENGTH = 2.5;
  private static final int RED_TIME = 7;
  private static final int YELLOW_TIME = 3;
  private static final int GREEN_TIME = 10;
  private static final VehicleType[] VEHICLES = new VehicleType[]{VehicleType.MOTORCYCLE, VehicleType.CAR, VehicleType.TRUCK};
  private static final double[] VEHICLES_PROBABILITY = new double[]{0.1, 0.8, 0.1};
  //  private static final VehicleType[] VEHICLES = new VehicleType[]{VehicleType.MOTORCYCLE};
//  private static final double[] VEHICLES_PROBABILITY = new double[]{1};
  private final VehicleGenerator vehicleGenerator;

  private final Road road1;

  public TrafficSimulator(final int nVehicles, final int lanes, final int length, final double slowDownProbability,
                          BiFunction<Particle, Road, List<Particle>> laneChanger, final VehicleGenerator vehicleGenerator) {
//    final TrafficLight trafficLight1 = new TrafficLight(GREEN_TIME, YELLOW_TIME, RED_TIME, Status.RED);
//    final TrafficLight trafficLight2 = new TrafficLight(GREEN_TIME, YELLOW_TIME, RED_TIME, Status.RED);
    this.vehicleGenerator = vehicleGenerator;
    this.road1 = new Road(lanes, length, TrafficLight.ALWAYS_GREEN, slowDownProbability, VEHICLES,
            VEHICLES_PROBABILITY, null, null, null, laneChanger, vehicleGenerator);


//    road0.setNextRoad(this.road1);
    road1.setNextRoad(this.road1);
    road1.setPreviousRoad(this.road1);
    road1.setOnExit(p -> {
      p = Particle.builder().from(p)
              .col(p.col() - road1.laneLength())
              .build();

      road1.incomingVehicle(p);
    });
    //TODO: hacer bien la funcion
    this.vehicleGenerator.generateInitialVehicles(this.road1, nVehicles);
  }

  @Override
  public Set<Particle> simulate(final Criteria endCriteria, final ParticlesWriter writer) {
    Set<Particle> currentParticles;
    long iteration = 0;
    do {
      // en realidad deberia ser una lista de segmentos
      road1.setActualized(true);
      road1.timeLapse();

//      road0.setActualized(false);
//      road0.moveVehicles();
      road1.setActualized(false);
      currentParticles = road1.moveVehicles();

      Either<Particle, ParticleWrapper>[][] lanes1 = road1.getLanes();

      System.out.println("Iteration " + iteration++);

      try {
        writer.write(iteration, Arrays.asList(road1));
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }
      for (int row = 0; row < road1.lanes(); row++) {
//        System.out.print("|");
        for (int col = 0; col < road1.laneLength(); col++) {
          if (lanes1[row][col] == null) {
            System.out.print(".");
          } else if (lanes1[row][col].isValuePresent()) {
            System.out.print(lanes1[row][col].getValue().velocity());
          } else {
            System.out.print(">");
          }
        }
//        System.out.print("|");

        System.out.println();
      }

      System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------");
    } while (!endCriteria.test(iteration, currentParticles));
    return currentParticles;
  }
}
