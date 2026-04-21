package sudoku.gui;

import sudoku.model.Model;

public class SudokuController {
    private final Model model;
    private final SudokuView view;

    public SudokuController(Model model, SudokuView view) {
        this.model = model;
        this.view = view;
        this.model.addObserver(view);
    }

    public Model getModel() {
        return model;
    }

    public SudokuView getView() {
        return view;
    }
}
