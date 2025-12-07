import java.awt.EventQueue;

import javax.swing.JFrame;

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
        setLocationRelativeTo(null);
        //TODO: Fullscreen button einfügen -> set resizable muss dann ausgestellt werden glaube ich um es zuzulassen
        // und dann wieder an sobald man den rausnimmt
        setResizable(false);

        BoardPanel board = new BoardPanel();
        setContentPane(board);
        board.requestFocusInWindow();


    }

}
