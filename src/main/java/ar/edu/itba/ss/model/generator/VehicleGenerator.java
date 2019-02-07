package ar.edu.itba.ss.model.generator;

import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Road;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import java.util.Comparator;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static java.lang.Math.min;

public class VehicleGenerator {

  private static VehicleGenerator INSTANCE = null;

  private static final Random RANDOM = ThreadLocalRandom.current();
  private static int LAST_ID = 0;

  private VehicleGenerator() {
  }

  public static VehicleGenerator getInstance() {
    if(INSTANCE == null){
      INSTANCE = new VehicleGenerator();
    }
    return INSTANCE;
  }

  public void generate(final Road road, int nVehicles) {
    generateParticles(road, nVehicles, 0, road.lanes(), 0, road.laneLength());
    updateParticlesWithProperties(road);
  }

  public void generate(final Road road, int nVehicles, final int fromRow, final int toRow,
                       final int fromCol, final int toCol) {
    generateParticles(road, nVehicles, fromRow, toRow, fromCol, toCol);
    updateParticlesWithProperties(road);
  }

  private void generateParticles(final Road road, final int nVehicles, final int fromRow, final int toRow,
                                 final int fromCol, final int toCol) {
    int vehicles = 0;
    while (vehicles != nVehicles) {
      final int row = RANDOM.nextInt(toRow) + fromRow;
      final int col = RANDOM.nextInt(toCol) + fromCol;
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
      int length = getRandomVehicleLength(new EnumeratedIntegerDistribution(road.getVehicleLengths(), road.getVehicleProbabilities()));
      if (length <= distance) {
        putParticleWithProperties(road, particle, distance, length);
      } else {
        final int[] lengths = previousLengths(length, road.getVehicleLengths());
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
            .velocity(RANDOM.nextInt(min(road.maxVelocities().get(particle.row()), distance - length) + 1))
            .length(length)
            .build();
    road.replace(particle, newParticle);
  }

  private static int getRandomVehicleLength(EnumeratedIntegerDistribution vehiclesDistribution) {
    return vehiclesDistribution.sample();
  }

  private static int[] previousLengths(final int lastLength, int[] vehicleLengths) {
    return IntStream.of(vehicleLengths)
            .boxed()
            .sorted(Comparator.reverseOrder())
            .filter(l -> l < lastLength)
            .mapToInt(i -> i)
            .toArray();
  }
}
