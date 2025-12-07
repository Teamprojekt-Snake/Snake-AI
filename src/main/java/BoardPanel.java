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
import javax.swing.Timer;

@SuppressWarnings("SpellCheckingInspection")
public class BoardPanel extends JPanel {

    private final static int MOVE_SPEED = 120;
    private Timer movementTimer;
    private Timer renderTimer;
    private final int TILE = 20;

    // Schlange, besteht aus Segmente
    private final List<SnakeSegment> snake = new LinkedList<>();

    //Interpolation
    private int targetX = 5;
    private int targetY = 5;
    private long lastMoveTime = 0;

    // Richtung in Grid-Steps
    private int dx = 0;
    private int dy = 0;
    private int nextDx = 0;
    private int nextDy = 0;
    private float snapshotX = 5.0f;
    private float snapshotY = 5.0f;

    // Apfel (feste Position)
    private Point apple = new Point(10, 10);

    private boolean running = false;

    public BoardPanel() {
        GameLogger.info("Game started!");
        setBackground(Color.GRAY);
        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyHandler());

        initGame();
        startGame();
    }

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

            boolean collision = false;
            for (SnakeSegment seg : snake) {
                if (seg.getGridPosition().equals(newApple)) {
                    collision = true;
                    break;
                }
            }
                if (!collision) break;
        } while (true);

        apple = newApple;
        GameLogger.fine("New apple spawned at: " + apple);
    }

    private void initGame() {
        snake.clear();
        snake.add(new SnakeSegment(5, 5));

        targetX = 5;
        targetY = 5;
        lastMoveTime = 0;
        snapshotX = 5.0f;
        snapshotY = 5.0f;

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
        lastMoveTime = System.currentTimeMillis();

        renderTimer = new Timer(16, e -> {
            updateInterpolation();
            repaint();
        });
        renderTimer.start();
        GameLogger.info("Game timers started (Movement: 200ms, Render: 60 FPS)");
    }

    private void updateInterpolation() {

        long now = System.currentTimeMillis();
        long elapsed = now - lastMoveTime;

        // nächster grid move
        if (elapsed >= MOVE_SPEED) {

            if (nextDx != 0 || nextDy != 0) {
                dx = nextDx;
                dy = nextDy;
                nextDx = 0;
                nextDy = 0;
            }

            // die Bewegung an sich
            if (dx != 0 || dy != 0) {

                snapshotX = targetX;
                snapshotY = targetY;
                targetX += dx;
                targetY += dy;

                //Kollisionen checken
                if (targetX < 0 || targetY < 0 ||
                        targetX >= getWidth() / TILE ||
                        targetY >= getHeight() / TILE) {
                    gameOver();
                    return;
                }

                //snapshots of every segment
                for (SnakeSegment seg : snake) {
                    seg.updateSnapshot();
                }

                // snake liste update
                SnakeSegment newHead = new SnakeSegment(targetX, targetY);
                SnakeSegment oldHead = snake.get(0);
                newHead.setSnapshot(oldHead.getCurrentX(), oldHead.getCurrentY());
                newHead.setCurrent(oldHead.getCurrentX(), oldHead.getCurrentY());

                //selbstkollision check
                for (SnakeSegment seg : snake) {
                    if (seg.getGridPosition().equals(newHead.getGridPosition())) {
                        gameOver();
                        return;
                    }
                }

                snake.add(0, newHead);

                boolean ateApple = (targetX == apple.x && targetY == apple.y);

                if (ateApple) {
                    GameLogger.info("Apple eaten! Snake length: " + snake.size());
                    spawnApple();
                } else {
                    snake.remove(snake.size() - 1);
                }

                int limit = ateApple ? snake.size() - 1 : snake.size();
                for (int i = limit - 1; i >= 1; i--) {
                    SnakeSegment current = snake.get(i);
                    SnakeSegment prev = snake.get(i - 1);
                    Point prevGrid = prev.getGridPosition();
                    current.setGridPosition(prevGrid.x, prevGrid.y);
                }

                // Apfel check
                lastMoveTime = now;
            }
        } else {
            // interpolation
            float progress = elapsed / (float) MOVE_SPEED;
            progress = Math.min(progress, 1.0f);


            for (int i = 0; i < snake.size(); i++) {
                SnakeSegment seg = snake.get(i);

                if (i == 0) {
                    //kopf interpoliert zum target
                    seg.interpolate(progress, targetX, targetY);
                } else {
                    //body interpoliert zur gridposition des verherigen segments
                    Point myGrid = seg.getGridPosition();
                    seg.interpolate(progress, myGrid.x, myGrid.y);
                }
            }
        }
    }

    private void gameOver() {
        running = false;

        if (renderTimer != null) renderTimer.stop();
        GameLogger.info("Game timers stopped");

        String[] options = {"Neues Spiel", "Spiel Beenden"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Game Over! Score: " + (snake.size() - 1),
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

        //Schlange interpoliert
        g.setColor(Color.GREEN);
        for (SnakeSegment seg : snake) {
            g.fillRect(
                    (int)(seg.getCurrentX() * TILE),
                    (int)(seg.getCurrentY() * TILE),
                    TILE, TILE
            );
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private class KeyHandler extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (key == KeyEvent.VK_UP && dy != 1 && nextDy != 1) {
                nextDx = 0;
                nextDy = -1;
                GameLogger.fine("Direction changed: UP");
            }
            if (key == KeyEvent.VK_DOWN && dy != -1 && nextDy != -1) {
                nextDx = 0;
                nextDy = 1;
                GameLogger.fine("Direction changed: DOWN");
            }
            if (key == KeyEvent.VK_LEFT && dx != 1 && nextDx != 1) {
                nextDx = -1;
                nextDy = 0;
                GameLogger.fine("Direction changed: LEFT");
            }
            if (key == KeyEvent.VK_RIGHT && dx != -1 && nextDx != -1) {
                nextDx = 1;
                nextDy = 0;
                GameLogger.fine("Direction changed: RIGHT");
            }
        }
    }
}
