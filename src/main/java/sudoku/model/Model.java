package sudoku.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Observable;
import java.util.Random;

/**
 * Shared model used by both GUI and CLI.
 */
@SuppressWarnings("deprecation")
public class Model extends Observable {
    private final PuzzleLoader puzzleLoader;
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

    public Model(PuzzleLoader puzzleLoader, Random random) {
        this.puzzleLoader = puzzleLoader;
        this.random = random;
        this.puzzles = new ArrayList<>(puzzleLoader.loadPuzzles());
        this.moveHistory = new ArrayDeque<>();
        this.validationFeedbackEnabled = true;
        this.hintEnabled = true;
        this.randomPuzzleSelectionEnabled = true;
        loadNewGame();
        assert board != null;
    }

    public void loadNewGame() {
        int puzzleIndex = randomPuzzleSelectionEnabled ? random.nextInt(puzzles.size()) : 0;
        loadPuzzle(puzzleIndex);
    }

    public void loadPuzzle(int puzzleIndex) {
        if (puzzleIndex < 0 || puzzleIndex >= puzzles.size()) {
            throw new IllegalArgumentException("Puzzle index out of range.");
        }
        currentPuzzle = puzzles.get(puzzleIndex);
        board = new Board(currentPuzzle.getInitialValues());
        moveHistory.clear();
        notifyModelChanged();
        assert board != null;
    }

    public int getCellValue(int row, int column) {
        return board.getValue(row, column);
    }

    public Cell getCell(int row, int column) {
        return new Cell(row, column, board.getValue(row, column), board.isFixed(row, column), isCellInvalid(row, column));
    }

    public boolean isEditable(int row, int column) {
        return !board.isFixed(row, column);
    }

    public void setCellValue(int row, int column, int value) {
        if (!isEditable(row, column)) {
            throw new IllegalStateException("Pre-filled cells cannot be edited.");
        }
        int previousValue = board.getValue(row, column);
        board.setValue(row, column, value);
        moveHistory.push(new Move(new Position(row, column), previousValue, value));
        notifyModelChanged();
    }

    public void clearCell(int row, int column) {
        setCellValue(row, column, Board.EMPTY);
    }

    public boolean canUndo() {
        return !moveHistory.isEmpty();
    }

    public void undoLastMove() {
        if (!canUndo()) {
            return;
        }
        Move move = moveHistory.pop();
        Position position = move.getPosition();
        board.setValue(position.getRow(), position.getColumn(), move.getPreviousValue());
        notifyModelChanged();
    }

    public void resetPuzzle() {
        board = new Board(currentPuzzle.getInitialValues());
        moveHistory.clear();
        notifyModelChanged();
        assert board != null;
    }

    public boolean isValidationFeedbackEnabled() {
        return validationFeedbackEnabled;
    }

    public void setValidationFeedbackEnabled(boolean validationFeedbackEnabled) {
        this.validationFeedbackEnabled = validationFeedbackEnabled;
        notifyModelChanged();
    }

    public boolean isHintEnabled() {
        return hintEnabled;
    }

    public void setHintEnabled(boolean hintEnabled) {
        this.hintEnabled = hintEnabled;
        notifyModelChanged();
    }

    public boolean isRandomPuzzleSelectionEnabled() {
        return randomPuzzleSelectionEnabled;
    }

    public void setRandomPuzzleSelectionEnabled(boolean randomPuzzleSelectionEnabled) {
        this.randomPuzzleSelectionEnabled = randomPuzzleSelectionEnabled;
        notifyModelChanged();
    }

    public int getBoardSize() {
        return Board.SIZE;
    }

    public List<Cell> getCells() {
        List<Cell> cells = new ArrayList<>();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                cells.add(getCell(row, column));
            }
        }
        return cells;
    }

    public boolean isCellInvalid(int row, int column) {
        int value = board.getValue(row, column);
        if (value == Board.EMPTY) {
            return false;
        }
        return hasDuplicateInRow(row, column, value)
                || hasDuplicateInColumn(row, column, value)
                || hasDuplicateInBox(row, column, value);
    }

    public boolean isBoardValid() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                if (isCellInvalid(row, column)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isComplete() {
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
        return puzzles.size();
    }

    public String getCurrentPuzzleSource() {
        return currentPuzzle.getSourceLine();
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
}
