package ar.edu.itba.ss.model.criteria;

import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Points;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.geometry.Point2D;

public class FractionCriteria implements Criteria {

  private final double fraction;
  private final Point2D start;
  private final Point2D end;
  private final double error;

  public FractionCriteria(final double fraction, final Point2D start, final Point2D end,
      double error) {
    if (fraction < 0.0 || fraction > 1.0) {
      throw new IllegalArgumentException("Invalid fraction");
    }

    if (error < 0.0 || error > 1.0) {
      throw new IllegalArgumentException("Invalid error");
    }

    this.fraction = fraction;
    this.start = Objects.requireNonNull(start);
    this.end = Objects.requireNonNull(end);
    this.error = error;
  }

  @Override
  public boolean test(final double time, final Set<Particle> originalParticles) {
    final List<Point2D> positions = originalParticles.stream().filter(p -> p.id() > 0)
        .map(Particle::position).collect(Collectors.toList());
    final double fractionInBox =
        (double) Points.between(positions, start, end) / positions.size();

    return (fraction - error) <= fractionInBox && (fraction + error) >= fractionInBox;
  }
}
