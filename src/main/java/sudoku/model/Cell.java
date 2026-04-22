package sudoku.model;

/**
 * Immutable snapshot of a board cell for use by GUI, CLI and tests.
 */
public final class Cell {
    private final int row;
    private final int column;
    private final int value;
    private final boolean fixed;
    private final boolean invalid;

    public Cell(int row, int column, int value, boolean fixed, boolean invalid) {
        this.row = row;
        this.column = column;
        this.value = value;
        this.fixed = fixed;
        this.invalid = invalid;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public int getValue() {
        return value;
    }

    public boolean isFixed() {
        return fixed;
    }

    public boolean isInvalid() {
        return invalid;
    }
}
