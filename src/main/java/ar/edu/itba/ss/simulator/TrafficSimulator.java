package ar.edu.itba.ss.simulator;

import static java.lang.Math.min;

import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.model.*;
import ar.edu.itba.ss.model.TrafficLight.Status;
import ar.edu.itba.ss.model.criteria.Criteria;

import java.util.*;
import java.util.function.Consumer;

import ar.edu.itba.ss.model.generators.VehicleGenerator;
import ar.edu.itba.ss.util.Either;

public class TrafficSimulator implements Simulator {

  private static final double CELL_LENGTH = 2.5;
  private static final int RED_TIME = 7;
  private static final int YELLOW_TIME = 3;
  private static final int GREEN_TIME = 10;
//  private static final int[] VEHICLES = new int[]{1, 2, 3, 5};
//  private static final double[] VEHICLES_PROBABILITY = new double[]{0.1, 0.7, 0.15, 0.05};
  private static final int[] VEHICLES = new int[]{1};
  private static final double[] VEHICLES_PROBABILITY = new double[]{1};
  private final VehicleGenerator generator;

  private final Road road1;
  private final Road road2;

  public TrafficSimulator(final int nVehicles, final int lanes, final int length, final Map<Integer,Integer> maxVelocities,
      final double slowDownProbability) {
    final TrafficLight trafficLight1 = new TrafficLight(GREEN_TIME, YELLOW_TIME, RED_TIME, Status.RED);
    final TrafficLight trafficLight2 = new TrafficLight(GREEN_TIME, YELLOW_TIME, RED_TIME, Status.RED);
    this.road1 = new Road(lanes, length, TrafficLight.ALWAYS_GREEN, maxVelocities, slowDownProbability, VEHICLES,
            VEHICLES_PROBABILITY, null, null, null);
    this.road2 = new Road(lanes, length, TrafficLight.ALWAYS_GREEN, maxVelocities, slowDownProbability, VEHICLES,
            VEHICLES_PROBABILITY, road1, road1, p -> {
      p = Particle.builder().from(p)
              .col(p.col() - length)
              .build();

      road1.incomingVehicle(p);
    });
    road1.setNextSegment(this.road2);
    road1.setPreviousSegment(this.road2);
    road1.setOnExit(p -> {
      p = Particle.builder().from(p)
                  .col(p.col() - length)
                  .build();

      road2.incomingVehicle(p);
    });
    this.generator = VehicleGenerator.getInstance();
    generator.generate((Road)this.road1, nVehicles);
    generator.generate((Road)this.road2, nVehicles);
  }

  @Override
  public Set<Particle> simulate(final Criteria endCriteria, final ParticlesWriter writer) {
    Set<Particle> currentParticles;
    long iteration = 0;
    do {
      // en realidad deberia ser una lista de segmentos
      road1.setActualized(true);
      currentParticles = road1.timeLapse();
      road2.setActualized(true);
      currentParticles = road2.timeLapse();
      road1.setActualized(false);
      road2.setActualized(false);

      Either<Particle, ParticleWrapper>[][] lanes1 = road1.getLanes();
      Either<Particle, ParticleWrapper>[][] lanes2 = road2.getLanes();

      System.out.println("Iteration " + iteration++);

      for (int row = 0; row < road1.lanes(); row++) {
        for (int col = 0; col < road1.laneLength(); col++) {
          if (lanes1[row][col] == null) {
            System.out.print(".");
          } else if (lanes1[row][col].isValuePresent()) {
            System.out.print(lanes1[row][col].getValue().velocity());
          } else {
            System.out.print(">");
          }
        }

        for (int col = 0; col < road2.laneLength(); col++) {
          if (lanes2[row][col] == null) {
            System.out.print(".");
          } else if (lanes2[row][col].isValuePresent()) {
            System.out.print(lanes2[row][col].getValue().velocity());
          } else {
            System.out.print(">");
          }
        }

        System.out.println();
      }

      System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------");
    } while (!endCriteria.test(iteration, currentParticles));
    return currentParticles;
  }
}
