import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;

/**
 * Haupt-Spielfeld Panel für Snake.
 * Verwaltet Spiellogik, Rendering und Q-Learning Integration.
 * Unterstützt drei Modi: MANUAL, TRAINING, AI_PLAY.
 */
public class BoardPanel extends JPanel {

    private final int TILE = 20;
    private final int FIELD_WIDTH = 40;
    private final int FIELD_HEIGHT = 30;
    private boolean paused = false;
    private int score = 0;

    private final List<Point> snake = new LinkedList<>();

    private int dx = 0;
    private int dy = 0;
    private int lastDx = 0;
    private int lastDy = 0;
    private boolean directionChanged = false;

    private Point apple = new Point(10, 10);

    private Timer gameTimer;
    private boolean running = false;

    private QLearningAgent agent;
    private GameMode mode = GameMode.MANUAL;
    private int gamesPlayed = 0;
    private int totalScore = 0;
    private int maxScore = 0;
    private double lastDistanceToApple = 0;
    private TrainingStats trainingStats;
    private Thread headlessTrainingThread;
    private volatile boolean headlessRunning = false;

    /**
     * Spielmodi.
     */
    enum GameMode {
        /** Manuelles Spielen mit Tastatur */
        MANUAL,
        /** AI Training Modus */
        TRAINING,
        /** AI spielt mit gelernter Policy */
        AI_PLAY
    }

    /**
     * Konstruktor - initialisiert das Spielfeld und lädt gespeicherte Q-Table.
     */
    public BoardPanel() {
        GameLogger.info("Game started!");
        setBackground(Color.GRAY);
        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyHandler());

