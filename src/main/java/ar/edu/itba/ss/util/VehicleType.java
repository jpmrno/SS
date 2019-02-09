package ar.edu.itba.ss.util;

public enum VehicleType {
  MOTORCYCLE(1, 8),
  CAR(2, 6),
  TRUCK(4, 3),
  LIGHT(0,1000)
  ;

  private final int length;
  private final int maxVelocity;

  VehicleType(int length, int maxVelocity) {
    this.length = length;
    this.maxVelocity = maxVelocity;
  }

  public int getLength() {
    return length;
  }

  public int getMaxVelocity() {
    return maxVelocity;
  }
}
