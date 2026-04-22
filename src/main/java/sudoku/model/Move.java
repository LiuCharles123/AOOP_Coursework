package sudoku.model;

/**
 * Single user action on an editable cell.
 */
public final class Move {
    private final Position position;
    private final int previousValue;
    private final int newValue;

    public Move(Position position, int previousValue, int newValue) {
        this.position = position;
        this.previousValue = previousValue;
        this.newValue = newValue;
    }

    public Position getPosition() {
        return position;
    }

    public int getPreviousValue() {
        return previousValue;
    }

    public int getNewValue() {
        return newValue;
    }
}
