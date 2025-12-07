import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;

public class BoardPanel extends JPanel {

    private final int TILE = 20;
    private int score = 0;


    // Schlange, besteht aus Punkten
    private final List<Point> snake = new LinkedList<>();

    // Richtung in Grid-Steps
    private int dx = 0;
    private int dy = 0;

    // Apfel (feste Position)
    private Point apple = new Point(10, 10);

    private Timer gameTimer;
    private boolean running = false;

    private void spawnApple() {
        int maxX = getWidth() / TILE;
        int maxY = getHeight() / TILE;

        if (maxX <= 0) maxX = 20;  // fallback
        if (maxY <= 0) maxY = 20;

        Point newApple;

        do {
            // apfel spawnt zwichen 0% und 90% von der mitte des feldes nie ganz außen vllt kann man so mit den schwirigkeiten spielen
            int x = (int) (Math.random() * (maxX * 0.8)) + (int)(maxX * 0.1);
            int y = (int) (Math.random() * (maxY * 0.8)) + (int)(maxY * 0.1);
            newApple = new Point(x, y);
        } while (snake.contains(newApple));

        apple = newApple;
        GameLogger.fine("New apple spawned at: " + apple);
    }

    public BoardPanel() {
        GameLogger.info("Game started!");
        setBackground(Color.GRAY);
        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyHandler());

        initGame();
        startGame();
    }

    private void initGame() {
        snake.clear();
        score = 0;
        snake.add(new Point(5, 5));

        dx = 0;
        dy = 0;

        spawnApple();
        GameLogger.info("Game initialized");
    }

    private void startGame() {
        if (running) {
            GameLogger.warning("Game already running");
            return;
        }

        running = true;

        // Timer: alle 200ms wird update() + repaint() aufgerufen
        gameTimer = new Timer(70, e -> {
            update();
            repaint();
        });

        gameTimer.start();
        GameLogger.info("Game timer started");
    }

    private void update() {
        if (dx == 0 && dy == 0) return;

        Point head = snake.getFirst();
        Point newHead = new Point(head.x + dx, head.y + dy);

        LinkedList<Point> snakeCopy = new LinkedList<>(snake);
        snakeCopy.removeFirst();
        if (snakeCopy.contains(newHead)) {
            gameOver();
            return;
        }

        if (newHead.x < 0 || newHead.y < 0 ||
                newHead.x * TILE >= getWidth() ||
                newHead.y * TILE >= getHeight()) {
            GameLogger.warning("Game Over - Wall collision at: " + newHead);
            gameOver();
            return;
        }

        boolean ateApple = newHead.equals(apple);

        snake.addFirst(newHead);

        if (!ateApple) {
            snake.removeLast();
        } else {
            score ++;
            GameLogger.info("Apple eaten! Snake length: " + snake.size());
            spawnApple();
        }
    }

    private void gameOver() {
        running = false;

        if (gameTimer != null) {
            gameTimer.stop();  // Timer stoppen - fertig!
            GameLogger.info("Game timer stopped");
        }

        String[] options = {"Neues Spiel", "Spiel Beenden"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Game Over! Score: " + score,
                "Snake",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            initGame();
            startGame();
        } else {
            System.exit(0);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //Apfel
        g.setColor(Color.RED);
        g.fillRect(apple.x * TILE, apple.y * TILE, TILE, TILE);

        //Schlange
        g.setColor(Color.GREEN);
        for (Point p : snake) {
            g.fillRect(p.x * TILE, p.y * TILE, TILE, TILE);
        }

        //Score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 30);


        Toolkit.getDefaultToolkit().sync();
    }

    private class KeyHandler extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (key == KeyEvent.VK_UP && dy != 1) {
                dx = 0; dy = -1;
                GameLogger.fine("Direction changed: UP");
            }
            if (key == KeyEvent.VK_DOWN && dy != -1) {
                dx = 0; dy = 1;
                GameLogger.fine("Direction changed: DOWN");
            }
            if (key == KeyEvent.VK_LEFT && dx != 1) {
                dx = -1; dy = 0;
                GameLogger.fine("Direction changed: LEFT");
            }
            if (key == KeyEvent.VK_RIGHT && dx != -1) {
                dx = 1; dy = 0;
                GameLogger.fine("Direction changed: RIGHT");
            }
        }
    }
}
