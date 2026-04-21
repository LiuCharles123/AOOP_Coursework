package sudoku.cli;

import sudoku.model.Board;
import sudoku.model.Model;

public class SudokuCLI {
    private final Model model;

    public SudokuCLI(Model model) {
        this.model = model;
    }

    public void start() {
        System.out.println("Sudoku CLI scaffold ready.");
        printBoard();
        System.out.println("Commands will be added next.");
    }

    public void printBoard() {
        for (int row = 0; row < Board.SIZE; row++) {
            if (row > 0 && row % Board.BOX_SIZE == 0) {
                System.out.println("------+-------+------");
            }
            for (int column = 0; column < Board.SIZE; column++) {
                if (column > 0 && column % Board.BOX_SIZE == 0) {
                    System.out.print("| ");
                }
                int value = model.getCellValue(row, column);
                char display = value == Board.EMPTY ? '.' : Character.forDigit(value, 10);
                System.out.print(display + " ");
            }
            System.out.println();
        }
    }
}
