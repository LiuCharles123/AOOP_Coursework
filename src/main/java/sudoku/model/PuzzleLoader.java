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
 */
public final class PuzzleLoader {
    private static final String DEFAULT_RESOURCE = "/puzzles.txt";

    public List<Puzzle> loadPuzzles() {
        return loadPuzzles(DEFAULT_RESOURCE);
    }

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
        return new Puzzle(line, grid);
    }
}