        agent = new QLearningAgent();
        agent.getQTable().putAll(QTableStorage.loadQTable());
    }

    /**
     * Spawnt einen neuen Apfel an zufälliger Position.
     */
    private void spawnApple() {
        int maxX = (mode == GameMode.TRAINING && headlessRunning) ? FIELD_WIDTH : getWidth() / TILE;
        int maxY = (mode == GameMode.TRAINING && headlessRunning) ? FIELD_HEIGHT : getHeight() / TILE;

        if (maxX <= 0) maxX = FIELD_WIDTH;
        if (maxY <= 0) maxY = FIELD_HEIGHT;

        Point newApple;

        do {
            int x = (int) (Math.random() * (maxX * 0.8)) + (int)(maxX * 0.1);
            int y = (int) (Math.random() * (maxY * 0.8)) + (int)(maxY * 0.1);
            newApple = new Point(x, y);
        } while (snake.contains(newApple));

        apple = newApple;
        GameLogger.fine("New apple spawned at: " + apple);
    }

    /**
     * Initialisiert das Spiel mit Startwerten.
     */
    public synchronized void initGame() {
        snake.clear();
        score = 0;
        snake.add(new Point(5, 5));

        if (mode == GameMode.TRAINING || mode == GameMode.AI_PLAY) {
            int[] directions = {0, 1, 2, 3};
            int randomDir = directions[(int)(Math.random() * 4)];

            switch (randomDir) {
                case 0: dx = 0; dy = -1; break;
                case 1: dx = 0; dy = 1; break;
                case 2: dx = -1; dy = 0; break;
                case 3: dx = 1; dy = 0; break;
            }
        } else {
            dx = 0;
            dy = 0;
        }

        spawnApple();
        lastDistanceToApple = getDistanceToApple(snake.getFirst());

        GameLogger.info("Game initialized");
    }

    /**
     * Startet den Game-Timer mit angepasster Geschwindigkeit.
     */
    public void startGame() {
        if (running) {
            GameLogger.warning("Game already running");
            return;
        }

        running = true;

        int timerDelay = (mode == GameMode.TRAINING) ? 10 : 70;

        gameTimer = new Timer(timerDelay, e -> {
            update();
            repaint();
        });

        gameTimer.start();
        GameLogger.info("Game timer started with " + timerDelay + "ms delay");
    }

    private void togglePause() {
        paused = !paused;

        if (paused) {
            GameLogger.info("Game paused");
            showPauseMenu();
        } else {
            GameLogger.info("Game resumed");
        }
    }

    private void showPauseMenu() {
        String[] options;
        String message;

        if (mode == GameMode.TRAINING) {
            options = new String[]{"Resume", "Save & Main Menu", "Save & Exit"};
            message = String.format(
                    """
                            Training Paused
                            
                            Games: %d
                            Max Score: %d
                            States Learned: %d""",
                    gamesPlayed, maxScore, agent.getQTableSize()
            );
        } else {
            options = new String[]{"Resume", "Main Menu", "Exit"};
            message = "Game Paused\n\nScore: " + score;
        }

        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                "Paused",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            paused = false;
        } else if (choice == 1) {
            if (mode == GameMode.TRAINING) {
                stopTraining();
            }
            goToMainMenu();
        } else if (choice == 2) {
            if (mode == GameMode.TRAINING) {
                stopTraining();
            }
            System.exit(0);
        } else {
            paused = false;
        }
    }

    private void goToMainMenu() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        running = false;

        Window window = SwingUtilities.getWindowAncestor(this);

        if (window != null) {
            window.dispose();
        }

        EventQueue.invokeLater(() -> {
            Snake_GUI frame = new Snake_GUI();
            frame.showModeSelection();
        });
    }

    public GameMode getMode() {
        return mode;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public QLearningAgent getAgent() {
        return agent;
    }

    /**
     * Hauptupdate-Methode.
     */
    private synchronized void update() {
        directionChanged = false;
        if (paused) return;
        if (dx == 0 && dy == 0) return;

        if (snake.isEmpty()) return;
        Point head = snake.getFirst();

        if (mode != GameMode.MANUAL) {
            GameState currentState = calculateGameState();

            int action;
            if (mode == GameMode.TRAINING) {
                action = agent.chooseAction(currentState);
            } else {
                action = agent.getBestActionForPlay(currentState);
            }

            applyAction(action);
            agent.updateLastExperience(currentState, action);
        }

        lastDx = dx;
        lastDy = dy;

        Point newHead = new Point(head.x + dx, head.y + dy);

        LinkedList<Point> snakeCopy = new LinkedList<>(snake);
        snakeCopy.removeFirst();
        if (snakeCopy.contains(newHead)) {
            gameOver();
            return;
        }

        int fieldWidth = (mode == GameMode.TRAINING && headlessRunning) ? FIELD_WIDTH : getWidth() / TILE;
        int fieldHeight = (mode == GameMode.TRAINING && headlessRunning) ? FIELD_HEIGHT : getHeight() / TILE;
        
        if (newHead.x < 0 || newHead.y < 0 ||
                newHead.x >= fieldWidth ||
                newHead.y >= fieldHeight) {
            GameLogger.warning("Game Over - Wall collision at: " + newHead);
            gameOver();
            return;
        }

        boolean ateApple = newHead.equals(apple);

        snake.addFirst(newHead);

        if (!ateApple) {
            snake.removeLast();

            if (mode == GameMode.TRAINING) {
                double currentDistance = getDistanceToApple(newHead);
                double reward = (lastDistanceToApple - currentDistance) * 0.1;

                GameState newState = calculateGameState();
                agent.processReward(reward, newState);

                lastDistanceToApple = currentDistance;
            }
        } else {
            score++;
            GameLogger.info("Apple eaten! Snake length: " + snake.size());

            if (mode == GameMode.TRAINING) {
                GameState newState = calculateGameState();
                agent.processReward(10.0, newState);
            }

            spawnApple();
            lastDistanceToApple = getDistanceToApple(head);
        }
    }

    /**
     * Wird aufgerufen wenn das Spiel vorbei ist.
     */
    private void gameOver() {
        running = false;

        if (gameTimer != null) {
            gameTimer.stop();
            GameLogger.info("Game timer stopped");
        }

        if (mode == GameMode.TRAINING) {
            GameState finalState = calculateGameState();
            agent.processReward(-10.0, finalState);

            agent.decayEpsilon();

            gamesPlayed++;
            totalScore += score;
            if (score > maxScore) maxScore = score;

            if (gamesPlayed % 100 == 0) {
                double avgScore = totalScore / (double) gamesPlayed;
                GameLogger.info(String.format(
                        "Training: Games=%d, Avg=%.2f, Max=%d, Epsilon=%.3f, States=%d",
                        gamesPlayed, avgScore, maxScore, agent.getEpsilon(), agent.getQTableSize()
                ));

                QTableStorage.saveQTable(agent.getQTable());

                trainingStats.update(gamesPlayed, totalScore, maxScore,
                        agent.getQTableSize(), agent.getEpsilon());
                trainingStats.saveSessionStats();
            }

            initGame();
            startGame();
            return;
        }

        String[] options = {"Neues Spiel", "Main Menu", "Spiel Beenden"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Game Over! Score: " + score,
                "Snake",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            initGame();
            startGame();
        } else if (choice == 1) {
            goToMainMenu();
        } else {
            System.exit(0);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.RED);
        g.fillRect(apple.x * TILE, apple.y * TILE, TILE, TILE);

        g.setColor(Color.GREEN);
        for (Point p : snake) {
            g.fillRect(p.x * TILE, p.y * TILE, TILE, TILE);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 30);

        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Berechnet den aktuellen Spielzustand für die AI.
     *
     * @return GameState mit Gefahr-, Apfel- und Tail-Informationen
     */
    private GameState calculateGameState() {
        if (snake.isEmpty()) return new GameState(true, true, true, false, false, false, false, false);
        Point head = snake.getFirst();

        boolean dangerAhead = isDanger(head.x + dx, head.y + dy);
        boolean dangerLeft = isDanger(head.x + getLeftDx(), head.y + getLeftDy());
        boolean dangerRight = isDanger(head.x + getRightDx(), head.y + getRightDy());

        int appleRelX = apple.x - head.x;
        int appleRelY = apple.y - head.y;

        boolean appleAhead = isInDirection(appleRelX, appleRelY, dx, dy);
        boolean appleLeft = isInDirection(appleRelX, appleRelY, getLeftDx(), getLeftDy());
        boolean appleRight = isInDirection(appleRelX, appleRelY, getRightDx(), getRightDy());

        boolean tailDangerLeft = isTailDanger(getLeftDx(), getLeftDy());
        boolean tailDangerRight = isTailDanger(getRightDx(), getRightDy());

        return new GameState(dangerAhead, dangerLeft, dangerRight,
                appleAhead, appleLeft, appleRight,
                tailDangerLeft, tailDangerRight);
    }

    /**
     * Prüft ob an der Position Gefahr besteht.
     *
     * @param x X-Koordinate
     * @param y Y-Koordinate
     * @return true wenn Wand oder Schlangenkörper
     */
    private boolean isDanger(int x, int y) {
        int fieldWidth = (mode == GameMode.TRAINING && headlessRunning) ? FIELD_WIDTH : getWidth() / TILE;
        int fieldHeight = (mode == GameMode.TRAINING && headlessRunning) ? FIELD_HEIGHT : getHeight() / TILE;
        
        if (x < 0 || y < 0 || x >= fieldWidth || y >= fieldHeight) {
            return true;
        }

        Point checkPos = new Point(x, y);
        for (int i = 1; i < snake.size(); i++) {
            Point segment = snake.get(i);
            if (segment != null && segment.equals(checkPos)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Prüft ob eigener Körper in 2-3 Tiles Entfernung in einer Richtung liegt.
     * Hilft der AI längere Schlangenkörper zu vermeiden.
     *
     * @param dirX Richtungs-X (-1, 0, oder 1)
     * @param dirY Richtungs-Y (-1, 0, oder 1)
     * @return true wenn Körper in 2-3 Tiles Entfernung gefunden
     */
    private boolean isTailDanger(int dirX, int dirY) {
        if (snake.isEmpty()) return false;
        Point head = snake.getFirst();
        
        // Prüfe 2-3 Tiles in die angegebene Richtung
        for (int distance = 2; distance <= 3; distance++) {
            int checkX = head.x + (dirX * distance);
            int checkY = head.y + (dirY * distance);
            
            Point checkPos = new Point(checkX, checkY);
            for (int i = 1; i < snake.size(); i++) {
                Point segment = snake.get(i);
                if (segment != null && segment.equals(checkPos)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private int getLeftDx() {
        if (dx == 0 && dy == -1) return -1;
        if (dx == 0 && dy == 1) return 1;
        if (dx == -1 && dy == 0) return 0;
        if (dx == 1 && dy == 0) return 0;
        return 0;
    }

    private int getLeftDy() {
        if (dx == 0 && dy == -1) return 0;
        if (dx == 0 && dy == 1) return 0;
        if (dx == -1 && dy == 0) return 1;
        if (dx == 1 && dy == 0) return -1;
        return 0;
    }

    private int getRightDx() {
        if (dx == 0 && dy == -1) return 1;
        if (dx == 0 && dy == 1) return -1;
        if (dx == -1 && dy == 0) return 0;
        if (dx == 1 && dy == 0) return 0;
        return 0;
    }

    private int getRightDy() {
        if (dx == 0 && dy == -1) return 0;
        if (dx == 0 && dy == 1) return 0;
        if (dx == -1 && dy == 0) return -1;
        if (dx == 1 && dy == 0) return 1;
        return 0;
    }

    /**
     * Prüft ob der Apfel in einer bestimmten Richtung liegt.
     *
     * @param appleRelX relative X-Position des Apfels
     * @param appleRelY relative Y-Position des Apfels
     * @param dirX Richtungs-X
     * @param dirY Richtungs-Y
     * @return true wenn Apfel in dieser Richtung
     */
    private boolean isInDirection(int appleRelX, int appleRelY, int dirX, int dirY) {
        if (dirX != 0) {
            return Integer.signum(appleRelX) == dirX;
        } else if (dirY != 0) {
            return Integer.signum(appleRelY) == dirY;
        }
        return false;
    }

    /**
     * Wendet eine AI-Action an.
     *
     * @param action 0=geradeaus, 1=links, 2=rechts
     */
    private void applyAction(int action) {
        if (action == 0) {
            return;
        } else if (action == 1) {
            int newDx = getLeftDx();
            int newDy = getLeftDy();
            dx = newDx;
            dy = newDy;
        } else if (action == 2) {
            int newDx = getRightDx();
            int newDy = getRightDy();
            dx = newDx;
            dy = newDy;
        }
    }

    /**
     * Berechnet Manhattan-Distanz zum Apfel.
     *
     * @param pos Position
     * @return Distanz
     */
    private double getDistanceToApple(Point pos) {
        return Math.abs(pos.x - apple.x) + Math.abs(pos.y - apple.y);
    }

    /**
     * Setzt den Spielmodus.
     *
     * @param newMode MANUAL, TRAINING oder AI_PLAY
     */
    public void setMode(GameMode newMode) {
        this.mode = newMode;
        GameLogger.info("Mode changed to: " + newMode);

        if (newMode == GameMode.AI_PLAY) {
            agent.getQTable().putAll(QTableStorage.loadQTable());
        }
    }

    /**
     * Startet das Training mit GUI.
     */
    public void startTraining() {
        setMode(GameMode.TRAINING);
        gamesPlayed = 0;
        totalScore = 0;
        maxScore = 0;
        trainingStats = new TrainingStats();
        initGame();
        startGame();
    }

    /**
     * Startet Headless Training ohne GUI.
     */
    public void startHeadlessTraining() {
        setMode(GameMode.TRAINING);
        gamesPlayed = 0;
        totalScore = 0;
        maxScore = 0;
        trainingStats = new TrainingStats();
        headlessRunning = true;

        headlessTrainingThread = new Thread(() -> {
            GameLogger.info("Headless training started");

            while (headlessRunning) {
                initGame();
                running = true;

                while (running && headlessRunning) {
                    update();
                }
            }

            GameLogger.info("Headless training stopped");
        });

        headlessTrainingThread.start();
    }

    /**
     * Stoppt Headless Training.
     */
    public void stopHeadlessTraining() {
        headlessRunning = false;
        running = false;

        if (headlessTrainingThread != null) {
            try {
                headlessTrainingThread.join(1000);
            } catch (InterruptedException e) {
                GameLogger.warning("Interrupted while stopping headless training");
            }
        }

        stopTraining();
    }

    /**
     * Stoppt das Training und speichert die Q-Table.
     */
    public void stopTraining() {
        QTableStorage.saveQTable(agent.getQTable());

        if (trainingStats != null) {
            trainingStats.update(gamesPlayed, totalScore, maxScore,
                    agent.getQTableSize(), agent.getEpsilon());
            trainingStats.saveAll();
        }

        GameLogger.info("Training stopped. Final stats: Games=" + gamesPlayed +
                ", Max=" + maxScore + ", States=" + agent.getQTableSize());
    }

    private class KeyHandler extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_ESCAPE) {
                togglePause();
                return;
            }

            if (mode != GameMode.MANUAL || directionChanged) {
                return;
            }

            if (key == KeyEvent.VK_UP && dy != 1) {
                dx = 0; dy = -1;
                directionChanged = true;
                GameLogger.fine("Direction changed: UP");
            }
            if (key == KeyEvent.VK_DOWN && dy != -1) {
                dx = 0; dy = 1;
                directionChanged = true;
                GameLogger.fine("Direction changed: DOWN");
            }
            if (key == KeyEvent.VK_LEFT && dx != 1) {
                dx = -1; dy = 0;
                directionChanged = true;
                GameLogger.fine("Direction changed: LEFT");
            }
            if (key == KeyEvent.VK_RIGHT && dx != -1) {
                dx = 1; dy = 0;
                directionChanged = true;
                GameLogger.fine("Direction changed: RIGHT");
            }
        }
    }
}
