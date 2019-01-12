package ar.edu.itba.ss.model;

import java.util.Objects;

public class ParticleWrapper {

  private final Particle particle;

  private ParticleWrapper(final Particle particle) {
    this.particle = Objects.requireNonNull(particle);
  }

  public static ParticleWrapper of(final Particle particle) {
    return new ParticleWrapper(particle);
  }
}
