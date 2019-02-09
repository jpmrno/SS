package ar.edu.itba.ss.model.generator;

import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Road;
import ar.edu.itba.ss.util.VehicleType;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.min;

public class VehicleGenerator {

  private static VehicleGenerator INSTANCE = null;

  private static final Random RANDOM = ThreadLocalRandom.current();
  private static int LAST_ID = 0;

  private VehicleGenerator() {
  }

  public static VehicleGenerator getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new VehicleGenerator();
    }
    return INSTANCE;
  }

  public int generateInitialVehicles(final Road road, int nVehicles, Function<VehicleType,Integer> maxVelocityCalculator) {
    int uninitializedVehicles = generateParticles(road, nVehicles, 0, road.lanes(), 0, road.laneLength(),
            null);
    for (Particle vehicle : road.particles()) {
      updateParticleWithProperties(vehicle, road, maxVelocityCalculator);
    }
    return uninitializedVehicles;
  }

  public int generate(final Road road, int nVehicles, final int fromRow, final int toRow,
                      final int fromCol, final int toCol, Function<VehicleType,Integer> maxVelocityCalculator) {
    Set<Particle> generated = new HashSet<>();
    int uninitializedVehicles = generateParticles(road, nVehicles, fromRow, toRow, fromCol, toCol, generated);
    generated.forEach(p -> {
      updateParticleWithProperties(p, road, maxVelocityCalculator);
    });
    return uninitializedVehicles;
  }

  private int generateParticles(final Road road, final int nVehicles, final int fromRow, final int toRow,
                                final int fromCol, final int toCol, Set<Particle> generated) {
    int vehicles = 0;
    List<Integer> rows = IntStream.range(fromRow, toRow).boxed().collect(Collectors.toList());
    List<Integer> cols = IntStream.range(fromCol, toCol).boxed().collect(Collectors.toList());
    List<int[]> shuffledPositions = new ArrayList<>();
    rows.forEach(r -> {
      cols.forEach(c -> {
        shuffledPositions.add(new int[]{r, c});
      });
    });
    Collections.shuffle(shuffledPositions);

    for (int[] position : shuffledPositions) {
      int row = position[0];
      int col = position[1];
      if (vehicles < nVehicles && road.isValidPosition(row, col, 1)) {
        vehicles++;
        final Particle particle = Particle.builder()
                .id(++LAST_ID)
                .position(row, col)
                .build();
        road.put(particle);
        if (generated != null) {
          generated.add(particle);
        }
      }
    }

    return nVehicles - vehicles;
  }

  private void updateParticleWithProperties(final Particle particle, final Road road, Function<VehicleType,Integer> maxVelocityCalculator) {
    final OptionalInt distanceOptional = road.distanceToNextParticle(particle);
    final int distance = distanceOptional.getAsInt();

    List<Pair<VehicleType, Double>> pairs = new ArrayList<>();
    for (int i = 0; i < road.getVehicleProbabilities().length; i++) {
      pairs.add(new Pair(road.getVehicleTypes()[i], road.getVehicleProbabilities()[i]));
    }

    VehicleType type = getRandomVehicleType(new EnumeratedDistribution(pairs));

    if (type.getLength() <= distance) {
      putParticleWithProperties(road, particle, type, maxVelocityCalculator);
    } else {
      final List<VehicleType> types = previousLengths(type.getLength(), road.getVehicleTypes());
      for (final VehicleType otherType : types) {
        if (otherType.getLength() <= distance) {
          putParticleWithProperties(road, particle, otherType, maxVelocityCalculator);
          return;
        }
      }
    }
  }

  private void putParticleWithProperties(final Road road, final Particle particle, final VehicleType type, Function<VehicleType,Integer> maxVelocityCalculator) {
    final int maxV = maxVelocityCalculator.apply(type);
    final Particle newParticle = Particle.builder().from(particle)
            .maxVelocity(maxV)
            .velocity(RANDOM.nextInt(maxV + 1))
            .vehicleType(type)
            .build();
    road.replace(particle, newParticle);
  }

  private static VehicleType getRandomVehicleType(EnumeratedDistribution<VehicleType> vehiclesDistribution) {
    return vehiclesDistribution.sample();
  }

  private static List<VehicleType> previousLengths(final int lastLength, VehicleType[] vehicleTypes) {
    return Arrays.stream(vehicleTypes)
            .sorted((o1, o2) -> o2.getLength() - o1.getLength())
            .filter(v -> v.getLength() < lastLength)
            .collect(Collectors.toList());
  }
}
