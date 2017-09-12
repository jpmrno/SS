package ar.edu.itba.ss.io.writer;

import ar.edu.itba.ss.model.Collision;
import ar.edu.itba.ss.model.Particle;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class PressureWriter implements ParticlesWriter {

  private double pressure;
  private double startTime = -1;
  private double endTime = -1;

  @Override
  public void write(final double time, final List<Particle> particles) throws IOException {
    // Ignore
  }

  @Override
  public void write(final double time, final Set<Particle> particles, final Collision collision)
      throws IOException {
    if (startTime == -1) {
      startTime = time;
      endTime = time;
    } else {
      endTime = time;
    }
    pressure += collision.getPressure();
  }

  @Override
  public void write(final double time, final Set<Particle> particles) throws IOException {
    // Ignore
  }

  public double getPressure() {
    return pressure / (endTime - startTime);
  }
}
