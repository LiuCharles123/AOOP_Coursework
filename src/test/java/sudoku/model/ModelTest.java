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
        Model model = new Model();

        assertTrue(model.getPuzzleCount() > 0);
        assertEquals(Board.SIZE, model.getBoardSize());
    }

    @Test
    void emptyEditableCellCanBeChanged() {
        Model model = new Model();
        int row = findEditableRow(model);
        int column = findEditableColumn(model, row);

        model.setCellValue(row, column, 1);

        assertEquals(1, model.getCellValue(row, column));
    }

    @Test
    void undoRestoresPreviousValue() {
        Model model = new Model();
        int row = findEditableRow(model);
        int column = findEditableColumn(model, row);

        model.setCellValue(row, column, 2);
        model.undoLastMove();

        assertFalse(model.canUndo());
        assertEquals(Board.EMPTY, model.getCellValue(row, column));
    }

    @Test
    void fixedCellCannotBeChanged() {
        Model model = new Model();
        Position fixedPosition = findFixedPosition(model);

        assertThrows(
                IllegalStateException.class,
                () -> model.setCellValue(fixedPosition.getRow(), fixedPosition.getColumn(), 1)
        );
    }

    @Test
    void hintFillsCellWithSolvedValue() {
        Model model = new Model(new PuzzleLoader(), new Random(0));
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
        Model model = new Model();
        model.setHintEnabled(false);

        assertFalse(model.canHint());
        assertThrows(IllegalStateException.class, model::applyHint);
    }

    @Test
    void conflictingEntriesMakeBoardInvalid() {
        Model model = new Model(new PuzzleLoader(), new Random(0));
        Position first = findEditablePosition(model);
        Position second = findSecondEditablePositionInSameRow(model, first.getRow(), first.getColumn());

        model.setCellValue(first.getRow(), first.getColumn(), 1);
        model.setCellValue(second.getRow(), second.getColumn(), 1);

        assertTrue(model.isCellInvalid(first.getRow(), first.getColumn()));
        assertTrue(model.isCellInvalid(second.getRow(), second.getColumn()));
        assertFalse(model.isBoardValid());
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
