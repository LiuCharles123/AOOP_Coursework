package sudoku.gui;

import sudoku.model.Model;

import javax.swing.JFrame;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("deprecation")
public class SudokuView extends JFrame implements Observer {
    public SudokuView() {
        super("Sudoku");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
    }

    @Override
    public void update(Observable observable, Object argument) {
        if (observable instanceof Model) {
            repaint();
        }
    }
}
