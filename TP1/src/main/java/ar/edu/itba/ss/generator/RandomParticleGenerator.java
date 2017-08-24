package ar.edu.itba.ss.generator;

import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javafx.geometry.Point2D;

public abstract class RandomParticleGenerator {

  public static List<Particle> generateParticles(final Particle minParticle,
      final Particle maxParticle) {

    // TODO: validate parameters

    final List<Particle> particles = new LinkedList<>();
    int id = minParticle.id();

    while (id <= maxParticle.id()) {

      double x = minParticle.position().getX();
      if (x != maxParticle.position().getX()) {
        x = ThreadLocalRandom.current()
            .nextDouble(minParticle.position().getX(), maxParticle.position().getX());
      }

      double y = minParticle.position().getY();
      if (y != maxParticle.position().getY()) {
        y = ThreadLocalRandom.current()
            .nextDouble(minParticle.position().getY(), maxParticle.position().getY());
      }

      double r = minParticle.radius();
      if (r != maxParticle.radius()) {
        r = ThreadLocalRandom.current().nextDouble(minParticle.radius(), maxParticle.radius());
      }

      double desiredMagnitude = minParticle.velocity().magnitude();
      if (desiredMagnitude != maxParticle.velocity().magnitude()) {
        desiredMagnitude = ThreadLocalRandom.current()
            .nextDouble(minParticle.velocity().magnitude(), maxParticle.velocity().magnitude());
      }

      double vx = ThreadLocalRandom.current().nextDouble(desiredMagnitude);
      double vy = Math.sqrt(Math.pow(minParticle.velocity().magnitude(), 2) - Math.pow(vx, 2));

      final Particle particle = ImmutableParticle.builder()
          .id(id)
          .position(new Point2D(x, y))
          .radius(r)
          .velocity(new Point2D(vx, vy))
          .build();

      if (validParticle(particle, particles)) {
        id++;
        particles.add(particle);
      }
    }

    return particles;
  }

  private static boolean validParticle(final Particle particle, final List<Particle> particleList) {
    return particleList.parallelStream().noneMatch(p -> collide(particle, p));
  }

  // TODO: move to Particle
  private static boolean collide(final Particle particle1, final Particle particle2) {
    return particle1.position().distance(particle2.position()) < particle1.radius() + particle2
        .radius();
  }
}
