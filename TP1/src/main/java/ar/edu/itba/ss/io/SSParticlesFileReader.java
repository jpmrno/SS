package ar.edu.itba.ss.io;

import ar.edu.itba.ss.model.Particle;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Point2D;

public class SSParticlesFileReader {

  public static Map<Particle, Point2D> read(final Path path) throws IOException {
    final Map<Particle, Point2D> ret = new HashMap<>();

    try (final BufferedReader reader = Files.newBufferedReader(path)) {
      String line = reader.readLine();

      if (line == null) {
        throw new IllegalArgumentException("Missing size");
      }

      final int size;
      try {
        size = Integer.valueOf(line.trim());
      } catch (final NumberFormatException exception) {
        throw new IllegalArgumentException("Invalid size");
      }

      if ((line = reader.readLine()) == null) {
        throw new IllegalArgumentException("Missing time");
      }

      final double time;
      try {
        time = Double.valueOf(line.trim());
      } catch (final NumberFormatException exception) {
        throw new IllegalArgumentException("Invalid time");
      }

      for (int i = 0; i < size; i++) {
        line = reader.readLine();

        if (line == null) {
          throw new IllegalArgumentException("Missing particles");
        }

        final String[] coordinateStrings = line.trim().split("\\s+", 3);
        if (coordinateStrings.length != 3) {
          throw new IllegalArgumentException("Invalid particle's properties");
        }

        final double r;
        final double x;
        final double y;
        try {
          r = Double.valueOf(coordinateStrings[0]);
          x = Double.valueOf(coordinateStrings[1]);
          y = Double.valueOf(coordinateStrings[2]);
        } catch (final NumberFormatException exception) {
          throw new IllegalArgumentException("Invalid particle's properties");
        }

        ret.put(new Particle(i, r), new Point2D(x, y));
      }
    } catch (final IOException exception) {
      throw exception;
    }

    return ret;
  }
}
