package sudoku.model;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelTest {
    @Test
    void modelLoadsAtLeastOnePuzzle() {
        // Scenario: a fresh model is created from the bundled puzzles resource.
        // Expectation: at least one puzzle is available and the board shape is always 9x9.
        Model model = createDeterministicModel();

        assertTrue(model.getPuzzleCount() > 0);
        assertEquals(Board.SIZE, model.getBoardSize());
    }

    @Test
    void emptyEditableCellCanBeChanged() {
        // Scenario: the player enters a digit into an editable empty cell.
        // Expectation: the model stores the new value.
        Model model = createDeterministicModel();
        int row = findEditableRow(model);
        int column = findEditableColumn(model, row);

        model.setCellValue(row, column, 1);

        assertEquals(1, model.getCellValue(row, column));
    }

    @Test
    void undoRestoresPreviousValue() {
        // Scenario: the player changes one editable cell and then presses Undo.
        // Expectation: only the most recent move is reverted and the cell becomes empty again.
        Model model = createDeterministicModel();
        int row = findEditableRow(model);
        int column = findEditableColumn(model, row);

        model.setCellValue(row, column, 2);
        model.undoLastMove();

        assertFalse(model.canUndo());
        assertEquals(Board.EMPTY, model.getCellValue(row, column));
    }

    @Test
    void fixedCellCannotBeChanged() {
        // Scenario: the player attempts to overwrite a pre-filled clue.
        // Expectation: the request is rejected because fixed cells are never editable.
        Model model = createDeterministicModel();
        Position fixedPosition = findFixedPosition(model);

        assertThrows(
                IllegalStateException.class,
                () -> model.setCellValue(fixedPosition.getRow(), fixedPosition.getColumn(), 1)
        );
    }

    @Test
    void hintFillsCellWithSolvedValue() {
        // Scenario: a hint is requested while an editable cell is still unsolved.
        // Expectation: the model fills one editable cell with the corresponding solved value.
        Model model = createDeterministicModel();
        Position editablePosition = findEditablePosition(model);

        model.clearCell(editablePosition.getRow(), editablePosition.getColumn());
        Cell hintedCell = model.applyHint();

        assertEquals(editablePosition.getRow(), hintedCell.getRow());
        assertEquals(editablePosition.getColumn(), hintedCell.getColumn());
        assertEquals(
                model.getSolvedValue(editablePosition.getRow(), editablePosition.getColumn()),
                model.getCellValue(editablePosition.getRow(), editablePosition.getColumn())
        );
    }

    @Test
    void disablingHintPreventsHintUsage() {
        // Scenario: the hint flag is switched off before the player requests help.
        // Expectation: no hint is available and applyHint throws an exception.
        Model model = createDeterministicModel();
        model.setHintEnabled(false);

        assertFalse(model.canHint());
        assertThrows(IllegalStateException.class, model::applyHint);
    }

    @Test
    void conflictingEntriesMakeBoardInvalid() {
        // Scenario: two editable cells in the same row are given the same digit.
        // Expectation: both cells become invalid and the board is no longer valid.
        Model model = createDeterministicModel();
        Position first = findEditablePosition(model);
        Position second = findSecondEditablePositionInSameRow(model, first.getRow(), first.getColumn());

        model.setCellValue(first.getRow(), first.getColumn(), 1);
        model.setCellValue(second.getRow(), second.getColumn(), 1);

        assertTrue(model.isCellInvalid(first.getRow(), first.getColumn()));
        assertTrue(model.isCellInvalid(second.getRow(), second.getColumn()));
        assertFalse(model.isBoardValid());
    }

    @Test
    void resetRestoresInitialPuzzleAndClearsUndoHistory() {
        // Scenario: the player edits an editable cell and then resets the puzzle.
        // Expectation: the board returns to the initial puzzle state and there is nothing left to undo.
        Model model = createDeterministicModel();
        Position editablePosition = findEditablePosition(model);
        int initialValue = model.getCellValue(editablePosition.getRow(), editablePosition.getColumn());

        model.setCellValue(editablePosition.getRow(), editablePosition.getColumn(), 3);
        model.resetPuzzle();

        assertEquals(initialValue, model.getCellValue(editablePosition.getRow(), editablePosition.getColumn()));
        assertFalse(model.canUndo());
    }

    private Model createDeterministicModel() {
        return new Model(new PuzzleLoader(), new Random(0));
    }

    private int findEditableRow(Model model) {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                if (model.isEditable(row, column)) {
                    return row;
                }
            }
        }
        throw new IllegalStateException("Expected at least one editable cell.");
    }

    private int findEditableColumn(Model model, int targetRow) {
        for (int column = 0; column < Board.SIZE; column++) {
            if (model.isEditable(targetRow, column)) {
                return column;
            }
        }
        throw new IllegalStateException("Expected at least one editable cell in the target row.");
    }

    private Position findEditablePosition(Model model) {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                if (model.isEditable(row, column)) {
                    return new Position(row, column);
                }
            }
        }
        throw new IllegalStateException("Expected at least one editable cell.");
    }

    private Position findFixedPosition(Model model) {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                if (!model.isEditable(row, column)) {
                    return new Position(row, column);
                }
            }
        }
        throw new IllegalStateException("Expected at least one fixed cell.");
    }

    private Position findSecondEditablePositionInSameRow(Model model, int row, int excludedColumn) {
        for (int column = 0; column < Board.SIZE; column++) {
            if (column != excludedColumn && model.isEditable(row, column)) {
                return new Position(row, column);
            }
        }
        throw new IllegalStateException("Expected at least two editable cells in the same row.");
    }
}
