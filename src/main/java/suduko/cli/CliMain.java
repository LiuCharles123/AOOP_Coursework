package sudoku.cli;

import sudoku.model.Model;

public final class CliMain {
    private CliMain() {
    }

    public static void main(String[] args) {
        Model model = new Model();
        SudokuCLI cli = new SudokuCLI(model);
        cli.start();
    }
}
