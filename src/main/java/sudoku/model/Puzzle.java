package sudoku.model;

/**
 * Immutable puzzle definition loaded from puzzles.txt.
 */
public final class Puzzle {
    public static final int BOARD_SIZE = 9;
    public static final int CELL_COUNT = BOARD_SIZE * BOARD_SIZE;

    private final String sourceLine;
    private final int[][] initialValues;

    public Puzzle(String sourceLine, int[][] initialValues) {
        this.sourceLine = sourceLine;
        this.initialValues = copyGrid(initialValues);
    }

    public String getSourceLine() {
        return sourceLine;
    }

    public int[][] getInitialValues() {
        return copyGrid(initialValues);
    }

    private int[][] copyGrid(int[][] grid) {
        int[][] copy = new int[BOARD_SIZE][BOARD_SIZE];
        for (int row = 0; row < BOARD_SIZE; row++) {
            System.arraycopy(grid[row], 0, copy[row], 0, BOARD_SIZE);
        }
        return copy;
    }
}
