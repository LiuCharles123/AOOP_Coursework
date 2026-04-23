package sudoku.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
