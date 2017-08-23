package ar.edu.itba.ss.io;

import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Particle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;

public class ParticlesFiles {

  public static void write(final Path path, final double time, final List<Particle> particles)
      throws IOException {
    try (final BufferedWriter writer = Files.newBufferedWriter(path)) {
      writer.write(particles.size() + "\n");
      writer.write(time + "\n");
      for (final Particle particle : particles) {
        writer.write(particle.id() + "  " + particle.radius() + "  " + particle.position().getX()
            + "  " + particle.position().getY() + "  " + particle.velocity().getX() + "  "
            + particle.velocity().getY() + "\n");
      }
    }
  }

  public static List<Particle> read(final Path path) throws IOException {
    final List<Particle> particles;

    try (final BufferedReader reader = Files.newBufferedReader(path)) {
      String line = reader.readLine();

      if (line == null) {
        throw new IllegalArgumentException("Missing size");
      }

      final int size;
      try {
        size = Integer.parseInt(line.trim());
      } catch (final NumberFormatException exception) {
        throw new IllegalArgumentException("Invalid size");
      }

      if ((line = reader.readLine()) == null) {
        throw new IllegalArgumentException("Missing time");
      }

      final double time;
      try {
        time = Double.parseDouble(line.trim());
      } catch (final NumberFormatException exception) {
        throw new IllegalArgumentException("Invalid time");
      }

      particles = new ArrayList<>(size);

      for (int i = 0; i < size; i++) {
        line = reader.readLine();

        if (line == null) {
          throw new IllegalArgumentException("Missing particles");
        }

        final String[] properties = line.trim().split("\\s+", 6);
        if (properties.length != 6) {
          throw new IllegalArgumentException("Invalid particle's properties");
        }

        final int id;
        final double r;
        final double x;
        final double y;
        final double vx;
        final double vy;
        try {
          id = Integer.parseInt(properties[0]);
          r = Double.parseDouble(properties[1]);
          x = Double.parseDouble(properties[2]);
          y = Double.parseDouble(properties[3]);
          vx = Double.parseDouble(properties[4]);
          vy = Double.parseDouble(properties[5]);
        } catch (final NumberFormatException exception) {
          exception.printStackTrace();
          throw new IllegalArgumentException("Invalid particle's properties");
        }

        particles.add(
            ImmutableParticle.builder().id(id).radius(r).position(new Point2D(x, y)).build());
      }
    }

    return particles;
  }
}
