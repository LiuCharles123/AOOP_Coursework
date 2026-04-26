package sudoku.model;

import java.util.Arrays;

/**
 * Board data structure with no UI knowledge.
 *
 * <p>Invariant: values and fixedCells are always 9x9 arrays, and every stored cell value is in
 * the range 0..9.
 */
public final class Board {
    public static final int SIZE = 9;
    public static final int BOX_SIZE = 3;
    public static final int EMPTY = 0;

    private final int[][] values;
    private final boolean[][] fixedCells;

    /**
     * Precondition: initialValues is a 9x9 grid containing digits in the range 0..9.
     * Postcondition: current values equal initialValues, and non-zero cells are marked as fixed.
     */
    public Board(int[][] initialValues) {
        validateGrid(initialValues);
        values = copyGrid(initialValues);
        fixedCells = new boolean[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                fixedCells[row][column] = initialValues[row][column] != EMPTY;
            }
        }
        assert isWellFormed();
    }

    /**
     * Precondition: row and column are each in the range 0..8.
     * Postcondition: returns the value currently stored at the requested position.
     */
    public int getValue(int row, int column) {
        validatePosition(row, column);
        assert isWellFormed();
        return values[row][column];
    }

    /**
     * Precondition: row and column are each in the range 0..8 and value is in the range 0..9.
     * Postcondition: the requested position stores value.
     */
    public void setValue(int row, int column, int value) {
        validatePosition(row, column);
        validateDigit(value);
        values[row][column] = value;
        assert values[row][column] == value;
        assert isWellFormed();
    }

    /**
     * Precondition: row and column are each in the range 0..8.
     * Postcondition: returns whether the cell was part of the initial puzzle.
     */
    public boolean isFixed(int row, int column) {
        validatePosition(row, column);
        assert isWellFormed();
        return fixedCells[row][column];
    }

    private boolean isSquare() {
        return values.length == SIZE && fixedCells.length == SIZE;
    }

    boolean isWellFormed() {
        if (!isSquare()) {
            return false;
        }
        for (int row = 0; row < SIZE; row++) {
            if (values[row] == null || fixedCells[row] == null) {
                return false;
            }
            if (values[row].length != SIZE || fixedCells[row].length != SIZE) {
                return false;
            }
            for (int column = 0; column < SIZE; column++) {
                int value = values[row][column];
                if (value < EMPTY || value > SIZE) {
                    return false;
                }
            }
        }
        return true;
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
