package ar.edu.itba.ss.model;

import static java.lang.Math.max;
import static java.lang.Math.min;

import ar.edu.itba.ss.model.generator.VehicleGenerator;
import ar.edu.itba.ss.util.Either;
import ar.edu.itba.ss.util.VehicleType;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class Road {

  private static final Random RANDOM = ThreadLocalRandom.current();

  private final Either<Particle, ParticleWrapper>[][] lanes;
  private final TrafficLight trafficLight;
  private Set<Particle> particles;
  private final double slowDownProbability;
  private Road prevRoad;
  private Road nextRoad;
  private boolean actualized = false;
  private Set<Particle> alreadyMoved;
  private final VehicleType[] vehicleTypes;
  private final double[] vehicleProbabilities;
  private Consumer<Particle> onExit;
  private final VehicleGenerator vehicleGenerator;
  private int vehiclesToBeCreated = 0;
  private final BiFunction<Particle, Road, List<Particle>> laneChanger;

  public Road(final int lanes, final int length, final TrafficLight trafficLight, final double slowDownProbability,
              VehicleType[] vehicleTypes, double[] vehicleProbabilities, Road prevRoad, Road nextRoad,
              Consumer<Particle> onExit, BiFunction<Particle, Road, List<Particle>> laneChanger,
              final VehicleGenerator vehicleGenerator) {
    this.trafficLight = trafficLight;
    this.prevRoad = prevRoad;
    this.nextRoad = nextRoad;
    this.onExit = onExit;
    this.laneChanger = laneChanger;
    this.lanes = new Either[lanes][length];
    this.slowDownProbability = slowDownProbability;
    this.particles = new HashSet<>();
    this.alreadyMoved = new HashSet<>();
    this.vehicleTypes = vehicleTypes;
    this.vehicleProbabilities = vehicleProbabilities;
    this.vehicleGenerator = vehicleGenerator;
  }

  public int lanes() {
    return lanes.length;
  }

  public int laneLength() {
    return lanes[0].length;
  }

  public Set<Particle> particles() {
    return Collections.unmodifiableSet(new HashSet<>(particles));
  }

  public Either<Particle, ParticleWrapper>[][] getLanes() {
    return lanes;
  }

  public boolean isActualized() {
    return actualized;
  }

  public void setActualized(boolean actualized) {
    this.actualized = actualized;
  }

  public void put(final Particle particle) {
    final ParticleWrapper particleWrapper = ParticleWrapper.of(particle);
    if (lanes[particle.row()][particle.col()] != null) {
      throw new IllegalStateException("Crash at: " + particle.row() + " - " + particle.col());
    }
    lanes[particle.row()][particle.col()] = Either.value(particle);
    for (int i = particle.col() + 1; i < lanes[0].length && i < particle.col() + particle.vehicleType().length(); i++) {
      if (lanes[particle.row()][i] != null) {
        throw new IllegalStateException("Crash at: " + particle.row() + " - " + i);
      }
      lanes[particle.row()][i] = Either.alternative(particleWrapper);
    }
    particles.add(particle);
  }

  private void remove(final Particle particle) {
    if (particles.contains(particle)) {
      if (isInsideRoad(particle.row(), particle.col())) {
        for (int i = particle.col(); i < laneLength() && i < particle.col() + particle.vehicleType().length(); i++) {
          lanes[particle.row()][i] = null;
        }
      }
      particles.remove(particle);
    }
  }

  public void replace(final Particle particle, final Particle newParticle) {
    remove(particle);
    if (isInsideRoad(newParticle)) {
      put(newParticle);
    }
  }

  public boolean isValidPosition(final Particle particle) {
    return isValidPosition(particle.row(), particle.col(), particle.vehicleType().length());
  }

  public OptionalInt firstVehicleInLane(int lane) {
    return distanceToNextParticle(lane, 0, 0);
  }

  public OptionalInt lastVehicleInLane(int lane) {
    return distanceToPreviousParticle(lane, laneLength());
  }

  public void incomingVehicle(Particle vehicle) {
    if (!actualized) {
      alreadyMoved.add(vehicle);
    }

    put(vehicle);
  }

  public boolean randomIncomingVehicle() {
    return vehicleGenerator.generate(this, 1, 0, lanes(), 0, 1) != 1;
  }

  public boolean randomVehicle() {
    return vehicleGenerator.generate(this, 1, 0, lanes(), 0, laneLength()) != 1;
  }

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
        final int newVelocity = velocity(particle, distance.orElse(particle.maxVelocity()));
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

    while (vehiclesToBeCreated > 0 && randomIncomingVehicle()) {
      vehiclesToBeCreated--;
    }

    return particles;
  }

  public void setNextRoad(Road road) {
    this.nextRoad = road;
  }

  public void setPreviousRoad(Road road) {
    this.prevRoad = road;
  }

  private int velocity(final Particle particle, final int distance) {
    int velocity = min(particle.velocity() + 1, particle.maxVelocity());
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
    for (Particle newParticle : laneChanger.apply(particle, this)) {
      if (isInsideRoad(newParticle) && isLaneChangePossible(newParticle, newParticle.maxVelocity(),
          newParticle.velocity())) {
        return newParticle;
      }
    }

    return null;
  }

  private boolean isLaneChangePossible(final Particle vehicle, final int precedingGap, final int successiveGap) {
    final OptionalInt precedingDistance = distanceToPreviousParticle(vehicle);
    final OptionalInt successiveDistance = distanceToNextParticle(vehicle);

    return !overlaps(vehicle.row(), vehicle.col(), vehicle)
        && precedingDistance.orElse(Integer.MAX_VALUE) >= precedingGap
        && successiveDistance.orElse(Integer.MAX_VALUE) >= successiveGap;
  }

  public OptionalInt distanceToNextParticle(final int lane, final int col, final int length) {
    for (int i = col + length; i < lanes[lane].length; i++) {
      if (lanes[lane][i] != null) {
        return OptionalInt.of(i - col - length + 1);
      }
    }

    int distanceToEndOfSegment = laneLength() - col - length + 1;
    int distanceToEndOfSegmentWithTrafficLight =
        distanceToEndOfSegment + trafficLight.currentStatus().additionalDistance();

    if (nextRoad == null) {
      return OptionalInt.of(distanceToEndOfSegmentWithTrafficLight);
    }

    return OptionalInt.of(Math.min(distanceToEndOfSegmentWithTrafficLight,
        distanceToEndOfSegment - 1 + nextRoad.firstVehicleInLane(lane).orElse(Integer.MAX_VALUE)));
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

    if (prevRoad == null) {
      return OptionalInt.empty();
    }

    OptionalInt distanceInPrevSegment = prevRoad.lastVehicleInLane(lane);

    if (distanceInPrevSegment.isPresent()) {
      return OptionalInt.of(col + distanceInPrevSegment.getAsInt());
    }

    return OptionalInt.empty();
  }

  private OptionalInt distanceToPreviousParticle(final Particle particle) {
    return distanceToPreviousParticle(particle.row(), particle.col());
  }

  private boolean overlaps(final int fromRow, final int fromCol, final Particle particle) {
    final VehicleType vehicleType = particle.vehicleType();

    if (fromCol < 0 && prevRoad != null && lastVehicleInLane(fromRow).orElse(particle.maxVelocity()) <= -fromCol) {
      return true;
    }

    if (fromCol + vehicleType.length() - 1 >= laneLength() && nextRoad != null
        && firstVehicleInLane(fromRow).orElse(particle.maxVelocity()) <= laneLength() - fromCol - vehicleType.length()) {
      return true;
    }

    for (int col = max(fromCol, 0); col < fromCol + vehicleType.length() && col < lanes[0].length; col++) {
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

  public VehicleType[] getVehicleTypes() {
    return vehicleTypes;
  }

  public double[] getVehicleProbabilities() {
    return vehicleProbabilities;
  }

  public void setOnExit(Consumer<Particle> onExit) {
    this.onExit = onExit;
  }

  public void addVehiclesToBeCreated(int amount) {
    this.vehiclesToBeCreated += amount;
  }
}
