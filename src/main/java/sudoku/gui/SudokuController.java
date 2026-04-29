package sudoku.gui;

import sudoku.model.Cell;
import sudoku.model.Model;

public class SudokuController {
    private final Model model;
    private final SudokuView view;

    private int selectedRow;
    private int selectedColumn;
    private boolean hasSelection;
    private boolean completionShown;

    public SudokuController(Model model, SudokuView view) {
        if (model == null || view == null) {
            throw new IllegalArgumentException("Model and view must be non-null.");
        }
        this.model = model;
        this.view = view;
        this.model.addObserver(view);
        this.view.setController(this);
        refreshControls();
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
        refreshControls();
    }

    public void enterDigit(int value) {
        if (!hasSelection()) {
            view.showStatus("Select a cell before entering a digit.");
            refreshControls();
            return;
        }

        if (!model.isEditable(selectedRow, selectedColumn)) {
            view.showStatus("Pre-filled cells cannot be edited.");
            refreshControls();
            return;
        }

        try {
            boolean wasComplete = model.isComplete();
            model.setCellValue(selectedRow, selectedColumn, value);
            view.showStatus("Placed " + value + " at row " + (selectedRow + 1) + ", column " + (selectedColumn + 1) + ".");
            view.refreshBoard();
            handleCompletionAfterBoardChange(wasComplete);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            view.showStatus(exception.getMessage());
        }
        refreshControls();
    }

    public void clearSelectedCell() {
        if (!hasSelection()) {
            view.showStatus("Select a cell before clearing it.");
            refreshControls();
            return;
        }

        if (!model.isEditable(selectedRow, selectedColumn)) {
            view.showStatus("Pre-filled cells cannot be cleared.");
            refreshControls();
            return;
        }

        try {
            boolean wasComplete = model.isComplete();
            model.clearCell(selectedRow, selectedColumn);
            view.showStatus("Cleared row " + (selectedRow + 1) + ", column " + (selectedColumn + 1) + ".");
            view.refreshBoard();
            handleCompletionAfterBoardChange(wasComplete);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            view.showStatus(exception.getMessage());
        }
        refreshControls();
    }

    public void undo() {
        if (!model.canUndo()) {
            view.showStatus("There is no move to undo.");
            refreshControls();
            return;
        }

        boolean wasComplete = model.isComplete();
        model.undoLastMove();
        view.showStatus("Undid the most recent move.");
        view.refreshBoard();
        handleCompletionAfterBoardChange(wasComplete);
        refreshControls();
    }

    public void applyHint() {
        if (!model.canHint()) {
            view.showStatus(model.isHintEnabled() ? "No hint is available." : "Hint functionality is disabled.");
            refreshControls();
            return;
        }

        try {
            boolean wasComplete = model.isComplete();
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
            handleCompletionAfterBoardChange(wasComplete);
        } catch (IllegalStateException exception) {
            view.showStatus(exception.getMessage());
        }
        refreshControls();
    }

    public void resetPuzzle() {
        model.resetPuzzle();
        completionShown = false;
        view.showStatus("Puzzle reset to its initial state.");
        view.refreshBoard();
        refreshControls();
    }

    public void loadNewGame() {
        model.loadNewGame();
        hasSelection = false;
        completionShown = false;
        view.showStatus("Loaded a new puzzle.");
        view.refreshBoard();
        refreshControls();
    }

    public void setValidationFeedbackEnabled(boolean enabled) {
        model.setValidationFeedbackEnabled(enabled);
        view.showStatus(enabled ? "Validation feedback enabled." : "Validation feedback disabled.");
        view.refreshBoard();
        refreshControls();
    }

    public void setHintEnabled(boolean enabled) {
        model.setHintEnabled(enabled);
        view.showStatus(enabled ? "Hints enabled." : "Hints disabled.");
        view.refreshBoard();
        refreshControls();
    }

    public void setRandomPuzzleSelectionEnabled(boolean enabled) {
        model.setRandomPuzzleSelectionEnabled(enabled);
        view.showStatus(enabled ? "Random puzzle selection enabled." : "Fixed puzzle selection enabled.");
        view.refreshBoard();
        refreshControls();
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

    private void refreshControls() {
        boolean canEditSelection = hasSelection && model.isEditable(selectedRow, selectedColumn);
        view.updateActionAvailability(canEditSelection, model.canUndo(), model.canHint());
        view.syncFlags();
    }

    private void handleCompletionAfterBoardChange(boolean wasComplete) {
        boolean isComplete = model.isComplete();
        if (!wasComplete && isComplete && !completionShown) {
            completionShown = true;
            view.showCompletionDialog();
        }
        if (!isComplete) {
            completionShown = false;
        }
    }
}
