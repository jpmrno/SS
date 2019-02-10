package ar.edu.itba.ss.io.writer;

import ar.edu.itba.ss.model.Road;
import java.io.IOException;
import java.util.List;

public interface ParticlesWriter {

  void write(final double time, final List<Road> roads) throws IOException;
}
