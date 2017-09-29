package ar.edu.itba.ss.simulator;

import ar.edu.itba.ss.io.writer.ParticlesWriter;
import ar.edu.itba.ss.method.CellIndexMethod;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.criteria.Criteria;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LennardJonesGasSimulator implements Simulator {

    private final List<Particle> initialParticles;
    private final double dt;
    private final Point2D[][] wallsVertical;
    private final Point2D[][] wallsHorizontal;
    private final double boxWidth;
    private final double boxHeight;
    private final double middleGap;
    private final double epsilon;
    private final double rm;
    private final CellIndexMethod cim;
    private final double rc;

    public LennardJonesGasSimulator(final List<Particle> initialParticles, final double boxWidth,
                                    final double boxHeight, final double middleGap, final double dt,
                                    double epsilon, double rm, double rc) {
        this.initialParticles = initialParticles;
        this.dt = dt;
        this.middleGap = middleGap;
        this.boxWidth = boxWidth;
        this.boxHeight = boxHeight;
        this.epsilon = epsilon;
        this.rm = rm;
        this.rc = rc;
        this.wallsVertical = new Point2D[][]{
                {Point2D.ZERO, new Point2D(0, boxHeight)},
                {new Point2D(boxWidth / 2, 0), new Point2D(boxWidth / 2, boxHeight / 2 - middleGap / 2)},
                {new Point2D(boxWidth / 2, boxHeight / 2 + middleGap / 2),
                        new Point2D(boxWidth / 2, boxHeight)},
                {new Point2D(boxWidth, 0), new Point2D(boxWidth, boxHeight)}
        };
        this.wallsHorizontal = new Point2D[][]{
                {Point2D.ZERO, new Point2D(boxWidth, 0)},
                {new Point2D(0, boxHeight), new Point2D(boxWidth, boxHeight)},
        };
        this.cim = new CellIndexMethod(boxHeight > boxWidth ? boxHeight : boxWidth, false);
    }

    @Override
    public Set<Particle> simulate(Criteria endCriteria, ParticlesWriter writer) {
        double time = 0;
        List<Particle> particles = initialParticles;

        while (!endCriteria.test(time, particles)) {
            Map<Particle, Set<Neighbour>> neighbours = cim.apply(particles,
                    particles.get(0).radius(), rc);
            particles = nextParticles(neighbours);
        }
        return null;
    }

    private List<Particle> nextParticles(Map<Particle, Set<Neighbour>> neighbours){
        List<Particle> nextParticles = new ArrayList<>(neighbours.size());
        for(Map.Entry<Particle,Set<Neighbour>> entry : neighbours.entrySet()){
            nextParticles.add(moveParticle(entry.getKey(), entry.getValue()));
        }
        return nextParticles;
    }

    private Particle moveParticle(Particle particle, Set<Neighbour> neighbours) {
        neighbours = neighbours.stream()
                .filter(n -> !isWallBetween(particle,n.getNeighbourParticle()))
                .collect(Collectors.toSet());
        addWallParticles(particle,neighbours);

        //TODO: finish
        return null;
    }

    private void addWallParticles(Particle particle, Set<Neighbour> neighbours) {
        // up wall
        double distanceToExtremeWall = boxHeight - particle.position().getY();
        if(distanceToExtremeWall <= rc){
            neighbours.add(new Neighbour(ImmutableParticle.builder()
                    .id(-1).position(new Point2D(particle.position().getX(),boxHeight))
                    .mass(Double.POSITIVE_INFINITY)
                    .velocity(Point2D.ZERO).build(),distanceToExtremeWall));
        }

        // down wall
        distanceToExtremeWall = particle.position().getY();
        if(distanceToExtremeWall <= rc){
            neighbours.add(new Neighbour(ImmutableParticle.builder()
                    .id(-1).position(new Point2D(particle.position().getX(),0))
                    .mass(Double.POSITIVE_INFINITY)
                    .velocity(Point2D.ZERO).build(),distanceToExtremeWall));
        }

        // left wall
        distanceToExtremeWall = particle.position().getX();
        double distanceToMiddleWall = distanceToExtremeWall - boxWidth/2;
        if(distanceToExtremeWall <= rc){
            neighbours.add(new Neighbour(ImmutableParticle.builder()
                    .id(-1).position(new Point2D(0,particle.position().getY()))
                    .mass(Double.POSITIVE_INFINITY)
                    .velocity(Point2D.ZERO).build(),distanceToExtremeWall));
        }else if(distanceToMiddleWall > 0 && distanceToMiddleWall <= rc){
            neighbours.add(new Neighbour(ImmutableParticle.builder()
                    .id(-1).position(new Point2D(boxWidth/2,particle.position().getY()))
                    .mass(Double.POSITIVE_INFINITY)
                    .velocity(Point2D.ZERO).build(),distanceToMiddleWall));
        }

        // right wall
        distanceToExtremeWall = boxWidth - particle.position().getX();
        distanceToMiddleWall = distanceToExtremeWall - boxWidth/2;
        if(distanceToExtremeWall <= rc){
            neighbours.add(new Neighbour(ImmutableParticle.builder()
                    .id(-1).position(new Point2D(boxWidth,particle.position().getY()))
                    .mass(Double.POSITIVE_INFINITY)
                    .velocity(Point2D.ZERO).build(),distanceToExtremeWall));
        }else if(distanceToMiddleWall > 0 && distanceToMiddleWall <= rc){
            neighbours.add(new Neighbour(ImmutableParticle.builder()
                    .id(-1).position(new Point2D(boxWidth/2,particle.position().getY()))
                    .mass(Double.POSITIVE_INFINITY)
                    .velocity(Point2D.ZERO).build(),distanceToMiddleWall));
        }
    }

    private final boolean isWallBetween(Particle particle1, Particle particle2){
        final double x1 = particle1.position().getX();
        final double x2 = particle2.position().getX();
        final double y1 = particle1.position().getY();
        final double y2 = particle2.position().getY();

        if(x1 == x2)
            return false;

        final double m = (y2 - y1) / (x2 - x1);
        final double b = y1 - m * x1;
        final double xp = boxWidth / 2;
        final double yp = m * xp + b;

        final double gapStart = (boxHeight/2)-(middleGap/2);
        final double gapEnd = boxHeight - gapStart;

        return yp > gapStart && yp < gapEnd;
    }
}
