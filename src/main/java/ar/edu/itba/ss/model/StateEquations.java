package ar.edu.itba.ss.model;

import java.awt.geom.Point2D;
import java.util.Set;

public abstract class StateEquations {
    private static final double BOLTZMANN_K = 1.3806504e-23;

    public static double temperature(final Set<Particle> particles){
        return particles.stream()
                .filter(p -> p.id() > 0)
                .mapToDouble(p -> p.mass()*p.velocity().magnitude()*p.velocity().magnitude())
                .average().getAsDouble() / (BOLTZMANN_K * 2);
    }

    public static double pressure(final Particle particle, double length){
        return 2*particle.mass()*particle.velocity().magnitude()/length;
    }
}
