package sudoku.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads puzzle definitions from a classpath resource.
 *
 * <p>Invariant: each loaded puzzle has exactly 81 digits, a 9x9 initial grid, and a solvable
 * 9x9 solution grid.
 */
public final class PuzzleLoader {
    private static final String DEFAULT_RESOURCE = "/puzzles.txt";

    /**
     * Postcondition: returns a non-empty immutable list of valid puzzles loaded from the default
     * bundled resource.
     */
    public List<Puzzle> loadPuzzles() {
        return loadPuzzles(DEFAULT_RESOURCE);
    }

    /**
     * Precondition: resourcePath names a readable classpath resource containing one puzzle per
     * non-empty line.
     * Postcondition: returns a non-empty immutable list of valid puzzles parsed from resourcePath.
     */
    public List<Puzzle> loadPuzzles(String resourcePath) {
        InputStream inputStream = PuzzleLoader.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IllegalStateException("Could not find puzzle resource: " + resourcePath);
        }

        List<Puzzle> puzzles = new ArrayList<>();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    puzzles.add(parsePuzzle(trimmed));
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read puzzle resource: " + resourcePath, exception);
        }

        if (puzzles.isEmpty()) {
            throw new IllegalStateException("No puzzles were loaded from: " + resourcePath);
        }
        assert !puzzles.isEmpty();
        return Collections.unmodifiableList(puzzles);
    }

    private Puzzle parsePuzzle(String line) {
        if (line.length() != Puzzle.CELL_COUNT) {
            throw new IllegalArgumentException("Each puzzle line must contain exactly 81 digits.");
        }

        int[][] grid = new int[Puzzle.BOARD_SIZE][Puzzle.BOARD_SIZE];
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (!Character.isDigit(character)) {
                throw new IllegalArgumentException("Puzzle lines must contain digits only.");
            }
            int row = index / Puzzle.BOARD_SIZE;
            int column = index % Puzzle.BOARD_SIZE;
            grid[row][column] = character - '0';
        }

        int[][] solvedGrid = copyGrid(grid);
        if (!solve(solvedGrid)) {
            throw new IllegalArgumentException("Puzzle line does not describe a solvable Sudoku puzzle.");
        }

        return new Puzzle(line, grid, solvedGrid);
    }

    private boolean solve(int[][] grid) {
        for (int row = 0; row < Puzzle.BOARD_SIZE; row++) {
            for (int column = 0; column < Puzzle.BOARD_SIZE; column++) {
                if (grid[row][column] == Board.EMPTY) {
                    for (int candidate = 1; candidate <= Board.SIZE; candidate++) {
                        if (isValidPlacement(grid, row, column, candidate)) {
                            grid[row][column] = candidate;
                            if (solve(grid)) {
                                return true;
                            }
                            grid[row][column] = Board.EMPTY;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValidPlacement(int[][] grid, int row, int column, int value) {
        for (int index = 0; index < Board.SIZE; index++) {
            if (grid[row][index] == value || grid[index][column] == value) {
                return false;
            }
        }

        int firstRow = (row / Board.BOX_SIZE) * Board.BOX_SIZE;
        int firstColumn = (column / Board.BOX_SIZE) * Board.BOX_SIZE;
        for (int rowOffset = 0; rowOffset < Board.BOX_SIZE; rowOffset++) {
            for (int columnOffset = 0; columnOffset < Board.BOX_SIZE; columnOffset++) {
                if (grid[firstRow + rowOffset][firstColumn + columnOffset] == value) {
                    return false;
                }
            }
        }
        return true;
    }

    private int[][] copyGrid(int[][] grid) {
        int[][] copy = new int[Puzzle.BOARD_SIZE][Puzzle.BOARD_SIZE];
        for (int row = 0; row < Puzzle.BOARD_SIZE; row++) {
            System.arraycopy(grid[row], 0, copy[row], 0, Puzzle.BOARD_SIZE);
        }
        return copy;
    }
}
