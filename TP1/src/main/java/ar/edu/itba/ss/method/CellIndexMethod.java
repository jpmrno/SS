package ar.edu.itba.ss.method;

import ar.edu.itba.ss.model.Neighbour;
import ar.edu.itba.ss.model.Particle;
import java.util.*;
import javafx.geometry.Point2D;

public class CellIndexMethod implements NeighbourFindingMethod {

  private final static int[][] DIRECTIONS = new int[][]{
      {0, 0},     // CURRENT
      {-1, 0},    // UP
      {-1, +1},   // UP-RIGHT
      {0, +1},    // RIGHT
      {+1, +1}    // DOWN-RIGHT
  };
  private final static int DIRECTIONS_ROW = 0;
  private final static int DIRECTIONS_COL = 1;

  private int m;
  private final double l;
  private double cellLength;
  private final boolean periodic;

  public CellIndexMethod(final double l, final boolean periodic) {
    this.l = l;
    this.periodic = periodic;
  }

  /**
   * @param particlesPositions positions of the particles, x as col and y as row
   */
  @Override
  public Map<Particle, Set<Neighbour>> apply(final Map<Particle, Point2D> particlesPositions,
      final double rc) {

    this.m = (int) (l / (rc + 2 * particlesPositions.keySet().stream().parallel().max(Comparator.comparingDouble(Particle::getRadius)).get().getRadius()));
    this.cellLength = l / m;

    if (rc >= cellLength) {
      throw new IllegalArgumentException(
          "Cutoff distance has to be less than cell length (" + cellLength + ").");
    }

    final Map<Particle, Set<Neighbour>> neighboursParticles = new HashMap<>();
    for (final Particle particle : particlesPositions.keySet()) {
      neighboursParticles.put(particle, new HashSet<>());
    }

    final List<Particle>[][] matrix = createMatrix(particlesPositions);
    for (int row = 0; row < m; row++) {
      for (int col = 0; col < m; col++) {
        if (matrix[row][col] != null) {
          addNeighbours(neighboursParticles, matrix, row, col, particlesPositions, rc);
        }
      }
    }

    return neighboursParticles;
  }

  private List<Particle>[][] createMatrix(final Map<Particle, Point2D> particlesPositions) {
    final List<Particle>[][] matrix = new List[m][m];

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

  private void addNeighbours(final Map<Particle, Set<Neighbour>> neighbours,
      final List<Particle>[][] matrix, final int currentRow, final int currentCol,
      final Map<Particle, Point2D> particlesPositions, final double rc) {

    final List<Particle> currentCell = matrix[currentRow][currentCol];

    for (final int[] direction : DIRECTIONS) {
      final int neighbourRow = currentRow + direction[DIRECTIONS_ROW];
      final int neighbourCol = currentCol + direction[DIRECTIONS_COL];

      // Skip if not periodic and row/col are out of bounds
      if (periodic || (neighbourRow >= 0 && neighbourRow < m && neighbourCol >= 0
          && neighbourCol < m)) {

        final List<Particle> neighbourCell = matrix[Math.floorMod(neighbourRow, m)][Math
            .floorMod(neighbourCol, m)];

        if (neighbourCell != null) {
          addNeighboursFromCell(neighbours, currentCell, neighbourCell, neighbourRow, neighbourCol,
              particlesPositions, rc);
        }
      }
    }
  }

  private void addNeighboursFromCell(final Map<Particle, Set<Neighbour>> neighbours,
      final List<Particle> currentCell, final List<Particle> neighbourCell, final int neighbourRow,
      final int neighbourCol, final Map<Particle, Point2D> particlesPositions, final double rc) {

    for (final Particle particle1 : currentCell) {
      for (final Particle particle2 : neighbourCell) {
        if (!particle1.equals(particle2)) {
          final Point2D point1 = particlesPositions.get(particle1);
          // Remember: col is x and row is y
          final Point2D point2 = particlesPositions.get(particle2)
              .add(coordinateCorrection(neighbourCol), coordinateCorrection(neighbourRow));
          final double distance = NeighbourFindingMethod
              .distance(particle1, point1, particle2, point2);

          if (distance <= rc) {
            neighbours.get(particle1).add(new Neighbour(particle2, distance));
            neighbours.get(particle2).add(new Neighbour(particle1, distance));
          }
        }
      }
    }
  }

  private double coordinateCorrection(final int cellIndex) {
    if (cellIndex < 0) {
      return -l;
    }

    if (cellIndex >= m) {
      return l;
    }

    return 0;
  }
}
