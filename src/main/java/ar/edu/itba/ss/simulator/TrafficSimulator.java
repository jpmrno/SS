package ar.edu.itba.ss.simulator;

import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.ParticleWrapper;
import ar.edu.itba.ss.model.criteria.Criteria;
import ar.edu.itba.ss.util.Either;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

public class TrafficSimulator implements Simulator {

  private static final Random random = ThreadLocalRandom.current();

  private static final double CELL_LENGTH = 2.5;
  private static final int[] VEHICLES = new int[]{1};
  private static final double[] VEHICLES_PROBABILITY = new double[]{1};
  private static final EnumeratedIntegerDistribution vehiclesDistribution =
      new EnumeratedIntegerDistribution(VEHICLES, VEHICLES_PROBABILITY);

  private final int nVehicles;
  private final Either<Particle, ParticleWrapper>[][] road;
  private final int vMax;
  private Set<Particle> initParticles;

  public TrafficSimulator(final int nVehicles, final int lanes, final int length, final int vMax) {
    this.nVehicles = nVehicles;
    this.road = new Either[lanes][length];
    this.vMax = vMax;
    this.initParticles = new HashSet<>();

    fillParticles(initParticles, road, nVehicles, lanes, length, vMax);
  }

  @Override
  public Set<Particle> simulate(final Criteria endCriteria, final ParticlesWriter writer) {
    return null;
  }

  private void fillParticles(final Set<Particle> initParticles, final Either<Particle, ParticleWrapper>[][] road,
      final int nVehicles, final int lanes, final int length, final int vMax) {
    int vehicles = 0;
    while (vehicles != nVehicles) {
      final int row = random.nextInt(lanes);
      final int col = random.nextInt(length);
      if (road[row][col] == null) {
        vehicles++;
        final Particle particle = Particle.builder()
            .position(row, col)
            .velocity(random.nextInt(vMax + 1))
            .build();
        road[row][col] = Either.value(particle);
        initParticles.add(particle);
      }
    }
  }

  private Set<Particle> setVehicleTypes(final Set<Particle> initParticles, final Either<Particle, ParticleWrapper>[][] road) {
    final Set<Particle> particles = new HashSet<>();
    for (final Particle particle : initParticles) {
      int length = getRandomVehicleLength();
      if (isValidPosition(road, particle, length)) {
        setParticleWithLength(road, particles, particle, length);
      }
    }
    return particles;
  }

  private void setParticleWithLength(final Either<Particle, ParticleWrapper>[][] road, final Set<Particle> particles,
      final Particle particle, final int length) {
    final Particle particleWithLength = Particle.builder().from(particle).length(length).build();
    road[particle.row()][particle.col()] = Either.value(particleWithLength);
    particles.add(particleWithLength);
    for (int nextCol = particle.col() + 1; nextCol < particle.col() + length; nextCol++) {
      road[particle.row()][nextCol] = Either.alternative(ParticleWrapper.of(particleWithLength));
    }
  }

  private static int getRandomVehicleLength() {
    return vehiclesDistribution.sample();
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
}
