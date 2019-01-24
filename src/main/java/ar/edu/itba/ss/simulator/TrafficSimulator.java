package ar.edu.itba.ss.simulator;

import static java.lang.Math.max;
import static java.lang.Math.min;

import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.ParticleWrapper;
import ar.edu.itba.ss.model.criteria.Criteria;
import ar.edu.itba.ss.util.Either;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import com.sun.tools.internal.xjc.reader.xmlschema.ParticleBinder;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

public class TrafficSimulator implements Simulator {

  private static final Random RANDOM = ThreadLocalRandom.current();

  private static final double CELL_LENGTH = 2.5;
  private static final int[] VEHICLES = new int[]{1, 2, 3, 5};
  private static final double[] VEHICLES_PROBABILITY = new double[]{0.1, 0.7, 0.15, 0.05};
  //  private static final int[] VEHICLES = new int[]{1};
//  private static final double[] VEHICLES_PROBABILITY = new double[]{1};
  private static final EnumeratedIntegerDistribution vehiclesDistribution =
          new EnumeratedIntegerDistribution(VEHICLES, VEHICLES_PROBABILITY);

  private static int LAST_ID = 0;

  private final Either<Particle, ParticleWrapper>[][] roads;
  private final int vMax;
  private final double slowDownProbability;
  private Set<Particle> initParticles;

  public TrafficSimulator(final int nVehicles, final int lanes, final int length, final int vMax,
                          final double slowDownProbability) {
    this.roads = new Either[lanes][length];
    this.vMax = vMax;
    this.slowDownProbability = slowDownProbability;
    this.initParticles = buildParticles(roads, generateParticles(roads, nVehicles), vMax);
  }

