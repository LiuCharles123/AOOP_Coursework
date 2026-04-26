package sudoku.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Observable;
import java.util.Random;

/**
 * Shared model used by both GUI and CLI.
 *
 * <p>Invariant: puzzles is non-empty, board/currentPuzzle are non-null, board is well-formed,
 * moveHistory contains only moves on in-range positions, and each enabled/disabled feature flag
 * is represented explicitly in this model.
 */
@SuppressWarnings("deprecation")
public class Model extends Observable {
    private final Random random;
    private final List<Puzzle> puzzles;
    private final Deque<Move> moveHistory;

    private Board board;
    private Puzzle currentPuzzle;

    private boolean validationFeedbackEnabled;
    private boolean hintEnabled;
    private boolean randomPuzzleSelectionEnabled;

    public Model() {
        this(new PuzzleLoader(), new Random());
    }

    /**
     * Precondition: puzzleLoader and random are non-null and puzzleLoader loads at least one
     * valid puzzle.
     * Postcondition: a puzzle is loaded and the model invariant holds.
     */
    public Model(PuzzleLoader puzzleLoader, Random random) {
        if (puzzleLoader == null || random == null) {
            throw new IllegalArgumentException("Puzzle loader and random generator must be non-null.");
        }
        this.random = random;
        this.puzzles = new ArrayList<>(puzzleLoader.loadPuzzles());
        this.moveHistory = new ArrayDeque<>();
        this.validationFeedbackEnabled = true;
        this.hintEnabled = true;
        this.randomPuzzleSelectionEnabled = true;
        loadNewGame();
        assert isWellFormed();
    }

    /**
     * Postcondition: a fresh puzzle is loaded and undo history is cleared.
     */
    public void loadNewGame() {
        assert !puzzles.isEmpty();
        int puzzleIndex = randomPuzzleSelectionEnabled ? random.nextInt(puzzles.size()) : 0;
        loadPuzzle(puzzleIndex);
        assert isWellFormed();
    }

    /**
     * Precondition: puzzleIndex is a valid index into the loaded puzzle list.
     * Postcondition: currentPuzzle and board refer to the chosen puzzle and undo history is empty.
     */
    public void loadPuzzle(int puzzleIndex) {
        if (puzzleIndex < 0 || puzzleIndex >= puzzles.size()) {
            throw new IllegalArgumentException("Puzzle index out of range.");
        }
        currentPuzzle = puzzles.get(puzzleIndex);
        board = new Board(currentPuzzle.getInitialValues());
        moveHistory.clear();
        notifyModelChanged();
        assert currentPuzzle == puzzles.get(puzzleIndex);
        assert moveHistory.isEmpty();
        assert isWellFormed();
    }

    /**
     * Precondition: row and column are each in the range 0..8.
     * Postcondition: returns the current value in that cell.
     */
    public int getCellValue(int row, int column) {
        validatePosition(row, column);
        assert isWellFormed();
        return board.getValue(row, column);
    }

    /**
     * Precondition: row and column are each in the range 0..8.
     * Postcondition: returns an immutable snapshot of the requested cell.
     */
    public Cell getCell(int row, int column) {
        validatePosition(row, column);
        assert isWellFormed();
        return new Cell(
                row,
                column,
                board.getValue(row, column),
                board.isFixed(row, column),
                validationFeedbackEnabled && isCellInvalid(row, column)
        );
    }

    /**
     * Precondition: row and column are each in the range 0..8.
     * Postcondition: returns true exactly when the cell was empty in the initial puzzle.
     */
    public boolean isEditable(int row, int column) {
        validatePosition(row, column);
        assert isWellFormed();
        return !board.isFixed(row, column);
    }

    /**
     * Precondition: row and column are each in the range 0..8, the cell is editable, and value is
     * in the range 0..9.
     * Postcondition: the cell stores value and, if the value changed, one undoable move is added.
     */
    public void setCellValue(int row, int column, int value) {
        validateEditableCell(row, column);
        validateValue(value);
        if (!isEditable(row, column)) {
            throw new IllegalStateException("Pre-filled cells cannot be edited.");
        }
        int previousValue = board.getValue(row, column);
        if (previousValue == value) {
            return;
        }
        board.setValue(row, column, value);
        moveHistory.push(new Move(new Position(row, column), previousValue, value));
        notifyModelChanged();
        assert board.getValue(row, column) == value;
        assert isWellFormed();
    }

