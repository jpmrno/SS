package ar.edu.itba.ss.model;

import static java.lang.Math.max;
import static java.lang.Math.min;

import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.util.Either;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class Road implements Segment {

  private static final Random RANDOM = ThreadLocalRandom.current();

  private final Either<Particle, ParticleWrapper>[][] lanes;
  private final TrafficLight trafficLight;
  private final int vMax;
  private Set<Particle> particles;
  private final double slowDownProbability;
  private Segment prevSegment;
  private Segment nextSegment;
  private boolean actualized = false;

  private static final Comparator<Particle> VEHICLE_COMPARATOR = (o1, o2) -> {
    final int rowCmp = o1.row() - o2.row();

    if(rowCmp != 0){
      return rowCmp;
    }

    final int colCmp = o1.col() - o2.col();

    if(colCmp != 0){
      return colCmp;
    }

    return o1.id() - o2.id();
  };

  public Road(final int lanes, final int length, final TrafficLight trafficLight, final int vMax,
              final double slowDownProbability, Segment prevSegment, Segment nextSegment) {
    this.trafficLight = trafficLight;
    this.prevSegment = prevSegment;
    this.nextSegment = nextSegment;
    this.lanes = new Either[lanes][length];
    this.vMax = vMax;
    this.slowDownProbability = slowDownProbability;
    this.particles = new TreeSet<>(VEHICLE_COMPARATOR);
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
  public boolean isActualized() {
    return actualized;
  }

  @Override
  public void setActualized(boolean actualized) {
    this.actualized = actualized;
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
    if(isInsideRoad(particle.row(), particle.col())) {
      for (int i = particle.col(); i < laneLength() && i < particle.col() + particle.length(); i++) {
        lanes[particle.row()][i] = null;
      }
      particles.remove(particle);
    }
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
  public OptionalInt firstVehicleInLane(int lane) {
      return distanceToNextParticle(lane, 0, 0);
  }

  @Override
  public OptionalInt lastVehicleInLane(int lane) {
    return distanceToPreviousParticle(lane, laneLength());
  }

  @Override
  public void incomingVehicle(Particle vehicle) {
    if(actualized){
      put(vehicle);
    } else {
      particles.add(vehicle);
    }
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
        if(nextSegment != null){
          final Particle vehicleForNextSegment = Particle.builder().from(newParticle)
              .col(newParticle.col() - this.laneLength())
              .build();
          nextSegment.incomingVehicle(vehicleForNextSegment);
        }
      }
    }

    print(writer, iteration);
    return particles;
  }

  public void setNextSegment(Segment segment){
    this.nextSegment = segment;
  }

  public void setPreviousSegment(Segment segment) {
    this.prevSegment = segment;
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
    return 0;
//    if (RANDOM.nextBoolean()) {
//      return 0;
//    }
//    return RANDOM.nextBoolean() ? 1 : -1;
  }

  private OptionalInt distanceToNextParticle(final int lane, final int col, final int vehicleLength){
    for (int i = col + vehicleLength; i < lanes[lane].length; i++) {
      if (lanes[lane][i] != null) {
        return OptionalInt.of(i - col - vehicleLength + 1);
      }
    }

    int distanceToEndOfSegment = laneLength() - col - vehicleLength + 1;

    if (distanceToEndOfSegment <= 0) {
      if(nextSegment == null){
        return OptionalInt.empty();
      }

      OptionalInt distanceInNextSegment = nextSegment.firstVehicleInLane(lane);

      if(distanceInNextSegment.isPresent()){
        return OptionalInt.of(lanes[lane].length - col - vehicleLength + distanceInNextSegment.getAsInt() + distanceToEndOfSegment);
      }

      return OptionalInt.empty();
    }

    int distanceToEndOfSegmentWithTrafficLight = distanceToEndOfSegment + trafficLight.currentStatus().additionalDistance();

    if(nextSegment == null){
      return OptionalInt.of(distanceToEndOfSegmentWithTrafficLight);
    }

    return OptionalInt.of(Math.min(distanceToEndOfSegmentWithTrafficLight, distanceToEndOfSegment + nextSegment.firstVehicleInLane(lane).orElse(vMax)));
  }

  public OptionalInt distanceToNextParticle(final Particle particle) {
    return distanceToNextParticle(particle.row(), particle.col(), particle.length());

  }

  private OptionalInt distanceToPreviousParticle(final int lane, final int col) {
    for (int i = col - 1; i >= 0; i--) {
      if (lanes[lane][i] != null) {
        return OptionalInt.of(col - i);
      }
    }

    if(prevSegment == null){
      return OptionalInt.empty();
    }

    OptionalInt distanceInPrevSegment = prevSegment.lastVehicleInLane(lane);

    if(distanceInPrevSegment.isPresent()){
      return OptionalInt.of(col + distanceInPrevSegment.getAsInt());
    }

    return OptionalInt.empty();
  }

  private OptionalInt distanceToPreviousParticle(final Particle particle) {
    return distanceToPreviousParticle(particle.row(), particle.col());
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
