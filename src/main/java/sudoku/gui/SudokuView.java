package sudoku.gui;

import sudoku.model.Board;
import sudoku.model.Cell;
import sudoku.model.Model;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("deprecation")
public class SudokuView extends JFrame implements Observer {
    private static final Color FRAME_BACKGROUND = new Color(243, 246, 251);
    private static final Color FIXED_BACKGROUND = new Color(224, 231, 242);
    private static final Color EDITABLE_BACKGROUND = Color.WHITE;
    private static final Color SELECTED_BACKGROUND = new Color(255, 243, 205);
    private static final Color INVALID_BACKGROUND = new Color(248, 215, 218);
    private static final Color FIXED_TEXT = new Color(35, 58, 87);
    private static final Color EDITABLE_TEXT = new Color(25, 82, 168);
    private static final Font CELL_FONT = new Font("SansSerif", Font.BOLD, 28);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.PLAIN, 16);

    private final Model model;
    private final JButton[][] cellButtons;
    private final JButton[] keypadButtons;
    private final JButton eraseButton;
    private final JButton undoButton;
    private final JButton hintButton;
    private final JButton resetButton;
    private final JButton newGameButton;
    private final JCheckBox validationFeedbackCheckBox;
    private final JCheckBox hintEnabledCheckBox;
    private final JCheckBox randomSelectionCheckBox;
    private final JLabel statusLabel;

    private SudokuController controller;

    public SudokuView(Model model) {
        super("Sudoku");
        if (model == null) {
            throw new IllegalArgumentException("Model must be non-null.");
        }

        this.model = model;
        this.cellButtons = new JButton[Board.SIZE][Board.SIZE];
        this.keypadButtons = new JButton[Board.SIZE];
        this.eraseButton = new JButton("Erase");
        this.undoButton = new JButton("Undo");
        this.hintButton = new JButton("Hint");
        this.resetButton = new JButton("Reset");
        this.newGameButton = new JButton("New Game");
        this.validationFeedbackCheckBox = new JCheckBox("Validation Feedback");
        this.hintEnabledCheckBox = new JCheckBox("Hints Enabled");
        this.randomSelectionCheckBox = new JCheckBox("Random Puzzle");
        this.statusLabel = new JLabel("Select a cell to begin.");

        buildUi();
        installKeyBindings();
        refreshBoard();
    }

    public void setController(SudokuController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("Controller must be non-null.");
        }
        this.controller = controller;
        bindActions();
        refreshBoard();
        syncFlags();
    }

    @Override
    public void update(Observable observable, Object argument) {
        if (observable instanceof Model) {
            refreshBoard();
        }
    }

    public void refreshBoard() {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                Cell cell = model.getCell(row, column);
                JButton button = cellButtons[row][column];
                button.setText(cell.getValue() == Board.EMPTY ? "" : Integer.toString(cell.getValue()));
                button.setForeground(cell.isFixed() ? FIXED_TEXT : EDITABLE_TEXT);
                button.setBackground(resolveCellBackground(cell, row, column));
                button.setEnabled(true);
            }
        }
        syncFlags();
    }

    public void showStatus(String message) {
        statusLabel.setText(message);
    }

    public void showSelectionStatus() {
        if (controller != null && controller.hasSelection()) {
            int row = controller.getSelectedRow();
            int column = controller.getSelectedColumn();
            Cell selectedCell = model.getCell(row, column);
            if (selectedCell.isFixed()) {
                showStatus("Selected fixed cell at row " + (row + 1) + ", column " + (column + 1) + ".");
            } else {
                showStatus("Selected editable cell at row " + (row + 1) + ", column " + (column + 1) + ".");
            }
        } else {
            showStatus("Select a cell to begin.");
        }
    }

    public void syncFlags() {
        syncCheckBox(validationFeedbackCheckBox, model.isValidationFeedbackEnabled());
        syncCheckBox(hintEnabledCheckBox, model.isHintEnabled());
        syncCheckBox(randomSelectionCheckBox, model.isRandomPuzzleSelectionEnabled());
    }

    public void updateActionAvailability(boolean canEditSelection, boolean canUndo, boolean canHint) {
        eraseButton.setEnabled(canEditSelection);
        undoButton.setEnabled(canUndo);
        hintButton.setEnabled(canHint);
        for (JButton keypadButton : keypadButtons) {
            keypadButton.setEnabled(canEditSelection);
        }
    }

    public void showCompletionDialog() {
        JOptionPane.showMessageDialog(
                this,
                "Congratulations. The puzzle is correctly completed.",
                "Puzzle Complete",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void buildUi() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 760));
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout(18, 18));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        rootPanel.setBackground(FRAME_BACKGROUND);

        rootPanel.add(createBoardPanel(), BorderLayout.CENTER);
        rootPanel.add(createSidePanel(), BorderLayout.EAST);
        rootPanel.add(statusLabel, BorderLayout.SOUTH);

        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

        setContentPane(rootPanel);
        pack();
    }

    private JPanel createBoardPanel() {
        JPanel boardPanel = new JPanel(new GridLayout(Board.SIZE, Board.SIZE));
        boardPanel.setBackground(new Color(20, 40, 70));
        boardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(20, 40, 70), 3),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        boardPanel.setPreferredSize(new Dimension(630, 630));

        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                JButton button = new JButton();
                button.setFont(CELL_FONT);
                button.setFocusPainted(false);
                button.setMargin(new Insets(0, 0, 0, 0));
                button.setOpaque(true);
                button.setHorizontalAlignment(SwingConstants.CENTER);
                button.setPreferredSize(new Dimension(70, 70));
                button.setBorder(createCellBorder(row, column));

                final int targetRow = row;
                final int targetColumn = column;
                button.addActionListener(event -> {
                    if (controller != null) {
                        controller.selectCell(targetRow, targetColumn);
                    }
                });

                cellButtons[row][column] = button;
                boardPanel.add(button);
            }
        }
        return boardPanel;
    }

    private JPanel createSidePanel() {
        JPanel sidePanel = new JPanel(new BorderLayout(12, 12));
        sidePanel.setBackground(FRAME_BACKGROUND);
        sidePanel.setPreferredSize(new Dimension(220, 630));

        JLabel keypadLabel = new JLabel("Number Pad");
        keypadLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        JPanel keypadPanel = new JPanel(new GridLayout(3, 3, 8, 8));
        keypadPanel.setBackground(FRAME_BACKGROUND);
        for (int value = 1; value <= Board.SIZE; value++) {
            JButton button = new JButton(Integer.toString(value));
            button.setFont(BUTTON_FONT);
            button.setFocusPainted(false);
            keypadButtons[value - 1] = button;
            keypadPanel.add(button);
        }

        JPanel controlsPanel = new JPanel(new GridLayout(5, 1, 8, 8));
        controlsPanel.setBackground(FRAME_BACKGROUND);
        JButton[] controlButtons = {eraseButton, undoButton, hintButton, resetButton, newGameButton};
        for (JButton button : controlButtons) {
            button.setFont(BUTTON_FONT);
            button.setFocusPainted(false);
            controlsPanel.add(button);
        }

        JPanel flagsPanel = new JPanel(new GridLayout(3, 1, 6, 6));
        flagsPanel.setBackground(FRAME_BACKGROUND);
        JCheckBox[] checkBoxes = {validationFeedbackCheckBox, hintEnabledCheckBox, randomSelectionCheckBox};
        for (JCheckBox checkBox : checkBoxes) {
            checkBox.setBackground(FRAME_BACKGROUND);
            checkBox.setFont(new Font("SansSerif", Font.PLAIN, 15));
            flagsPanel.add(checkBox);
        }

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setBackground(FRAME_BACKGROUND);
        topPanel.add(keypadLabel, BorderLayout.NORTH);
        topPanel.add(keypadPanel, BorderLayout.CENTER);

        sidePanel.add(topPanel, BorderLayout.NORTH);
        sidePanel.add(controlsPanel, BorderLayout.CENTER);
        sidePanel.add(flagsPanel, BorderLayout.SOUTH);
        return sidePanel;
    }

    private void bindActions() {
        for (int value = 1; value <= Board.SIZE; value++) {
            final int digit = value;
            keypadButtons[value - 1].addActionListener(event -> controller.enterDigit(digit));
        }

        eraseButton.addActionListener(event -> controller.clearSelectedCell());
        undoButton.addActionListener(event -> controller.undo());
        hintButton.addActionListener(event -> controller.applyHint());
        resetButton.addActionListener(event -> controller.resetPuzzle());
        newGameButton.addActionListener(event -> controller.loadNewGame());
        validationFeedbackCheckBox.addActionListener(
                event -> controller.setValidationFeedbackEnabled(validationFeedbackCheckBox.isSelected())
        );
        hintEnabledCheckBox.addActionListener(
                event -> controller.setHintEnabled(hintEnabledCheckBox.isSelected())
        );
        randomSelectionCheckBox.addActionListener(
                event -> controller.setRandomPuzzleSelectionEnabled(randomSelectionCheckBox.isSelected())
        );
    }

    private void installKeyBindings() {
        for (int value = 1; value <= Board.SIZE; value++) {
            int digit = value;
            bindKey("digit-" + value, KeyStroke.getKeyStroke(Integer.toString(value)), () -> {
                if (controller != null) {
                    controller.enterDigit(digit);
                }
            });
            bindKey("numpad-" + value, KeyStroke.getKeyStroke("NUMPAD" + value), () -> {
                if (controller != null) {
                    controller.enterDigit(digit);
                }
            });
        }

        bindKey("clear-delete", KeyStroke.getKeyStroke("DELETE"), () -> {
            if (controller != null) {
                controller.clearSelectedCell();
            }
        });
        bindKey("clear-backspace", KeyStroke.getKeyStroke("BACK_SPACE"), () -> {
            if (controller != null) {
                controller.clearSelectedCell();
            }
        });
        bindKey("clear-zero", KeyStroke.getKeyStroke("0"), () -> {
            if (controller != null) {
                controller.clearSelectedCell();
            }
        });
        bindKey("up", KeyStroke.getKeyStroke("UP"), () -> moveSelection(-1, 0));
        bindKey("down", KeyStroke.getKeyStroke("DOWN"), () -> moveSelection(1, 0));
        bindKey("left", KeyStroke.getKeyStroke("LEFT"), () -> moveSelection(0, -1));
        bindKey("right", KeyStroke.getKeyStroke("RIGHT"), () -> moveSelection(0, 1));
    }

    private void bindKey(String name, KeyStroke keyStroke, Runnable action) {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, name);
        getRootPane().getActionMap().put(name, new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }

    private void moveSelection(int rowOffset, int columnOffset) {
        if (controller == null) {
            return;
        }

        int startRow = controller.hasSelection() ? controller.getSelectedRow() : 0;
        int startColumn = controller.hasSelection() ? controller.getSelectedColumn() : 0;
        int targetRow = clamp(startRow + rowOffset);
        int targetColumn = clamp(startColumn + columnOffset);
        controller.selectCell(targetRow, targetColumn);
    }

    private int clamp(int index) {
        return Math.max(0, Math.min(Board.SIZE - 1, index));
    }

    private Border createCellBorder(int row, int column) {
        int top = row % Board.BOX_SIZE == 0 ? 3 : 1;
        int left = column % Board.BOX_SIZE == 0 ? 3 : 1;
        int bottom = row == Board.SIZE - 1 ? 3 : 1;
        int right = column == Board.SIZE - 1 ? 3 : 1;
        return BorderFactory.createMatteBorder(top, left, bottom, right, new Color(20, 40, 70));
    }

    private Color resolveCellBackground(Cell cell, int row, int column) {
        if (controller != null && controller.hasSelection()
                && controller.getSelectedRow() == row && controller.getSelectedColumn() == column) {
            return SELECTED_BACKGROUND;
        }
        if (cell.isInvalid()) {
            return INVALID_BACKGROUND;
        }
        return cell.isFixed() ? FIXED_BACKGROUND : EDITABLE_BACKGROUND;
    }

    private void syncCheckBox(JCheckBox checkBox, boolean selected) {
        if (checkBox.isSelected() != selected) {
            checkBox.setSelected(selected);
        }
    }
}