  @Override
  public Set<Particle> simulate(final Criteria endCriteria, final ParticlesWriter writer) {
    Set<Particle> currentParticles = initParticles;
    long iteration = 0;
    try {
      writer.write(iteration, roads, currentParticles);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    while (!endCriteria.test(iteration++, currentParticles)) {
      // change lanes
      final Set<Particle> iterationParticles = new HashSet<>();
      for (final Particle particle : currentParticles) {
        final Particle newParticle = laneChange(particle, roads, vMax, particle.velocity());
        if (newParticle != null) {
          iterationParticles.add(newParticle);
          deleteVehicle(particle, roads);
          addVehicle(newParticle, roads);
        } else {
          iterationParticles.add(particle);
        }
      }

      currentParticles.clear();
      currentParticles.addAll(iterationParticles);
      iterationParticles.clear();

      // perform NaSch rules
      for (final Particle particle : currentParticles) {
        final OptionalInt distance = distanceToNextParticle(roads, particle);
        final int newVelocity = velocity(particle, distance.orElse(Integer.MAX_VALUE));
        final int[] newPosition = moveForward(particle, newVelocity, roads);
        iterationParticles.add(Particle.builder().from(particle)
                .velocity(newVelocity)
                .position(newPosition[0], newPosition[1])
                .build());
      }
      updateRoads(roads, currentParticles, iterationParticles);
      currentParticles = iterationParticles;
      try {
        writer.write(iteration, roads, currentParticles);
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }
    }
    return currentParticles;
  }

  private void updateRoads(final Either<Particle, ParticleWrapper>[][] roads, final Set<Particle> oldVehicles,
                           final Set<Particle> newVehicles) {
    for (final Particle oldVehicle : oldVehicles) {
      deleteVehicle(oldVehicle, roads);
    }
    final Iterator<Particle> it = newVehicles.iterator();
    while (it.hasNext()) {
      final Particle newVehicle = it.next();
      if (isInsideRoad(roads, newVehicle)) {
        addVehicle(newVehicle, roads);
      } else {
        it.remove();
        // TODO: generate new vehicle
      }
    }
  }

  private void deleteVehicle(final Particle vehicle, final Either<Particle, ParticleWrapper>[][] roads) {
    for (int i = vehicle.col(); i < roads[0].length && i < vehicle.col() + vehicle.length(); i++) {
      roads[vehicle.row()][i] = null;
    }
  }

  private void addVehicle(final Particle vehicle, final Either<Particle, ParticleWrapper>[][] roads) {
    final ParticleWrapper particleWrapper = ParticleWrapper.of(vehicle);
    if (roads[vehicle.row()][vehicle.col()] != null) {
      throw new IllegalStateException("Crash at: " + vehicle.row() + " - " + vehicle.col());
    }
    roads[vehicle.row()][vehicle.col()] = Either.value(vehicle);
    for (int i = vehicle.col() + 1; i < roads[0].length && i < vehicle.col() + vehicle.length(); i++) {
      if (roads[vehicle.row()][i] != null) {
        throw new IllegalStateException("Crash at: " + vehicle.row() + " - " + i);
      }
      roads[vehicle.row()][i] = Either.alternative(particleWrapper);
    }
  }

  private Set<Particle> generateParticles(final Either<Particle, ParticleWrapper>[][] road,
                                          final int nVehicles) {
    final Set<Particle> particles = new HashSet<>();
    int vehicles = 0;
    while (vehicles != nVehicles) {
      final int row = RANDOM.nextInt(road.length);
      final int col = RANDOM.nextInt(road[0].length);
      if (road[row][col] == null) {
        vehicles++;
        final Particle particle = Particle.builder()
                .id(++LAST_ID)
                .position(row, col)
                .build();
        particles.add(particle);
        road[row][col] = Either.value(particle);
      }
    }
    return particles;
  }

  private Set<Particle> buildParticles(final Either<Particle, ParticleWrapper>[][] road,
                                       final Set<Particle> particles, final int vMax) {
    final Set<Particle> newParticles = new HashSet<>();
    for (final Particle particle : particles) {
      int length = getRandomVehicleLength();
      if (isValidPosition(road, particle, length)) {
        newParticles.add(updateParticleWithProperties(road, particle, length, vMax));
      } else {
        final int[] lengths = previousLengths(length);
        for (final int otherLength : lengths) {
          if (isValidPosition(road, particle, otherLength)) {
            newParticles.add(updateParticleWithProperties(road, particle, otherLength, vMax));
            break;
          }
        }
      }
    }
    return newParticles;
  }

  private Particle updateParticleWithProperties(final Either<Particle, ParticleWrapper>[][] roads,
                                                final Particle particle, final int length, final int vMax) {
    final OptionalInt distance = distanceToNextParticle(roads, particle);
    final Particle newParticle = Particle.builder().from(particle)
            .velocity(RANDOM.nextInt(min(vMax, distance.orElse(Integer.MAX_VALUE) - length) + 1))
            .length(length)
            .build();
    roads[particle.row()][particle.col()] = Either.value(newParticle);
    for (int nextCol = particle.col() + 1; nextCol < particle.col() + length; nextCol++) {
      roads[particle.row()][nextCol] = Either.alternative(ParticleWrapper.of(newParticle));
    }
    return newParticle;
  }

  private static int getRandomVehicleLength() {
    return vehiclesDistribution.sample();
  }

  private boolean isInsideRoad(final Either<Particle, ParticleWrapper>[][] road, final int row, final int col) {
    return row >= 0 && col >= 0 && row < road.length && col < road[row].length;
  }

  private boolean isInsideRoad(final Either<Particle, ParticleWrapper>[][] road, final Particle particle) {
    return isInsideRoad(road, particle.row(), particle.col());
  }

  private boolean isValidPosition(final Either<Particle, ParticleWrapper>[][] road, final Particle particle,
                                  final int length) {
    final int particleRow = particle.row();
    final int particleCol = particle.col();
    final int roadLength = road[0].length;
    for (int i = 1; i < length; i++) {
      final int col = particleCol + i;
      if (col >= roadLength || road[particleRow][col] != null) {
        return false;
      }
    }
    return true;
  }

  private int[] previousLengths(final int lastLength) {
    return IntStream.of(VEHICLES)
            .boxed()
            .sorted(Comparator.reverseOrder())
            .filter(l -> l < lastLength)
            .mapToInt(i -> i)
            .toArray();
  }

  private int velocity(final Particle particle, final int distance) {
    int velocity = min(particle.velocity() + 1, vMax);
    velocity = min(distance - 1, velocity);
    if (RANDOM.nextDouble() <= slowDownProbability) {
      velocity = max(0, velocity - 1);
    }
    return velocity;
  }

  // me parece que ya no es necesario que devuelva int[]
  private int[] moveForward(final Particle particle, final int velocity, final Either<Particle, ParticleWrapper>[][] roads) {
    return new int[]{particle.row(), particle.col() + velocity};
  }

  private Particle laneChange(final Particle particle, final Either<Particle, ParticleWrapper>[][] roads,
                              final int precedingGap, final int successiveGap) {
    final int laneChange = laneChangeCriteria(particle, roads);
    if (laneChange != 0) {
      final int newLane = particle.row() + laneChange;
      final Particle newParticle = Particle.builder().from(particle)
              .row(newLane)
              .build();
      if (newLane >= 0 && newLane < roads.length && roads[newLane][particle.col()] == null) {
        final OptionalInt precedingDistance = distanceToPreviousParticle(roads, newParticle);
        final OptionalInt successiveDistance = distanceToNextParticle(roads, newParticle);
        if (!overlaps(particle, roads, newParticle.row(), newParticle.col())
                && precedingDistance.orElse(Integer.MAX_VALUE) >= precedingGap
                && successiveDistance.orElse(Integer.MAX_VALUE) >= successiveGap) {
          return newParticle;
        }
      }
    }
    return null;
  }

  private int laneChangeCriteria(final Particle particle, final Either<Particle, ParticleWrapper>[][] roads) {
    //TODO: cambiar el criterio
    if (RANDOM.nextBoolean()) {
      return 0;
    }
    return RANDOM.nextBoolean() ? 1 : -1;
  }

  private OptionalInt distanceToNextParticle(final Either<Particle, ParticleWrapper>[][] road,
                                             final Particle particle) {
    final int row = particle.row();
    final int col = particle.col();
    final int length = particle.length();
    for (int i = col + length; i < road[row].length; i++) {
      if (road[row][i] != null) {
        return OptionalInt.of(i - col - length + 1);
      }
    }
    return OptionalInt.empty();
  }

  private OptionalInt distanceToPreviousParticle(final Either<Particle, ParticleWrapper>[][] road,
                                                 final Particle particle) {
    final int row = particle.row();
    final int col = particle.col();
    for (int i = col - 1; i >= 0; i--) {
      if (road[row][i] != null) {
        return OptionalInt.of(col - i);
      }
    }
    return OptionalInt.empty();
  }

  private boolean overlaps(final Particle particle, final Either<Particle, ParticleWrapper>[][] road, final int fromRow, final int fromCol) {
    for (int col = fromCol; col < fromCol + particle.length() && col < road[0].length; col++) {
      if(road[fromRow][col] != null){
        return true;
      }
    }
    return false;
  }
}
