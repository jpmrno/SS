package ar.edu.itba.ss.util;

public enum VehicleType {

  MOTORCYCLE(1, 7),
  CAR(2, 7),
  TRUCK(4, 6);

  private final int length;
  private final int maxVelocity;

  VehicleType(int length, int maxVelocity) {
    this.length = length;
    this.maxVelocity = maxVelocity;

  }

  public int length() {
    return length;
  }

  public int maxVelocity() {
    return maxVelocity;
  }
}