    /**
     * Precondition: row and column are each in the range 0..8 and the cell is editable.
     * Postcondition: the cell stores 0.
     */
    public void clearCell(int row, int column) {
        setCellValue(row, column, Board.EMPTY);
    }

    /**
     * Postcondition: returns true exactly when one user action can be undone.
     */
    public boolean canUndo() {
        assert isWellFormed();
        return !moveHistory.isEmpty();
    }

    /**
     * Postcondition: if an undoable move exists, the most recent editable-cell change is reverted.
     */
    public void undoLastMove() {
        if (!canUndo()) {
            return;
        }
        Move move = moveHistory.pop();
        Position position = move.getPosition();
        board.setValue(position.getRow(), position.getColumn(), move.getPreviousValue());
        notifyModelChanged();
        assert isWellFormed();
    }

    /**
     * Postcondition: board values are restored to the initial puzzle and undo history is cleared.
     */
    public void resetPuzzle() {
        board = new Board(currentPuzzle.getInitialValues());
        moveHistory.clear();
        notifyModelChanged();
        assert moveHistory.isEmpty();
        assert isWellFormed();
    }

    public boolean isValidationFeedbackEnabled() {
        assert isWellFormed();
        return validationFeedbackEnabled;
    }

    /**
     * Postcondition: validation feedback flag equals validationFeedbackEnabled.
     */
    public void setValidationFeedbackEnabled(boolean validationFeedbackEnabled) {
        this.validationFeedbackEnabled = validationFeedbackEnabled;
        notifyModelChanged();
        assert this.validationFeedbackEnabled == validationFeedbackEnabled;
        assert isWellFormed();
    }

    public boolean isHintEnabled() {
        assert isWellFormed();
        return hintEnabled;
    }

    /**
     * Postcondition: hint flag equals hintEnabled.
     */
    public void setHintEnabled(boolean hintEnabled) {
        this.hintEnabled = hintEnabled;
        notifyModelChanged();
        assert this.hintEnabled == hintEnabled;
        assert isWellFormed();
    }

    public boolean isRandomPuzzleSelectionEnabled() {
        assert isWellFormed();
        return randomPuzzleSelectionEnabled;
    }

    /**
     * Postcondition: puzzle selection flag equals randomPuzzleSelectionEnabled.
     */
    public void setRandomPuzzleSelectionEnabled(boolean randomPuzzleSelectionEnabled) {
        this.randomPuzzleSelectionEnabled = randomPuzzleSelectionEnabled;
        notifyModelChanged();
        assert this.randomPuzzleSelectionEnabled == randomPuzzleSelectionEnabled;
        assert isWellFormed();
    }

    public int getBoardSize() {
        assert isWellFormed();
        return Board.SIZE;
    }

