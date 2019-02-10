package ar.edu.itba.ss.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Tendency {

  private final double randomLaneChangeProbability;
  private static final Random RANDOM = ThreadLocalRandom.current();

  public Tendency(double randomLaneChangeProbability) {
    this.randomLaneChangeProbability = randomLaneChangeProbability;
  }

  public List<Particle> tendencyToLeft(Particle vehicle, Road road) {
    final List<Particle> ret = new ArrayList<>();
    final Particle toLeft = toLeft(vehicle, road);

    if (toLeft != null) {
      ret.add(toLeft);
    }

    return addRandomMovement(vehicle, ret);
  }

  public List<Particle> tendencyToAnywhere(Particle vehicle, Road road) {
    final List<Particle> ret = new ArrayList<>();
    final Particle toLeft = toLeft(vehicle, road);
    final Particle toRight = toRight(vehicle, road);

    if (RANDOM.nextBoolean()) {
      if (toLeft != null) {
        ret.add(toLeft);
      }
      if (toRight != null) {
        ret.add(toRight);
      }
    } else {
      if (toRight != null) {
        ret.add(toRight);
      }
      if (toLeft != null) {
        ret.add(toLeft);
      }
    }

    return addRandomMovement(vehicle, ret);
  }

  private Particle toLeft(Particle vehicle, Road road) {
    if (vehicle.row() > 0) {
      final int distanceToNextVehicle = road.distanceToNextParticle(vehicle).orElse(vehicle.maxVelocity());
      final int distanceToNextVehicleLeft = road.distanceToNextParticle(vehicle.row() - 1, vehicle.col(), vehicle.length())
          .orElse(vehicle.maxVelocity());
      if (distanceToNextVehicle < vehicle.velocity() + 1 && distanceToNextVehicleLeft >= vehicle.velocity() + 1) {
        return Particle.builder().from(vehicle)
            .row(vehicle.row() - 1)
            .build();
      }
    }
    return null;
  }

  private Particle toRight(Particle vehicle, Road road) {
    if (vehicle.row() < road.lanes() - 1) {
      final int distanceToNextVehicle = road.distanceToNextParticle(vehicle).orElse(vehicle.maxVelocity());
      final int distanceToNextVehicleLeft =
          road.distanceToNextParticle(vehicle.row() + 1, vehicle.col(), vehicle.length()).orElse(vehicle.maxVelocity());
      if (distanceToNextVehicle < vehicle.velocity() + 1 && distanceToNextVehicleLeft >= vehicle.velocity() + 1) {
        return Particle.builder().from(vehicle)
            .row(vehicle.row() + 1)
            .build();
      }
    }
    return null;
  }

  private List<Particle> addRandomMovement(Particle vehicle, List<Particle> changes) {
    if (RANDOM.nextDouble() <= randomLaneChangeProbability) {
      changes.add(Particle.builder().from(vehicle)
          .row(vehicle.row() + (RANDOM.nextBoolean() ? 1 : -1))
          .build());
    }

    return changes;
  }
}
