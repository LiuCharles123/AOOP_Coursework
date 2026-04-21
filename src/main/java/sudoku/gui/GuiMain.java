package sudoku.gui;

import sudoku.model.Model;

import javax.swing.SwingUtilities;

public final class GuiMain {
    private GuiMain() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Model model = new Model();
            SudokuView view = new SudokuView();
            new SudokuController(model, view);
            view.setVisible(true);
        });
    }
}
