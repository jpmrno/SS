package ar.edu.itba.ss.model.criteria;

import ar.edu.itba.ss.model.Particle;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class StationaryStateCriteria implements Criteria {

  private final int times;
  private final double error;
  private Queue<Double> values;

  public StationaryStateCriteria(int times, double error) {
    this.times = times;
    this.error = error;
    this.values = new LinkedList<>();
  }

  @Override
  public boolean test(long iteration, Collection<Particle> particles) {
    values.offer(particles.stream().mapToInt(Particle::velocity).average().orElse(0));

    System.out.println(particles.stream().mapToInt(Particle::velocity).average().orElse(0));
    if (values.size() > times) {
      values.poll();
    } else if (values.size() != times) {
      return false;
    }

    final double max = values.stream().max(Double::compareTo).orElse(Double.POSITIVE_INFINITY);
    final double min = values.stream().min(Double::compareTo).orElse(Double.NEGATIVE_INFINITY);

    return Math.abs(max - min) <= error;
  }
}
