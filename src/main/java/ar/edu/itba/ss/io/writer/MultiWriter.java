package ar.edu.itba.ss.io.writer;

import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MultiWriter implements ParticlesWriter{

    private final List<ParticlesWriter> writers;

    public MultiWriter(List<ParticlesWriter> writers) {
        this.writers = writers;
    }

    @Override
    public void write(double time, List<Particle> particles) throws IOException {

    }

    @Override
    public void write(double time, Map<Particle, Set<Neighbour>> neighbours) throws IOException {
        for(ParticlesWriter writer : writers){
            writer.write(time,neighbours);
        }
    }
}
