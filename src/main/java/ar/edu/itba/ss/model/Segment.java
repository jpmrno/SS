package ar.edu.itba.ss.model;

import ar.edu.itba.ss.io.writer.ParticlesWriter;

import java.util.OptionalInt;
import java.util.Set;

public interface Segment {

  int lanes();

  int laneLength();

  int vMax();

  Set<Particle> particles();

  boolean isActualized();

  void setActualized(boolean actualized);

  void put(final Particle particle);

  void replace(final Particle particle, final Particle newParticle);

  Set<Particle> timeLapse(final long iteration, final ParticlesWriter writer);

  boolean isValidPosition(final int particleRow, final int particleCol, final int particleLength);

  boolean isValidPosition(final Particle particle);

  OptionalInt firstVehicleInLane(final int lane);

  OptionalInt lastVehicleInLane(final int lane);

  void incomingVehicle(Particle vehicle);



  // TODO: Falta un onParticleExit(callback) para avisarle a la union que se fue un auto o una parte de un auto
  // TODO: el callback debería tener algo para indicarle que tamaño del auto está entrando o si es el auto entero
  // TODO: Y falta un incomingParticle(...) o algo asi para avisarle q esta entrando un auto
  // TODO: Tambien falta una forma de ver el proximo auto en el proximo segmento, se puede hacer que reciba
  // TODO: en el constructor alguna funcion para pedirlo y cuando da OptionalInt.empty() en distanceToNextParticle()
  // TODO: buscar ahí a ver que valor da.
}
