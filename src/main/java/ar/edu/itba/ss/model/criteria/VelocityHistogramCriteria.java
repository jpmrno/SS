package ar.edu.itba.ss.model.criteria;

import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Points;
import javafx.geometry.Point2D;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VelocityHistogramCriteria implements Criteria{

    private boolean checked100 = false;
    private boolean checked75 = false;
    private boolean checked50 = false;
    private final Point2D start;
    private final Point2D end;

    public VelocityHistogramCriteria(Point2D start, Point2D end) {
        this.start = start;
        this.end = end;
    }


    @Override
    public boolean test(double time, Set<Particle> particles) {
        return test(time, new HashSet<>(particles));
    }

    @Override
    public boolean test(double time, List<Particle> particles) {
        if(!checked100){
            checked100 = true;
            return true;
        } else if (!checked75 && fractionInFirstBox(particles) <= 0.75){
            checked75 = true;
            return true;
        } else if (!checked50 && fractionInFirstBox(particles) <= 0.5){
            checked50 = true;
            return true;
        }
        return false;
    }

    private double fractionInFirstBox(List<Particle> particles){
        final List<Point2D> positions = particles.stream().filter(p -> p.id() > 0)
                .map(Particle::position).collect(Collectors.toList());
        final double fraction =
                (double) Points.between(positions, start, end) / positions.size();
        return fraction;
    }
}
