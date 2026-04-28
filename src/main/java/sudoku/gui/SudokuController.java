package sudoku.gui;

import sudoku.model.Cell;
import sudoku.model.Model;

public class SudokuController {
    private final Model model;
    private final SudokuView view;

    private int selectedRow;
    private int selectedColumn;
    private boolean hasSelection;

    public SudokuController(Model model, SudokuView view) {
        if (model == null || view == null) {
            throw new IllegalArgumentException("Model and view must be non-null.");
        }
        this.model = model;
        this.view = view;
        this.model.addObserver(view);
        this.view.setController(this);
    }

    public Model getModel() {
        return model;
    }

    public SudokuView getView() {
        return view;
    }

    public void selectCell(int row, int column) {
        selectedRow = row;
        selectedColumn = column;
        hasSelection = true;
        view.refreshBoard();
        view.showSelectionStatus();
    }

    public void enterDigit(int value) {
        if (!hasSelection()) {
            view.showStatus("Select a cell before entering a digit.");
            return;
        }

        try {
            model.setCellValue(selectedRow, selectedColumn, value);
            view.showStatus("Placed " + value + " at row " + (selectedRow + 1) + ", column " + (selectedColumn + 1) + ".");
            view.refreshBoard();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            view.showStatus(exception.getMessage());
        }
    }

    public void clearSelectedCell() {
        if (!hasSelection()) {
            view.showStatus("Select a cell before clearing it.");
            return;
        }

        try {
            model.clearCell(selectedRow, selectedColumn);
            view.showStatus("Cleared row " + (selectedRow + 1) + ", column " + (selectedColumn + 1) + ".");
            view.refreshBoard();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            view.showStatus(exception.getMessage());
        }
    }

    public void undo() {
        if (!model.canUndo()) {
            view.showStatus("There is no move to undo.");
            return;
        }

        model.undoLastMove();
        view.showStatus("Undid the most recent move.");
        view.refreshBoard();
    }

    public void applyHint() {
        try {
            Cell hintedCell = model.applyHint();
            selectedRow = hintedCell.getRow();
            selectedColumn = hintedCell.getColumn();
            hasSelection = true;
            view.showStatus(
                    "Hint placed " + hintedCell.getValue()
                            + " at row " + (hintedCell.getRow() + 1)
                            + ", column " + (hintedCell.getColumn() + 1) + "."
            );
            view.refreshBoard();
        } catch (IllegalStateException exception) {
            view.showStatus(exception.getMessage());
        }
    }

    public void resetPuzzle() {
        model.resetPuzzle();
        view.showStatus("Puzzle reset to its initial state.");
        view.refreshBoard();
    }

    public void loadNewGame() {
        model.loadNewGame();
        hasSelection = false;
        view.showStatus("Loaded a new puzzle.");
        view.refreshBoard();
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedColumn() {
        return selectedColumn;
    }

    public boolean hasSelection() {
        return hasSelection;
    }
}
