package ar.edu.itba.ss;

import ar.edu.itba.ss.generator.RandomParticleGenerator;
import ar.edu.itba.ss.io.AppendFileParticlesWriter;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Points;
import ar.edu.itba.ss.simulator.GasDiffusionSimulator2;
import java.util.List;
import javafx.geometry.Point2D;

public class TP3Main2 {

  private static final double BOX_HEIGHT = 0.5;
  private static final double BOX_WIDTH = 0.5;
  private static final double BOX_GAP = 0.1;

  private static final double VELOCITY_MAGNITUDE_MAX = 0.01;

  private static final double OVITO_BOX_PARTICLE_RADIUS = 0;
  private static final double OVITO_BOX_PARTICLE_MASS = 0;

  public static void main(String[] args) {

    final Particle minParticle = ImmutableParticle.builder()
        .id(1)
        .position(new Point2D(0.01, 0.01))
        .velocity(Point2D.ZERO)
        .radius(0.01)
        .build();
    final Particle maxParticle = ImmutableParticle.builder()
        .id(1)
        .position(new Point2D(BOX_WIDTH - 0.01, BOX_HEIGHT - 0.01))
        .velocity(Points.magnitudeToPoint2D(VELOCITY_MAGNITUDE_MAX))
        .radius(0.01)
        .build();
    List<Particle> particles = RandomParticleGenerator.generateParticles(minParticle, maxParticle);
    int i = -1;
    particles.add(ImmutableParticle.builder()
        .position(Point2D.ZERO)
        .velocity(Point2D.ZERO)
        .id(i--)
        .radius(OVITO_BOX_PARTICLE_RADIUS)
        .mass(OVITO_BOX_PARTICLE_MASS)
        .build());
    particles.add(ImmutableParticle.builder()
        .position(new Point2D(0, BOX_HEIGHT))
        .velocity(Point2D.ZERO)
        .id(i--)
        .radius(OVITO_BOX_PARTICLE_RADIUS)
        .mass(OVITO_BOX_PARTICLE_MASS)
        .build());
    particles.add(ImmutableParticle.builder()
        .position(new Point2D(BOX_WIDTH, 0))
        .velocity(Point2D.ZERO)
        .id(i--)
        .radius(OVITO_BOX_PARTICLE_RADIUS)
        .mass(OVITO_BOX_PARTICLE_MASS)
        .build());
    particles.add(ImmutableParticle.builder()
        .position(new Point2D(BOX_WIDTH, BOX_HEIGHT))
        .velocity(Point2D.ZERO)
        .id(i--)
        .radius(OVITO_BOX_PARTICLE_RADIUS)
        .mass(OVITO_BOX_PARTICLE_MASS)
        .build());
    particles.add(ImmutableParticle.builder()
        .position(new Point2D(BOX_WIDTH / 2, 0))
        .velocity(Point2D.ZERO)
        .id(i--)
        .radius(OVITO_BOX_PARTICLE_RADIUS)
        .mass(OVITO_BOX_PARTICLE_MASS)
        .build());
    particles.add(ImmutableParticle.builder()
        .position(new Point2D(BOX_WIDTH / 2, BOX_HEIGHT / 2 - BOX_GAP / 2))
        .velocity(Point2D.ZERO)
        .id(i--)
        .radius(OVITO_BOX_PARTICLE_RADIUS)
        .mass(OVITO_BOX_PARTICLE_MASS)
        .build());
    particles.add(ImmutableParticle.builder()
        .position(new Point2D(BOX_WIDTH / 2, BOX_HEIGHT / 2 + BOX_GAP / 2))
        .velocity(Point2D.ZERO)
        .id(i--)
        .radius(OVITO_BOX_PARTICLE_RADIUS)
        .mass(OVITO_BOX_PARTICLE_MASS)
        .build());
    particles.add(ImmutableParticle.builder()
        .position(new Point2D(BOX_WIDTH / 2, BOX_HEIGHT))
        .velocity(Point2D.ZERO)
        .id(i--)
        .radius(OVITO_BOX_PARTICLE_RADIUS)
        .mass(OVITO_BOX_PARTICLE_MASS)
        .build());
    final GasDiffusionSimulator2 simulator = new GasDiffusionSimulator2(
        particles,
        BOX_WIDTH,
        BOX_HEIGHT,
        BOX_GAP,
        100,
        new AppendFileParticlesWriter("gd_simulation"));
    simulator.call();
  }
}
