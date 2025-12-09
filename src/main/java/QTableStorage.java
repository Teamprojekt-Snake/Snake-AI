import java.io.*;
import java.nio.file.*;
import java.util.*;

public class QTableStorage {
    
    private static final String DATA_DIR = "data";
    private static final String QTABLE_FILE = DATA_DIR + "/qtable.txt";
    
    /**
     * Speichert die Q-Table in eine Textdatei.
     * Format pro Zeile: "101010:1.5,2.3,-0.8"
     *
     * @param qTable zu speichernde Q-Table
     */
    public static void saveQTable(Map<GameState, double[]> qTable) {
        try {
            Path dataDir = Paths.get(DATA_DIR);
            Files.createDirectories(dataDir);

            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(QTABLE_FILE))) {

                for (Map.Entry<GameState, double[]> entry : qTable.entrySet()) {
                    GameState state = entry.getKey();
                    double[] qValues = entry.getValue();

                    String line = stateToString(state) + ":" +
                            qValues[0] + "," + qValues[1] + "," + qValues[2];

                    writer.write(line);
                    writer.newLine();
                }

                GameLogger.info("Q-Table saved: " + qTable.size() + " states to " + QTABLE_FILE);
            }

        } catch (IOException e) {
            GameLogger.severe("Failed to save Q-Table: " + e.getMessage());
            for (StackTraceElement elem : e.getStackTrace()) {
                GameLogger.severe("  at " + elem);
            }
        }
    }
    
    /**
     * Lädt die Q-Table aus einer Textdatei.
     *
     * @return geladene Q-Table
     */
    public static Map<GameState, double[]> loadQTable() {
        Map<GameState, double[]> qTable = new HashMap<>();

        try {
            if (!Files.exists(Paths.get(QTABLE_FILE))) {
                GameLogger.info("No saved Q-Table found, starting fresh");
                return qTable;
            }

            List<String> lines = Files.readAllLines(Paths.get(QTABLE_FILE));

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(":");
                if (parts.length != 2) continue;

                GameState state = stringToState(parts[0]);

                String[] valueStrings = parts[1].split(",");
                double[] qValues = new double[3];
                for (int i = 0; i < 3; i++) {
                    qValues[i] = Double.parseDouble(valueStrings[i]);
                }

                qTable.put(state, qValues);
            }

            GameLogger.info("Q-Table loaded: " + qTable.size() + " states from " + QTABLE_FILE);

        } catch (IOException | NumberFormatException e) {
            GameLogger.severe("Failed to load Q-Table: " + e.getMessage());
        }

        return qTable;
    }
    
    /**
     * Konvertiert GameState zu String.
     *
     * @param state GameState
     * @return String-Repräsentation (8 Booleans)
     */
    private static String stateToString(GameState state) {
        return String.format("%d%d%d%d%d%d%d%d",
                state.isDangerAhead() ? 1 : 0,
                state.isDangerLeft() ? 1 : 0,
                state.isDangerRight() ? 1 : 0,
                state.isAppleAhead() ? 1 : 0,
                state.isAppleLeft() ? 1 : 0,
                state.isAppleRight() ? 1 : 0,
                state.isTailDangerLeft() ? 1 : 0,
                state.isTailDangerRight() ? 1 : 0
        );
    }
    
    /**
     * Konvertiert String zu GameState.
     *
     * @param str String-Repräsentation (8 Booleans)
     * @return GameState
     */
    private static GameState stringToState(String str) {
        return new GameState(
                str.charAt(0) == '1',
                str.charAt(1) == '1',
                str.charAt(2) == '1',
                str.charAt(3) == '1',
                str.charAt(4) == '1',
                str.charAt(5) == '1',
                str.charAt(6) == '1',
                str.charAt(7) == '1'
        );
    }
}
