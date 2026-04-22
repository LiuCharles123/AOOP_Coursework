package sudoku.model;

import java.util.Arrays;

/**
 * Board data structure with no UI knowledge.
 */
public final class Board {
    public static final int SIZE = 9;
    public static final int BOX_SIZE = 3;
    public static final int EMPTY = 0;

    private final int[][] values;
    private final boolean[][] fixedCells;

    public Board(int[][] initialValues) {
        validateGrid(initialValues);
        values = copyGrid(initialValues);
        fixedCells = new boolean[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                fixedCells[row][column] = initialValues[row][column] != EMPTY;
            }
        }
        assert isSquare();
    }

    public int getValue(int row, int column) {
        validatePosition(row, column);
        return values[row][column];
    }

    public void setValue(int row, int column, int value) {
        validatePosition(row, column);
        validateDigit(value);
        values[row][column] = value;
        assert isSquare();
    }

    public boolean isFixed(int row, int column) {
        validatePosition(row, column);
        return fixedCells[row][column];
    }

    public int[][] copyValues() {
        return copyGrid(values);
    }

    public boolean[][] copyFixedCells() {
        boolean[][] copy = new boolean[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            System.arraycopy(fixedCells[row], 0, copy[row], 0, SIZE);
        }
        return copy;
    }

    public void restoreValues(int[][] restoredValues) {
        validateGrid(restoredValues);
        for (int row = 0; row < SIZE; row++) {
            System.arraycopy(restoredValues[row], 0, values[row], 0, SIZE);
        }
        assert isSquare();
    }

    public boolean isSquare() {
        return values.length == SIZE && fixedCells.length == SIZE;
    }

    private void validateGrid(int[][] grid) {
        if (grid == null || grid.length != SIZE) {
            throw new IllegalArgumentException("Grid must be 9x9.");
        }
        for (int row = 0; row < SIZE; row++) {
            if (grid[row] == null || grid[row].length != SIZE) {
                throw new IllegalArgumentException("Grid must be 9x9.");
            }
            for (int column = 0; column < SIZE; column++) {
                validateDigit(grid[row][column]);
            }
        }
    }

    private void validatePosition(int row, int column) {
        if (row < 0 || row >= SIZE || column < 0 || column >= SIZE) {
            throw new IllegalArgumentException("Row and column must be in the range 0..8.");
        }
    }

    private void validateDigit(int value) {
        if (value < EMPTY || value > SIZE) {
            throw new IllegalArgumentException("Cell value must be in the range 0..9.");
        }
    }

    private int[][] copyGrid(int[][] grid) {
        int[][] copy = new int[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            copy[row] = Arrays.copyOf(grid[row], SIZE);
        }
        return copy;
    }
}
