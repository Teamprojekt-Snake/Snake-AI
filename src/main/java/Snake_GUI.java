import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowListener;

/**
 * Hauptfenster der Snake AI Anwendung.
 * Bietet Auswahl zwischen Manual Play, AI Training und AI Play Modus.
 */
public class Snake_GUI extends JFrame {

    private BoardPanel board;

    /**
     * Hauptmethode - startet die Anwendung.
     *
     * @param args Kommandozeilenargumente (nicht verwendet)
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Snake_GUI frame = new Snake_GUI();
            frame.showModeSelection();
        });
    }

    /**
     * Konstruktor - initialisiert das Hauptfenster.
     */
    public Snake_GUI() {
        setTitle("Snake AI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 600);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    /**
     * Zeigt den Mode-Selection Dialog beim Start.
     */
    public void showModeSelection() {
        JDialog dialog = new JDialog(this, "Snake AI - Mode Selection", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(null);

        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(34, 139, 34));
        JLabel titleLabel = new JLabel("SNAKE AI");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 10, 15));

        JButton manualButton = createStyledButton("Manual Play", new Color(52, 152, 219));
        JButton highscoreButton = createStyledButton("Highscore", new Color(231, 76, 60));
        JButton trainingButton = createStyledButton("AI Training", new Color(231, 76, 60));
        JButton aiPlayButton = createStyledButton("AI Play", new Color(46, 204, 113));


        manualButton.addActionListener(e -> {
            dialog.dispose();
            startManualMode();
        });

        highscoreButton.addActionListener(e -> {
            dialog.dispose();
            openHighScoreText();
        });

        trainingButton.addActionListener(e -> {
            dialog.dispose();
            showTrainingOptions();
        });

        aiPlayButton.addActionListener(e -> {
            dialog.dispose();
            startAIPlayMode();
        });

        buttonPanel.add(manualButton);
        buttonPanel.add(highscoreButton);
        buttonPanel.add(trainingButton);
        buttonPanel.add(aiPlayButton);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel exitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton exitButton = createStyledButton("Exit", new Color(149, 165, 166));
        exitButton.setPreferredSize(new Dimension(200, 45));
        exitPanel.add(exitButton);
        exitPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        exitButton.addActionListener(e -> System.exit(0));

        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(exitPanel);

        JPanel infoPanel = new JPanel();
        JLabel infoLabel = new JLabel("<html><center>Select a mode to begin</center></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setForeground(Color.GRAY);
        infoPanel.add(infoLabel);

        dialog.add(titlePanel, BorderLayout.NORTH);
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(infoPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Zeigt Training-Optionen Dialog.
     */
    private void showTrainingOptions() {
        JDialog dialog = new JDialog(this, "Training Options", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 400);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);

        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Training Configuration");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JCheckBox guiCheckbox = new JCheckBox("Show GUI (slower, for watching)");
        guiCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
        guiCheckbox.setFont(new Font("Arial", Font.PLAIN, 14));

        JTextArea infoText = new JTextArea(
                "Without GUI (Headless):\n" +
                "  - Ultra fast (~350 games/sec)\n" +
                "  - ~20.000 games/min\n" +
                "  - 50-60k games in ~3 min\n" +
                "  - Empfohlen f\u00fcr Training\n\n" +
                "With GUI:\n" +
                "  - Watch the AI learn\n" +
                "  - ~60 games/min (langsam)\n" +
                "  - Zum Zuschauen/Debuggen"
        );
        infoText.setEditable(false);
        infoText.setFont(new Font("Monospaced", Font.PLAIN, 11));
        infoText.setBackground(new Color(240, 240, 240));
        infoText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton startButton = createStyledButton("Start Training", new Color(231, 76, 60));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setMaximumSize(new Dimension(250, 50));
        startButton.addActionListener(e -> {
            boolean withGui = guiCheckbox.isSelected();
            dialog.dispose();
            startTrainingMode(withGui);
        });

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(guiCheckbox);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(infoText);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(startButton);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * Erstellt einen styled Button.
     *
     * @param text Button-Text
     * @param color Button-Farbe
     * @return gestylter JButton
     */
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    /**
     * Startet Manual Mode.
     */
    private void startManualMode() {
        board = new BoardPanel();
        board.setMode(BoardPanel.GameMode.MANUAL);
        board.initGame();
        board.startGame();
        setContentPane(board);
        board.requestFocusInWindow();
        setVisible(true);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        for (WindowListener wl : getWindowListeners()) {
            removeWindowListener(wl);
        }
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });
    }

    /**
     * Startet AI Training (mit oder ohne GUI).
     *
     * @param withGui true für GUI, false für Headless
     */
    private void startTrainingMode(boolean withGui) {
        board = new BoardPanel();
        board.setMode(BoardPanel.GameMode.TRAINING);

        if (withGui) {
            setContentPane(board);
            setVisible(true);
            board.startTraining();

            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            for (WindowListener wl : getWindowListeners()) {
                removeWindowListener(wl);
            }
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    board.stopTraining();
                    System.exit(0);
                }
            });
        } else {
            GameLogger.info("Starting headless training...");
            board.startHeadlessTraining();

            JDialog trainingDialog = new JDialog(this, "Headless Training", false);
            trainingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            trainingDialog.setSize(600, 320);
            trainingDialog.setResizable(false);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            panel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel titleLabel = new JLabel("<html><center><h2>Training l\u00e4uft!</h2></center></html>");
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel statsLabel = new JLabel();
            statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            statsLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));

            // initial stats immediately
            {
                int games = board.getGamesPlayed();
                double avg = games > 0 ? (double) board.getTotalScore() / games : 0;
                int max = board.getMaxScore();
                double epsilon = board.getAgent().getEpsilon();
                int states = board.getAgent().getQTableSize();
                String stats = String.format(
                        "<html><center>" +
                                "Games: %,d<br>" +
                                "Avg Score: %.2f<br>" +
                                "Max Score: %d<br>" +
                                "Epsilon: %.3f<br>" +
                                "States: %d" +
                                "</center></html>",
                        games, avg, max, epsilon, states
                );
                statsLabel.setText(stats);
            }

            Timer statsTimer = new Timer(250, e -> {
                int games = board.getGamesPlayed();
                double avg = games > 0 ? (double) board.getTotalScore() / games : 0;
                int max = board.getMaxScore();
                double epsilon = board.getAgent().getEpsilon();
                int states = board.getAgent().getQTableSize();

                String stats = String.format(
                    "<html><center>" +
                    "Games: %,d<br>" +
                    "Avg Score: %.2f<br>" +
                    "Max Score: %d<br>" +
                    "Epsilon: %.3f<br>" +
                    "States: %d" +
                    "</center></html>",
                    games, avg, max, epsilon, states
                );
                statsLabel.setText(stats);
            });
            statsTimer.start();

            JButton stopButton = new JButton("Stop Training");
            stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            stopButton.addActionListener(e -> {
                statsTimer.stop();
                board.stopHeadlessTraining();
                trainingDialog.dispose();
                System.exit(0);
            });

            trainingDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    statsTimer.stop();
                    board.stopHeadlessTraining();
                    System.exit(0);
                }
            });

            panel.add(titleLabel);
            panel.add(Box.createVerticalStrut(10));
            panel.add(statsLabel);
            panel.add(Box.createVerticalStrut(20));
            panel.add(stopButton);

            trainingDialog.add(panel);
            trainingDialog.pack();
            trainingDialog.setLocationRelativeTo(null);
            trainingDialog.setVisible(true);
        }
    }

    /**
     * Oeffnet Highscore Textfeld
     */
    private void openHighScoreText(){
        board = new BoardPanel();
        PropConfig.loadProperties();
        String Highscore = PropConfig.properties.getProperty("Highscore");
        int choice = JOptionPane.showOptionDialog(
                this,
                "<html><center>" +
                        "<b>Highscore vom Manual Play: </b>" +
                        Highscore +
                        "</center></html>",
                "Highscore",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Zurück zum Menü"},
                "Zurück zum Menü"
        );

        if (choice != 1) {
            showModeSelection();
            return;
        }

    }

    /**
     * Startet AI Play Mode.
     */
    private void startAIPlayMode() {
        board = new BoardPanel();
        
        // Prüfe ob Q-Table existiert und trainiert ist
        int qTableSize = board.getAgent().getQTableSize();
        if (qTableSize < 50) {
            int choice = JOptionPane.showOptionDialog(
                this,
                "<html><center>" +
                "<b>WARNUNG: Keine oder wenig trainierte AI gefunden!</b><br><br>" +
                "Die AI muss erst trainiert werden, bevor sie spielen kann.<br><br>" +
                "<u>Empfohlenes Training:</u><br>" +
                "1. Wähle 'AI Training' im Hauptmenü<br>" +
                "2. Wähle 'Headless Training' (ohne GUI)<br>" +
                "3. Trainiere für 3 Minuten (~50-60k games)<br>" +
                "4. Drücke 'Stop Training'<br>" +
                "5. Dann kann die AI spielen!<br><br>" +
                "Möchtest du trotzdem fortfahren?<br>" +
                "(Die AI wird random spielen ohne Training)" +
                "</center></html>",
                "Training Required",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new String[]{"Zurück zum Menü", "Trotzdem fortfahren"},
                "Zurück zum Menü"
            );
            
            if (choice != 1) {
                showModeSelection();
                return;
            }
        }
        
        board.setMode(BoardPanel.GameMode.AI_PLAY);
        board.initGame();
        board.startGame();
        setContentPane(board);
        board.requestFocusInWindow();
        setVisible(true);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        for (WindowListener wl : getWindowListeners()) {
            removeWindowListener(wl);
        }
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });
    }
}
