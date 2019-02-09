package ar.edu.itba.ss.model;

import ar.edu.itba.ss.util.VehicleType;
import org.immutables.value.Value;

@Value.Immutable
public abstract class Particle {

  public abstract int id();

  public abstract int row();

  public abstract int col();

  @Value.Default
  public int velocity() {
    return 1;
  }

  @Value.Default
  public VehicleType vehicleType() {
    return VehicleType.MOTORCYCLE;
  }

  public static Builder builder() {
    return new Particle.Builder();
  }

  public static class Builder extends ImmutableParticle.Builder {

    public final Particle.Builder position(final int row, final int col) {
      return row(row).col(col);
    }
  }
}
