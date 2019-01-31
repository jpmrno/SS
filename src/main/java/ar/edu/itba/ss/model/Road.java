package ar.edu.itba.ss.model;

import static java.lang.Math.max;
import static java.lang.Math.min;

import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.util.Either;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class Road implements Segment {

  private static final Random RANDOM = ThreadLocalRandom.current();

  private final Either<Particle, ParticleWrapper>[][] lanes;
  private final TrafficLight trafficLight;
  private final int vMax;
  private Set<Particle> particles;
  private final double slowDownProbability;

  public Road(final int lanes, final int length, final TrafficLight trafficLight, final int vMax,
      final double slowDownProbability) {
    this.trafficLight = trafficLight;
    this.lanes = new Either[lanes][length];
    this.vMax = vMax;
    this.slowDownProbability = slowDownProbability;
    this.particles = new HashSet<>();
  }

  @Override
  public int lanes() {
    return lanes.length;
  }

  @Override
  public int laneLength() {
    return lanes[0].length;
  }

  @Override
  public int vMax() {
    return vMax;
  }

  @Override
  public Set<Particle> particles() {
    return Collections.unmodifiableSet(new HashSet<>(particles));
  }

  @Override
  public void put(final Particle particle) {
    final ParticleWrapper particleWrapper = ParticleWrapper.of(particle);
    if (lanes[particle.row()][particle.col()] != null) {
      throw new IllegalStateException("Crash at: " + particle.row() + " - " + particle.col());
    }
    lanes[particle.row()][particle.col()] = Either.value(particle);
    for (int i = particle.col() + 1; i < lanes[0].length && i < particle.col() + particle.length(); i++) {
      if (lanes[particle.row()][i] != null) {
        throw new IllegalStateException("Crash at: " + particle.row() + " - " + i);
      }
      lanes[particle.row()][i] = Either.alternative(particleWrapper);
    }
    particles.add(particle);
  }

  private void remove(final Particle particle) {
    for (int i = particle.col(); i < laneLength() && i < particle.col() + particle.length(); i++) {
      lanes[particle.row()][i] = null;
    }
    particles.remove(particle);
  }

  @Override
  public void replace(final Particle particle, final Particle newParticle) {
    remove(particle);
    put(newParticle);
  }

  @Override
  public boolean isValidPosition(final Particle particle) {
    return isValidPosition(particle.row(), particle.col(), particle.length());
  }

  @Override
  public boolean isValidPosition(final int particleRow, final int particleCol, final int particleLength) {
    if (particleRow < 0 || particleRow >= lanes() || particleCol < 0) {
      return false;
    }
    for (int i = 0; i < particleLength; i++) {
      final int col = particleCol + i;
      if (col >= laneLength() || lanes[particleRow][col] != null) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Set<Particle> timeLapse(final long iteration, final ParticlesWriter writer) {
    // traffic light
    trafficLight.timeLapse();

    // change lanes
    for (final Particle particle : particles()) {
      final Particle newParticle = laneChange(particle, vMax, particle.velocity());
      if (newParticle != null) {
        replace(particle, newParticle);
      }
    }

    // perform NaSch rules
    for (final Particle particle : particles()) {
      final OptionalInt distance = distanceToNextParticle(particle);
      final int newVelocity = velocity(particle, distance.orElse(Integer.MAX_VALUE));
      final int[] newPosition = moveForward(particle, newVelocity);
      final Particle newParticle = Particle.builder().from(particle)
          .velocity(newVelocity)
          .position(newPosition[0], newPosition[1])
          .build();
      if (isInsideRoad(newParticle)) {
        replace(particle, newParticle);
      } else {
        remove(particle);
      }
    }

    print(writer, iteration);
    return particles;
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
  private int[] moveForward(final Particle particle, final int velocity) {
    return new int[]{particle.row(), particle.col() + velocity};
  }

  private Particle laneChange(final Particle particle, final int precedingGap, final int successiveGap) {
    final int laneChange = laneChangeCriteria(particle, lanes);
    if (laneChange == 0) {
      return null;
    }
    final int newLane = particle.row() + laneChange;
    final Particle newParticle = Particle.builder().from(particle)
        .row(newLane)
        .build();
    if (newLane >= 0 && newLane < lanes() && lanes[newLane][particle.col()] == null) {
      final OptionalInt precedingDistance = distanceToPreviousParticle(newParticle);
      final OptionalInt successiveDistance = distanceToNextParticle(newParticle);
      if (!overlaps(particle, lanes, newParticle.row(), newParticle.col())
          && precedingDistance.orElse(Integer.MAX_VALUE) >= precedingGap
          && successiveDistance.orElse(Integer.MAX_VALUE) >= successiveGap) {
        return newParticle;
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

  public OptionalInt distanceToNextParticle(final Particle particle) {
    final int row = particle.row();
    final int col = particle.col();
    final int length = particle.length();
    for (int i = col + length; i < lanes[row].length; i++) {
      if (lanes[row][i] != null) {
        return OptionalInt.of(i - col - length + 1);
      }
    }
    if (col + length - 1 >= lanes[row].length) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(lanes[row].length - col - length + 1 + trafficLight.currentStatus().additionalDistance());
  }

  public OptionalInt distanceToPreviousParticle(final Particle particle) {
    final int row = particle.row();
    final int col = particle.col();
    for (int i = col - 1; i >= 0; i--) {
      if (lanes[row][i] != null) {
        return OptionalInt.of(col - i);
      }
    }
    return OptionalInt.empty();
  }

  private boolean overlaps(final Particle particle, final Either<Particle, ParticleWrapper>[][] road, final int fromRow,
      final int fromCol) {
    for (int col = fromCol; col < fromCol + particle.length() && col < road[0].length; col++) {
      if (road[fromRow][col] != null) {
        return true;
      }
    }
    return false;
  }

  private boolean isInsideRoad(final int row, final int col) {
    return row >= 0 && col >= 0 && row < lanes.length && col < lanes[row].length;
  }

  private boolean isInsideRoad(final Particle particle) {
    return isInsideRoad(particle.row(), particle.col());
  }

  public void print(final ParticlesWriter writer, final long iteration) {
    try {
      System.out.println("Traffic light: " + trafficLight.currentStatus());
      writer.write(iteration, lanes, particles);
    } catch (final IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public interface ParticleGenerator {

    void generate(final Road road);
  }
}
