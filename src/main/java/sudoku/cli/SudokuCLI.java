package sudoku.cli;

import sudoku.model.Board;
import sudoku.model.Cell;
import sudoku.model.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Command-line interface that talks directly to the shared Sudoku model.
 */
public class SudokuCLI {
    private final Model model;
    private final Scanner scanner;

    public SudokuCLI(Model model) {
        this(model, new Scanner(System.in));
    }

    SudokuCLI(Model model, Scanner scanner) {
        if (model == null || scanner == null) {
            throw new IllegalArgumentException("Model and scanner must be non-null.");
        }
        this.model = model;
        this.scanner = scanner;
    }

    public void start() {
        printWelcome();
        printBoard();

        boolean running = true;
        while (running && scanner.hasNextLine()) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            running = handleCommand(line);
        }
    }

    public void printBoard() {
        System.out.println();
        System.out.println("    1 2 3   4 5 6   7 8 9");
        for (int row = 0; row < Board.SIZE; row++) {
            if (row > 0 && row % Board.BOX_SIZE == 0) {
                System.out.println("  +-------+-------+-------+");
            }
            System.out.print((row + 1) + " | ");
            for (int column = 0; column < Board.SIZE; column++) {
                if (column > 0 && column % Board.BOX_SIZE == 0) {
                    System.out.print("| ");
                }
                int value = model.getCellValue(row, column);
                char display = value == Board.EMPTY ? '.' : Character.forDigit(value, 10);
                System.out.print(display + " ");
            }
            System.out.println("|");
        }
        System.out.println();
    }

    private boolean handleCommand(String line) {
        String[] parts = line.split("\\s+");
        String command = parts[0].toLowerCase();

        try {
            switch (command) {
                case "set":
                    handleSet(parts);
                    return true;
                case "clear":
                    handleClear(parts);
                    return true;
                case "undo":
                    handleUndo(parts);
                    return true;
                case "hint":
                    handleHint(parts);
                    return true;
                case "reset":
                    handleReset(parts);
                    return true;
                case "new":
                    handleNewGame(parts);
                    return true;
                case "board":
                    requireArgumentCount(parts, 1, "Usage: board");
                    printBoard();
                    return true;
                case "help":
                    requireArgumentCount(parts, 1, "Usage: help");
                    printHelp();
                    return true;
                case "quit":
                case "exit":
                    requireArgumentCount(parts, 1, "Usage: quit");
                    System.out.println("Exiting Sudoku CLI.");
                    return false;
                default:
                    System.out.println("Unknown command. Type 'help' to see the available commands.");
                    return true;
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            System.out.println(exception.getMessage());
            return true;
        }
    }

    private void handleSet(String[] parts) {
        requireArgumentCount(parts, 4, "Usage: set <row> <column> <value>");
        int row = parseBoardIndex(parts[1], "row");
        int column = parseBoardIndex(parts[2], "column");
        int value = parseDigit(parts[3]);

        model.setCellValue(row, column, value);
        System.out.println("Set row " + (row + 1) + ", column " + (column + 1) + " to " + value + ".");
        printBoard();
        printValidationFeedback();
        printCompletionMessageIfNeeded();
    }

    private void handleClear(String[] parts) {
        requireArgumentCount(parts, 3, "Usage: clear <row> <column>");
        int row = parseBoardIndex(parts[1], "row");
        int column = parseBoardIndex(parts[2], "column");

        model.clearCell(row, column);
        System.out.println("Cleared row " + (row + 1) + ", column " + (column + 1) + ".");
        printBoard();
        printValidationFeedback();
    }

    private void handleUndo(String[] parts) {
        requireArgumentCount(parts, 1, "Usage: undo");
        if (!model.canUndo()) {
            System.out.println("There is no move to undo.");
            return;
        }

        model.undoLastMove();
        System.out.println("Undid the most recent move.");
        printBoard();
        printValidationFeedback();
        printCompletionMessageIfNeeded();
    }

    private void handleHint(String[] parts) {
        requireArgumentCount(parts, 1, "Usage: hint");
        Cell hintedCell = model.applyHint();
        System.out.println(
                "Hint placed " + hintedCell.getValue()
                        + " at row " + (hintedCell.getRow() + 1)
                        + ", column " + (hintedCell.getColumn() + 1) + "."
        );
        printBoard();
        printValidationFeedback();
        printCompletionMessageIfNeeded();
    }

    private void handleReset(String[] parts) {
        requireArgumentCount(parts, 1, "Usage: reset");
        model.resetPuzzle();
        System.out.println("Puzzle reset to its initial state.");
        printBoard();
        printValidationFeedback();
    }

    private void handleNewGame(String[] parts) {
        requireArgumentCount(parts, 1, "Usage: new");
        model.loadNewGame();
        System.out.println("Loaded a new puzzle.");
        printBoard();
        printValidationFeedback();
    }

    private void printWelcome() {
        System.out.println("Sudoku CLI");
        printHelp();
    }

    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("  board                 Show the current board");
        System.out.println("  set r c v             Put digit v (1-9) at row r, column c");
        System.out.println("  clear r c             Clear the editable cell at row r, column c");
        System.out.println("  undo                  Undo the most recent move");
        System.out.println("  hint                  Reveal one correct value");
        System.out.println("  reset                 Restore the current puzzle");
        System.out.println("  new                   Load a new puzzle");
        System.out.println("  help                  Show this help message");
        System.out.println("  quit                  Exit the program");
        System.out.println("Rows and columns use 1-9 indexing.");
        System.out.println();
    }

    private void printValidationFeedback() {
        if (!model.isValidationFeedbackEnabled() || !model.hasValidationErrors()) {
            return;
        }

        List<String> invalidCells = new ArrayList<>();
        for (Cell cell : model.getCells()) {
            if (cell.isInvalid()) {
                invalidCells.add(
                        "r" + (cell.getRow() + 1)
                                + "c" + (cell.getColumn() + 1)
                                + "=" + cell.getValue()
                );
            }
        }

        if (!invalidCells.isEmpty()) {
            System.out.println("Warning: the board currently has conflicts at " + String.join(", ", invalidCells) + ".");
        }
    }

    private void printCompletionMessageIfNeeded() {
        if (model.isComplete()) {
            System.out.println("Congratulations. The puzzle is complete and valid.");
        }
    }

    private void requireArgumentCount(String[] parts, int expectedCount, String usageMessage) {
        if (parts.length != expectedCount) {
            throw new IllegalArgumentException(usageMessage);
        }
    }

    private int parseBoardIndex(String token, String label) {
        int parsed = parseInteger(token, label);
        if (parsed < 1 || parsed > Board.SIZE) {
            throw new IllegalArgumentException(
                    "The " + label + " must be an integer in the range 1.." + Board.SIZE + "."
            );
        }
        return parsed - 1;
    }

    private int parseDigit(String token) {
        int parsed = parseInteger(token, "value");
        if (parsed < 1 || parsed > Board.SIZE) {
            throw new IllegalArgumentException("The value must be an integer in the range 1..9.");
        }
        return parsed;
    }

    private int parseInteger(String token, String label) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("The " + label + " must be an integer.");
        }
    }
}
