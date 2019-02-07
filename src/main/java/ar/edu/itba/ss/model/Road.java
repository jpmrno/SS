package ar.edu.itba.ss.model;

import ar.edu.itba.ss.model.generator.VehicleGenerator;
import ar.edu.itba.ss.util.Either;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import static java.lang.Math.max;
import static java.lang.Math.min;

public final class Road implements Segment {

  private static final Random RANDOM = ThreadLocalRandom.current();

  private final Either<Particle, ParticleWrapper>[][] lanes;
  private final Map<Integer,Integer> maxVelocities;
  private final TrafficLight trafficLight;
  private Set<Particle> particles;
  private final double slowDownProbability;
  private Segment prevSegment;
  private Segment nextSegment;
  private boolean actualized = false;
  private Set<Particle> alreadyMoved;
  private final int[] vehicleLengths;
  private final double[] vehicleProbabilities;
  private Consumer<Particle> onExit;
  private final VehicleGenerator vehicleGenerator;

  public Road(final int lanes, final int length, final TrafficLight trafficLight, final Map<Integer, Integer> maxVelocities,
              final double slowDownProbability, int[] vehicleLengths, double[] vehicleProbabilities, Segment prevSegment, Segment nextSegment, Consumer<Particle> onExit) {
    this.trafficLight = trafficLight;
    this.prevSegment = prevSegment;
    this.nextSegment = nextSegment;
    this.onExit = onExit;
    this.lanes = new Either[lanes][length];
    this.maxVelocities = maxVelocities;
    this.slowDownProbability = slowDownProbability;
    this.particles = new HashSet<>();
    this.alreadyMoved = new HashSet<>();
    this.vehicleLengths = vehicleLengths;
    this.vehicleProbabilities = vehicleProbabilities;
    this.vehicleGenerator = VehicleGenerator.getInstance();
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
  public Map<Integer, Integer> maxVelocities() {
    return maxVelocities;
  }

  @Override
  public Set<Particle> particles() {
    return Collections.unmodifiableSet(new HashSet<>(particles));
  }

  @Override
  public Either<Particle, ParticleWrapper>[][] getLanes() {
    return lanes;
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
    if (isInsideRoad(particle.row(), particle.col())) {
      for (int i = particle.col(); i < laneLength() && i < particle.col() + particle.length(); i++) {
        lanes[particle.row()][i] = null;
      }
    }
    particles.remove(particle);
  }

  @Override
  public void replace(final Particle particle, final Particle newParticle) {
    remove(particle);
    if (isInsideRoad(newParticle)) {
      put(newParticle);
    }
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
    if (!actualized) {
      alreadyMoved.add(vehicle);
    }

    put(vehicle);
  }

  @Override
  public void randomIncomingVehicle() {
    vehicleGenerator.generate(this, 1, 0, lanes(), 0, 1);
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
  public Set<Particle> timeLapse() {
    // traffic light
    trafficLight.timeLapse();

    // change lanes
    for (final Particle particle : particles()) {
      if (!alreadyMoved.contains(particle)) {
        final Particle newParticle = laneChange(particle);
        if (newParticle != null) {
          replace(particle, newParticle);
        }
      }
    }

    Set<Particle[]> newParticles = new HashSet<>();

    // perform NaSch rules
    for (final Particle particle : particles()) {
      if (!alreadyMoved.contains(particle)) {
        final OptionalInt distance = distanceToNextParticle(particle);
        final int newVelocity = velocity(particle, distance.orElse(Integer.MAX_VALUE));
        final int[] newPosition = moveForward(particle, newVelocity);
        final Particle newParticle = Particle.builder().from(particle)
                .velocity(newVelocity)
                .position(newPosition[0], newPosition[1])
                .build();

        newParticles.add(new Particle[]{particle, newParticle});

        if (!isInsideRoad(newParticle) && onExit != null) {
          onExit.accept(newParticle);
        }
      }
    }

    newParticles.forEach(p -> replace(p[0], p[1]));

    alreadyMoved.clear();

    return particles;
  }

  public void setNextSegment(Segment segment) {
    this.nextSegment = segment;
  }

  public void setPreviousSegment(Segment segment) {
    this.prevSegment = segment;
  }

  private int velocity(final Particle particle, final int distance) {
    int velocity = min(particle.velocity() + 1, maxVelocities.get(particle.row()));
    velocity = min(distance - 1, velocity);
    if (RANDOM.nextDouble() <= slowDownProbability && velocity > 1) {
      velocity = max(0, velocity - 1);
    }
    return velocity;
  }

  // me parece que ya no es necesario que devuelva int[]
  private int[] moveForward(final Particle particle, final int velocity) {
    return new int[]{particle.row(), particle.col() + velocity};
  }

  private Particle laneChange(final Particle particle) {
    final int laneChange = laneChangeCriteria(particle, lanes);

    if (laneChange == 0) {
      return null;
    }

    final int newLane = particle.row() + laneChange;
    final Particle newParticle = Particle.builder().from(particle)
            .row(newLane)
            .build();

    if(isInsideRoad(newParticle) && isLaneChangePossible(newParticle,  maxVelocities.get(newParticle.row()), newParticle.velocity())) {
      return newParticle;
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

  private boolean isLaneChangePossible(final Particle vehicle, final int precedingGap, final int successiveGap) {
    final OptionalInt precedingDistance = distanceToPreviousParticle(vehicle);
    final OptionalInt successiveDistance = distanceToNextParticle(vehicle);

    return !overlaps(vehicle.row(), vehicle.col(), vehicle.length())
            && precedingDistance.orElse(Integer.MAX_VALUE) >= precedingGap
            && successiveDistance.orElse(Integer.MAX_VALUE) >= successiveGap;
  }

  private OptionalInt distanceToNextParticle(final int lane, final int col, final int vehicleLength) {
    for (int i = col + vehicleLength; i < lanes[lane].length; i++) {
      if (isInsideRoad(lane, i) && lanes[lane][i] != null) {
        return OptionalInt.of(i - col - vehicleLength + 1);
      }
    }

    int distanceToEndOfSegment = laneLength() - col - vehicleLength + 1;

    if (distanceToEndOfSegment <= 0) {
      if (nextSegment == null) {
        return OptionalInt.empty();
      }

      OptionalInt distanceInNextSegment = nextSegment.firstVehicleInLane(lane);

      if (distanceInNextSegment.isPresent()) {
        return OptionalInt.of(lanes[lane].length - col - vehicleLength + distanceInNextSegment.getAsInt() + distanceToEndOfSegment);
      }

      return OptionalInt.empty();
    }

    int distanceToEndOfSegmentWithTrafficLight = distanceToEndOfSegment + trafficLight.currentStatus().additionalDistance();

    if (nextSegment == null) {
      return OptionalInt.of(distanceToEndOfSegmentWithTrafficLight);
    }

    return OptionalInt.of(Math.min(distanceToEndOfSegmentWithTrafficLight, distanceToEndOfSegment - 1 + nextSegment.firstVehicleInLane(lane).orElse(maxVelocities.get(lane))));
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

    if (prevSegment == null) {
      return OptionalInt.empty();
    }

    OptionalInt distanceInPrevSegment = prevSegment.lastVehicleInLane(lane);

    if (distanceInPrevSegment.isPresent()) {
      return OptionalInt.of(col + distanceInPrevSegment.getAsInt());
    }

    return OptionalInt.empty();
  }

  private OptionalInt distanceToPreviousParticle(final Particle particle) {
    return distanceToPreviousParticle(particle.row(), particle.col());
  }

  private boolean overlaps(final int fromRow,
                           final int fromCol, final int length) {

    if (fromCol < 0 && prevSegment != null && lastVehicleInLane(fromRow).orElse(maxVelocities.get(fromRow)) <= -fromCol) {
      return true;
    }

    if (fromCol + length - 1 >= laneLength() && nextSegment != null
            && firstVehicleInLane(fromRow).orElse(maxVelocities.get(fromRow)) <= laneLength() - fromCol - length) {
      return true;
    }

    for (int col = max(fromCol, 0); col < fromCol + length && col < lanes[0].length; col++) {
      if (lanes[fromRow][col] != null) {
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

  public int[] getVehicleLengths() {
    return vehicleLengths;
  }

  public double[] getVehicleProbabilities() {
    return vehicleProbabilities;
  }

  public void setOnExit(Consumer<Particle> onExit) {
    this.onExit = onExit;
  }
}
