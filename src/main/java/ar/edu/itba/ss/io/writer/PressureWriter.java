package ar.edu.itba.ss.io.writer;

import ar.edu.itba.ss.model.Collision;
import ar.edu.itba.ss.model.Particle;
import ar.edu.itba.ss.model.StateEquations;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class PressureWriter implements ParticlesWriter {

    private double pressure;

    @Override
    public void write(double time, List<Particle> particles) throws IOException {

    }

    @Override
    public void write(double time, Set<Particle> particles) throws IOException {

    }

    @Override
    public void write(final double time, final Set<Particle> particles, final Collision collision) throws IOException{
        pressure += collision.getPressure();
    }

    public double getPressure() {
        return pressure;
    }
}
