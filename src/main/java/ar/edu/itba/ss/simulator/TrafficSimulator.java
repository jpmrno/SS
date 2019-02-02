package ar.edu.itba.ss.simulator;

import static java.lang.Math.min;

import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Road;
import ar.edu.itba.ss.model.Segment;
import ar.edu.itba.ss.model.TrafficLight;
import ar.edu.itba.ss.model.TrafficLight.Status;
import ar.edu.itba.ss.model.criteria.Criteria;
import java.util.Comparator;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

public class TrafficSimulator implements Simulator {

  private static final Random RANDOM = ThreadLocalRandom.current();

  private static final double CELL_LENGTH = 2.5;
  private static final int RED_TIME = 7;
  private static final int YELLOW_TIME = 3;
  private static final int GREEN_TIME = 10;
//  private static final int[] VEHICLES = new int[]{1, 2, 3, 5};
//  private static final double[] VEHICLES_PROBABILITY = new double[]{0.1, 0.7, 0.15, 0.05};
  private static final int[] VEHICLES = new int[]{1};
  private static final double[] VEHICLES_PROBABILITY = new double[]{1};
  private static final EnumeratedIntegerDistribution vehiclesDistribution =
      new EnumeratedIntegerDistribution(VEHICLES, VEHICLES_PROBABILITY);

  private static int LAST_ID = 0;

  private final Segment segment;

  public TrafficSimulator(final int nVehicles, final int lanes, final int length, final int vMax,
      final double slowDownProbability) {
    final TrafficLight trafficLight = new TrafficLight(GREEN_TIME, YELLOW_TIME, RED_TIME, Status.RED);
    this.segment = new Road(lanes, length, trafficLight, vMax, slowDownProbability, null, null);
    ((Road)this.segment).setNextSegment(this.segment);
    ((Road)this.segment).setPreviousSegment(this.segment);
    new ParticleGenerator(nVehicles).generate((Road)this.segment);
  }

  @Override
  public Set<Particle> simulate(final Criteria endCriteria, final ParticlesWriter writer) {
    Set<Particle> currentParticles;
    long iteration = 0;
    do {
      // en realidad deberia ser una lista de segmentos
      segment.setActualized(true);
      currentParticles = segment.timeLapse(++iteration, writer);
      segment.setActualized(false);
    } while (!endCriteria.test(iteration, currentParticles));
    return currentParticles;
  }

  private static final class ParticleGenerator implements Road.ParticleGenerator {

    private final int nVehicles;

    public ParticleGenerator(final int nVehicles) {
      this.nVehicles = nVehicles;
    }

    @Override
    public void generate(final Road road) {
      generateParticles(road, nVehicles);
      updateParticlesWithProperties(road);
    }

    private void generateParticles(final Road road, final int nVehicles) {
      int vehicles = 0;
      while (vehicles != nVehicles) {
        final int row = RANDOM.nextInt(road.lanes());
        final int col = RANDOM.nextInt(road.laneLength());
        if (road.isValidPosition(row, col, 1)) {
          vehicles++;
          final Particle particle = Particle.builder()
              .id(++LAST_ID)
              .position(row, col)
              .build();
          road.put(particle);
        }
      }
    }

    private void updateParticlesWithProperties(final Road road) {
      for (final Particle particle : road.particles()) {
        final OptionalInt distanceOptional = road.distanceToNextParticle(particle);
        if (!distanceOptional.isPresent()) {
          continue;
        }
        final int distance = distanceOptional.getAsInt();
        int length = getRandomVehicleLength();
        if (length <= distance) {
          putParticleWithProperties(road, particle, distance, length);
        } else {
          final int[] lengths = previousLengths(length);
          for (final int otherLength : lengths) {
            if (otherLength <= distance) {
              putParticleWithProperties(road, particle, distance, otherLength);
            }
          }
        }
      }
    }

    private void putParticleWithProperties(final Road road, final Particle particle, final int distance,
        final int length) {
      final Particle newParticle = Particle.builder().from(particle)
          .velocity(RANDOM.nextInt(min(road.vMax(), distance - length) + 1))
          .length(length)
          .build();
      road.replace(particle, newParticle);
    }

    private static int getRandomVehicleLength() {
      return vehiclesDistribution.sample();
    }

    private static int[] previousLengths(final int lastLength) {
      return IntStream.of(VEHICLES)
          .boxed()
          .sorted(Comparator.reverseOrder())
          .filter(l -> l < lastLength)
          .mapToInt(i -> i)
          .toArray();
    }
  }
}
