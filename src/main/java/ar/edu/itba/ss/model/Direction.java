package ar.edu.itba.ss.model;

public enum Direction {
    NORTH, EAST, SOUTH, WEST;

    public boolean isHorizontal() {
        return this == EAST || this == WEST;
    }

    public Boolean isVertical() {
        return this == NORTH || this == SOUTH;
    }
}
