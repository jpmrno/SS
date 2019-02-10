package ar.edu.itba.ss.model.generator;

import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Road;
import ar.edu.itba.ss.util.VehicleType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.Pair;

public class VehicleGenerator {

  private static int LAST_ID = 0;

  private final Random random = ThreadLocalRandom.current();
  private final ToIntFunction<VehicleType> maxVelocityFunction;

  private VehicleGenerator(final ToIntFunction<VehicleType> maxVelocityFunction) {
    this.maxVelocityFunction = maxVelocityFunction;
  }

  public static VehicleGenerator simpleMaxVelocity(final int maxVelocity) {
    return new VehicleGenerator(pt -> maxVelocity);
  }

  public static VehicleGenerator simpleMaxVelocity(final int maxVelocity, final double sigma) {
    final NormalDistribution normalDistribution = new NormalDistribution(0, sigma * sigma);
    return new VehicleGenerator(pt -> (int) (maxVelocity * normalDistribution.sample()));
  }

  public static VehicleGenerator perTypeMaxVelocity() {
    return new VehicleGenerator(VehicleType::maxVelocity);
  }

  public static VehicleGenerator perTypeMaxVelocity(final double sigma) {
    final NormalDistribution normalDistribution = new NormalDistribution(0, sigma * sigma);
    double sample = Math.abs(normalDistribution.sample() / (3 * sigma));
    sample = sample > 1 ? 1 : sample;

    double factor;
    if(sample < 0.33){
      factor = 0.5;
    } else if (sample < 0.66){
      factor = 0.75;
    } else {
      factor = 1;
    }

    return new VehicleGenerator(pt -> (int) (pt.maxVelocity() * factor));
  }

  public int generateInitialVehicles(final Road road, int nVehicles) {
    int uninitializedVehicles = generateParticles(road, nVehicles, 0, road.lanes(), 0, road.laneLength(),
            null);
    road.particles().forEach(vehicle -> updateParticleWithProperties(vehicle, road));
    return uninitializedVehicles;
  }

  public int generate(final Road road, int nVehicles, final int fromRow, final int toRow, final int fromCol, final int toCol) {
    final Set<Particle> generated = new HashSet<>();
    final int uninitializedVehicles = generateParticles(road, nVehicles, fromRow, toRow, fromCol, toCol, generated);
    generated.forEach(p -> updateParticleWithProperties(p, road));
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

  private void updateParticleWithProperties(final Particle particle, final Road road) {
    final OptionalInt distanceOptional = road.distanceToNextParticle(particle);
    final int distance = distanceOptional.getAsInt();

    List<Pair<VehicleType, Double>> pairs = new ArrayList<>();
    for (int i = 0; i < road.getVehicleProbabilities().length; i++) {
      pairs.add(new Pair(road.getVehicleTypes()[i], road.getVehicleProbabilities()[i]));
    }

    VehicleType type = getRandomVehicleType(new EnumeratedDistribution(pairs));

    if (type.length() <= distance) {
      putParticleWithProperties(road, particle, type);
    } else {
      final List<VehicleType> types = previousLengths(type.length(), road.getVehicleTypes());
      for (final VehicleType otherType : types) {
        if (otherType.length() <= distance) {
          putParticleWithProperties(road, particle, otherType);
          return;
        }
      }
    }
  }

  private void putParticleWithProperties(final Road road, final Particle particle, final VehicleType type) {
    final int maxV = maxVelocityFunction.applyAsInt(type);
    final Particle newParticle = Particle.builder().from(particle)
            .maxVelocity(maxV)
            .velocity(random.nextInt(maxV + 1))
            .vehicleType(type)
            .build();
    road.replace(particle, newParticle);
  }

  private static VehicleType getRandomVehicleType(EnumeratedDistribution<VehicleType> vehiclesDistribution) {
    return vehiclesDistribution.sample();
  }

  private static List<VehicleType> previousLengths(final int lastLength, VehicleType[] vehicleTypes) {
    return Arrays.stream(vehicleTypes)
            .sorted((o1, o2) -> o2.length() - o1.length())
            .filter(v -> v.length() < lastLength)
            .collect(Collectors.toList());
  }
}
