package ar.edu.itba.ss;

import ar.edu.itba.ss.generator.RandomParticleGenerator;
import ar.edu.itba.ss.io.writer.*;
import ar.edu.itba.ss.method.BeemanMovementFunction;
import ar.edu.itba.ss.method.EulerMovementFunction;
import ar.edu.itba.ss.method.LennardJonesForceFunction;
import ar.edu.itba.ss.method.MovementFunction;
import ar.edu.itba.ss.method.neigbour.CellIndexMethod;
import ar.edu.itba.ss.model.ImmutableParticle;
import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.Points;
import ar.edu.itba.ss.model.criteria.*;
import ar.edu.itba.ss.simulator.LennardJonesGasSimulator;
import javafx.geometry.Point2D;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class GasMainHistogram {

    //TODO: set correct parameters
    private static final int N = 100;
    private static final double MASS = 0.1;
    private static final double RADIUS = 2;
    private static final double INITIAL_VELOCITY_MAGNITUDE = 10;
    private static final double DT = 0.001;
    private static final int WRITER_ITERATION = (int) (1 / DT) / 10;

    private static final double BOX_HEIGHT = 200;
    private static final double BOX_WIDTH = 400;
    private static final double BOX_GAP = 10;

    private static final double RC = 5;
    private static final double EPSILON = 2;
    private static final double RM = 1;

    private static final BiFunction<Particle, Set<Neighbour>, Point2D> FORCE_FUNCTION =
            new LennardJonesForceFunction(EPSILON, RM);

    private static final CellIndexMethod cellIndexMethod = new CellIndexMethod(
            Math.max(BOX_HEIGHT, BOX_WIDTH), false);

    public static void main(final String[] args) {
        List<Particle> previousParticles = randomParticles();
        previousParticles = previousParticles.stream()
                .map(p -> ImmutableParticle.builder().from(p).radius(0).build())
                .collect(Collectors.toList());
        final Map<Particle, Set<Neighbour>> previousNeighbours = cellIndexMethod
                .apply(previousParticles, previousParticles.get(0).radius(), RC);

        final List<Particle> currentParticles = new ArrayList<>(previousParticles.size());
        final Map<Particle, MovementFunction> movementFunctions = new HashMap<>(
                previousParticles.size());

        beemanAddCurrentParticlesAndMovementFunctions(previousParticles, previousNeighbours,
                currentParticles, movementFunctions);

        final LennardJonesGasSimulator simulator = new LennardJonesGasSimulator(currentParticles,
                BOX_WIDTH, BOX_HEIGHT, BOX_GAP, DT, WRITER_ITERATION, RC, movementFunctions);

        final VelocityHistogramCriteria velocityHistogramCriteria = new VelocityHistogramCriteria(Point2D.ZERO,
                new Point2D(BOX_WIDTH/2,BOX_HEIGHT));
        final VelocityHistogramWriter velocityHistogramWriter =
                new VelocityHistogramWriter(21,velocityHistogramCriteria);
        final ParticlesInFirstBoxWriter particlesInFirstBoxWriter = new ParticlesInFirstBoxWriter(BOX_WIDTH, BOX_HEIGHT);
        List<ParticlesWriter> writers = new LinkedList<>();
        writers.add(particlesInFirstBoxWriter);
        writers.add(velocityHistogramWriter);
        final MultiWriter multiWriter = new MultiWriter(writers);

        final Criteria criteria = new EquilibriumOscilationCriteria(Point2D.ZERO,
                new Point2D(BOX_WIDTH/2,BOX_HEIGHT), 3,0.05);

//        final ParticlesWriter particlesWriter = new BoxParticleWritter("ljg_simulation", BOX_WIDTH,
//                BOX_HEIGHT, BOX_GAP);
//
//        try {
//            particlesWriter.write(0, currentParticles);
//        } catch (IOException e) {
//            System.err.println("Could not write initial particles");
//        }


        simulator.simulate(criteria, multiWriter);
        particlesInFirstBoxWriter.addSeries("");
    }

    private static List<Particle> randomParticles() {
        final Particle minParticle = ImmutableParticle.builder()
                .id(1)
                .position(new Point2D(RADIUS, RADIUS))
                .velocity(Points.magnitudeToPoint2D(INITIAL_VELOCITY_MAGNITUDE))
                .radius(RADIUS)
                .mass(MASS)
                .build();

        final Particle maxParticle = ImmutableParticle.builder()
                .id(N)
                .position(new Point2D(BOX_WIDTH / 2 - RADIUS, BOX_HEIGHT - RADIUS))
                .velocity(Points.magnitudeToPoint2D(INITIAL_VELOCITY_MAGNITUDE))
                .radius(RADIUS)
                .mass(MASS)
                .build();

        return RandomParticleGenerator.generateParticles(minParticle, maxParticle);
    }

    private static void beemanAddCurrentParticlesAndMovementFunctions(
            final List<Particle> previousParticles,
            final Map<Particle, Set<Neighbour>> previousNeighbours, final List<Particle> currentParticles,
            final Map<Particle, MovementFunction> movementFunctions) {
        for (final Particle previousParticle : previousParticles) {
            final Point2D previousAcceleration = FORCE_FUNCTION
                    .apply(previousParticle, previousNeighbours.get(previousParticle))
                    .multiply(1.0 / previousParticle.mass());

            final Particle currentParticle = EulerMovementFunction
                    .move(previousParticle, previousNeighbours.get(previousParticle), DT, FORCE_FUNCTION);

            final MovementFunction movementFunction = new BeemanMovementFunction(FORCE_FUNCTION,
                    previousAcceleration);

            currentParticles.add(currentParticle);
            movementFunctions.put(currentParticle, movementFunction);
        }
    }

    private static void eulerAddCurrentParticlesAndMovementFunctions(
            final List<Particle> previousParticles,
            final Map<Particle, Set<Neighbour>> previousNeighbours, final List<Particle> currentParticles,
            final Map<Particle, MovementFunction> movementFunctions) {

        final MovementFunction movementFunction = new EulerMovementFunction(FORCE_FUNCTION);
        for (final Particle previousParticle : previousParticles) {
            currentParticles.add(previousParticle);
            movementFunctions.put(previousParticle, movementFunction);
        }
    }

}
