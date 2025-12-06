package snake;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class BoardPanel extends JPanel implements Runnable {

    private final int TILE = 20;

    // Schlange, besteht aus Punkten
    private List<Point> snake = new LinkedList<>();

    // Richtung in Grid-Steps
    private int dx = 0;
    private int dy = 0;

    // Apfel (feste Position)
    private Point apple = new Point(10, 10);

    private Thread gameThread;
    private boolean running = false;

    public BoardPanel() {

        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyHandler());

        initGame();
        startGame();
    }

    private void initGame() {
        snake.clear();

        // Kopf
        snake.add(new Point(5, 5));
    }

    private void startGame() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {

        final int FPS = 60;
        final double FRAME_TIME = 1_000_000_000.0 / FPS;

        final long MOVE_DELAY = 100_000_000; // 100 ms pro Step
        long lastMove = System.nanoTime();

        double delta = 0;
        long lastTime = System.nanoTime();

        while (running) {

            long now = System.nanoTime();
            delta += (now - lastTime) / FRAME_TIME;
            lastTime = now;

            if (now - lastMove >= MOVE_DELAY) {
                update();
                lastMove = now;
            }

            while (delta >= 1) {
                repaint();
                delta--;
            }

            try { Thread.sleep(1); } catch(Exception e){}
        }
    }

    private void update() {

        // Kopf holen
        Point head = snake.get(0);

        // Neue Kopfposition
        Point newHead = new Point(head.x + dx, head.y + dy);

        //Spielrand, Spiel endet wenn Wand berührt
        if (newHead.x < 0 || newHead.y < 0 ||
                newHead.x * TILE >= getWidth() ||
                newHead.y * TILE >= getHeight()) {

            gameOver();
            return;
        }

        //Apfel gegessen?
        boolean ateApple = newHead.equals(apple);

        // Kopf an vorderste Stelle packen
        snake.add(0, newHead);

        // Wenn kein Apfel gegessen wird der Schwanz gelöscht (sonst unendlich Schlange)
        if (!ateApple) {
            snake.remove(snake.size() - 1);
        }

    }

    private void gameOver() {
        running = false;
        JOptionPane.showMessageDialog(this, "GAME OVER!");
        System.exit(0);
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

        Toolkit.getDefaultToolkit().sync();
    }

    private class KeyHandler extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (key == KeyEvent.VK_UP && dy != 1) {
                dx = 0; dy = -1;
            }
            if (key == KeyEvent.VK_DOWN && dy != -1) {
                dx = 0; dy = 1;
            }
            if (key == KeyEvent.VK_LEFT && dx != 1) {
                dx = -1; dy = 0;
            }
            if (key == KeyEvent.VK_RIGHT && dx != -1) {
                dx = 1; dy = 0;
            }
        }
    }
}
