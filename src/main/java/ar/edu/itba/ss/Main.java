package ar.edu.itba.ss;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

public class Main {

  private static final int[] vehicles = new int[]{1};
  private static final double[] vehiclesProbability = new double[]{1};
  private static final EnumeratedIntegerDistribution vehiclesDistribution =
      new EnumeratedIntegerDistribution(vehicles, vehiclesProbability);

  public static void main(final String[] args) {
    for (int i = 0; i < 100; i++) {
      System.out.println("Vehicle: " + vehiclesDistribution.sample());
    }
  }
}
