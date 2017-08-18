package ar.edu.itba.ss;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javafx.geometry.Point2D;

public class CellIndexMethod {

  private final int sideSize;
  private final double cellLength;

  public CellIndexMethod(final int sideSize, final double sideLength) {
    this.sideSize = sideSize;
    this.cellLength = sideLength / sideSize;
  }

  /**
   * @param particlesPositions positions of the particles, x as col and y as row
   */
  public Map<Particle, List<Neighbour>> apply(final Map<Particle, Point2D> particlesPositions,
      final double maxDistance) {
    if (cellLength <= maxDistance) {
      throw new IllegalArgumentException(
          "Max distance has to be less than cell length (" + cellLength + ").");
    }

    final Map<Particle, List<Neighbour>> neighboursParticles = new HashMap<>();
    for (final Particle particle : particlesPositions.keySet()) {
      neighboursParticles.put(particle, new LinkedList<Neighbour>());
    }

    final List<Particle>[][] matrix = createMatrix(particlesPositions);
    for (int row = 0; row < sideSize; row++) {
      for (int col = 0; col < sideSize; col++) {
        if (matrix[row][col] != null) {
          final List<Particle>[] neighbourCells = getNeighbourCells(matrix, row, col);

          for (final Particle particle : matrix[row][col]) {
            addParticleNeighbours(particle, particlesPositions, neighboursParticles, neighbourCells,
                maxDistance);
          }
        }
      }
    }

    return neighboursParticles;
  }

  private List<Particle>[][] createMatrix(final Map<Particle, Point2D> particlesPositions) {
    final List<Particle>[][] matrix = new List[sideSize][sideSize];

    for (final Particle particle : particlesPositions.keySet()) {
      final Point2D position = particlesPositions.get(particle);
      final int row = (int) (position.getY() / cellLength);
      final int col = (int) (position.getX() / cellLength);

      if (matrix[row][col] == null) {
        matrix[row][col] = new LinkedList<>();
      }

      matrix[row][col].add(particle);
    }

    return matrix;
  }

  private List<Particle>[] getNeighbourCells(final List<Particle>[][] matrix, final int row,
      final int col) {
    final List<Particle>[] neighbourCells = new LinkedList[5];

    neighbourCells[0] = matrix[row][col];

    neighbourCells[1] = row == sideSize - 1 ? matrix[0][col] : matrix[row + 1][col];

    neighbourCells[2] = col == sideSize - 1 ? matrix[row][0] : matrix[row][col + 1];

    // Down and right
    if (row == sideSize - 1) {
      if (col == sideSize - 1) {
        neighbourCells[3] = matrix[0][0];
      } else {
        neighbourCells[3] = matrix[0][col + 1];
      }
    } else if (col == sideSize - 1) {
      neighbourCells[3] = matrix[row + 1][0];
    } else {
      neighbourCells[3] = matrix[row + 1][col + 1];
    }

    // Down and left
    if (row == sideSize - 1) {
      if (col == 0) {
        neighbourCells[4] = matrix[0][sideSize - 1];
      } else {
        neighbourCells[4] = matrix[0][col - 1];
      }
    } else if (col == 0) {
      neighbourCells[4] = matrix[row + 1][sideSize - 1];
    } else {
      neighbourCells[4] = matrix[row + 1][col - 1];
    }

    return neighbourCells;
  }

  private void addParticleNeighbours(final Particle currentParticle,
      final Map<Particle, Point2D> particlesPositions,
      final Map<Particle, List<Neighbour>> neighboursParticles,
      final List<Particle>[] neighbourCells,
      final double maxDistance) {

    for (final List<Particle> neighbourCell : neighbourCells) {
      if (neighbourCell != null) {
        for (final Particle particle : neighbourCell) {
          if (currentParticle != particle) {
            final double distance = getDistance(currentParticle, particle,
                particlesPositions);

            if (distance < maxDistance
                && !neighboursParticles.get(currentParticle).contains(new Neighbour(particle,distance))) {
              neighboursParticles.get(currentParticle).add(new Neighbour(particle, distance));
              neighboursParticles.get(particle).add(new Neighbour(currentParticle, distance));
            }
          }
        }
      }
    }
  }

  private double getDistance(final Particle particle1, final Particle particle2,
      final Map<Particle, Point2D> particlesPositions) {
    return particlesPositions.get(particle1).distance(particlesPositions.get(particle2))
        - particle1.getRadius() - particle2.getRadius();
  }
}
