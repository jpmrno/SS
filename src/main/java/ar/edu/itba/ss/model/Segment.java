package ar.edu.itba.ss.model;

import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.util.Either;

import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Consumer;

public interface Segment {

  int lanes();

  int laneLength();

  Map<Integer,Integer> maxVelocities();

  Set<Particle> particles();

  Either<Particle, ParticleWrapper>[][] getLanes();

  boolean isActualized();

  void setActualized(boolean actualized);

  void put(final Particle particle);

  void replace(final Particle particle, final Particle newParticle);

  Set<Particle> timeLapse();

  boolean isValidPosition(final int particleRow, final int particleCol, final int particleLength);

  boolean isValidPosition(final Particle particle);

  OptionalInt firstVehicleInLane(final int lane);

  OptionalInt lastVehicleInLane(final int lane);

  void incomingVehicle(Particle vehicle);

  void randomIncomingVehicle();


  // TODO: Falta un onParticleExit(callback) para avisarle a la union que se fue un auto o una parte de un auto
  // TODO: el callback debería tener algo para indicarle que tamaño del auto está entrando o si es el auto entero
}