    /**
     * Postcondition: returns a snapshot of every cell in row-major order.
     */
    public List<Cell> getCells() {
        assert isWellFormed();
        List<Cell> cells = new ArrayList<>();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                cells.add(getCell(row, column));
            }
        }
        return cells;
    }

    /**
     * Precondition: row and column are each in the range 0..8.
     * Postcondition: returns true exactly when the cell currently duplicates another equal digit in
     * its row, column, or 3x3 box.
     */
    public boolean isCellInvalid(int row, int column) {
        validatePosition(row, column);
        assert isWellFormed();
        int value = board.getValue(row, column);
        if (value == Board.EMPTY) {
            return false;
        }
        return hasDuplicateInRow(row, column, value)
                || hasDuplicateInColumn(row, column, value)
                || hasDuplicateInBox(row, column, value);
    }

    /**
     * Postcondition: returns true exactly when no current cell violates Sudoku uniqueness rules.
     */
    public boolean isBoardValid() {
        assert isWellFormed();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                if (isCellInvalid(row, column)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Postcondition: returns true exactly when every cell is filled and the board is valid.
     */
    public boolean isComplete() {
        assert isWellFormed();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                if (board.getValue(row, column) == Board.EMPTY) {
                    return false;
                }
            }
        }
        return isBoardValid();
    }

    public int getPuzzleCount() {
        assert isWellFormed();
        return puzzles.size();
    }

    public String getCurrentPuzzleSource() {
        assert isWellFormed();
        return currentPuzzle.getSourceLine();
    }

    /**
     * Postcondition: returns true exactly when hints are enabled and at least one editable cell is
     * currently different from the solved puzzle.
     */
    public boolean canHint() {
        assert isWellFormed();
        return hintEnabled && findHintPosition() != null;
    }

    /**
     * Precondition: hintEnabled is true and a hint is available.
     * Postcondition: one editable cell is set to its solved value and returned as a snapshot.
     */
    public Cell applyHint() {
        if (!hintEnabled) {
            throw new IllegalStateException("Hint functionality is disabled.");
        }

        Position position = findHintPosition();
        if (position == null) {
            throw new IllegalStateException("No hint is available.");
        }

        int row = position.getRow();
        int column = position.getColumn();
        int hintValue = currentPuzzle.getSolvedValues()[row][column];
        setCellValue(row, column, hintValue);
        assert board.getValue(row, column) == hintValue;
        assert isWellFormed();
        return getCell(row, column);
    }

    /**
     * Precondition: row and column are each in the range 0..8.
     * Postcondition: returns the solved value for that position in the current puzzle.
     */
    public int getSolvedValue(int row, int column) {
        validatePosition(row, column);
        assert isWellFormed();
        return currentPuzzle.getSolvedValues()[row][column];
    }

    /**
     * Postcondition: returns true exactly when the board contains at least one invalid cell.
     */
    public boolean hasValidationErrors() {
        assert isWellFormed();
        return !isBoardValid();
    }

    private boolean isWellFormed() {
        if (random == null || puzzles == null || moveHistory == null) {
            return false;
        }
        if (puzzles.isEmpty() || board == null || currentPuzzle == null) {
            return false;
        }
        if (!puzzles.contains(currentPuzzle) || !board.isWellFormed()) {
            return false;
        }
        for (Move move : moveHistory) {
            if (move == null || move.getPosition() == null) {
                return false;
            }
            Position position = move.getPosition();
            if (position.getRow() < 0 || position.getRow() >= Board.SIZE
                    || position.getColumn() < 0 || position.getColumn() >= Board.SIZE) {
                return false;
            }
            if (move.getPreviousValue() < Board.EMPTY || move.getPreviousValue() > Board.SIZE
                    || move.getNewValue() < Board.EMPTY || move.getNewValue() > Board.SIZE) {
                return false;
            }
        }
        return true;
    }

    private boolean hasDuplicateInRow(int row, int column, int value) {
        for (int otherColumn = 0; otherColumn < Board.SIZE; otherColumn++) {
            if (otherColumn != column && board.getValue(row, otherColumn) == value) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDuplicateInColumn(int row, int column, int value) {
        for (int otherRow = 0; otherRow < Board.SIZE; otherRow++) {
            if (otherRow != row && board.getValue(otherRow, column) == value) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDuplicateInBox(int row, int column, int value) {
        int firstRow = (row / Board.BOX_SIZE) * Board.BOX_SIZE;
        int firstColumn = (column / Board.BOX_SIZE) * Board.BOX_SIZE;
        for (int rowOffset = 0; rowOffset < Board.BOX_SIZE; rowOffset++) {
            for (int columnOffset = 0; columnOffset < Board.BOX_SIZE; columnOffset++) {
                int currentRow = firstRow + rowOffset;
                int currentColumn = firstColumn + columnOffset;
                if ((currentRow != row || currentColumn != column)
                        && board.getValue(currentRow, currentColumn) == value) {
                    return true;
                }
            }
        }
        return false;
    }

    private void notifyModelChanged() {
        setChanged();
        notifyObservers();
    }

    private void validatePosition(int row, int column) {
        if (row < 0 || row >= Board.SIZE || column < 0 || column >= Board.SIZE) {
            throw new IllegalArgumentException("Row and column must be in the range 0..8.");
        }
    }

    private void validateEditableCell(int row, int column) {
        validatePosition(row, column);
    }

    private void validateValue(int value) {
        if (value < Board.EMPTY || value > Board.SIZE) {
            throw new IllegalArgumentException("Cell value must be in the range 0..9.");
        }
    }

    private Position findHintPosition() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                if (isEditable(row, column) && board.getValue(row, column) != getSolvedValue(row, column)) {
                    return new Position(row, column);
                }
            }
        }
        return null;
    }
}
