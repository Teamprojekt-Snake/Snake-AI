import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;

public class Snake_GUI extends JFrame {



    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Snake_GUI frame = new Snake_GUI();
            frame.setVisible(true);

        });
    }

    /**
     * Create the frame.
     */
    public Snake_GUI() {
        setTitle("Snake");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 600);

        BoardPanel board = new BoardPanel();
        setContentPane(board);
        board.requestFocusInWindow();


    }

}
