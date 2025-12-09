import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;

public class TrainingStats {
    
    private static final String DATA_DIR = "data";
    private static final String SESSION_FILE = DATA_DIR + "/session_stats.txt";
    private static final String HISTORY_FILE = DATA_DIR + "/training_history.txt";
    
    private long startTime;
    private int totalGames;
    private int totalScore;
    private int maxScore;
    private int statesLearned;
    private double finalEpsilon;
    
    public TrainingStats() {
        this.startTime = System.currentTimeMillis();
        this.totalGames = 0;
        this.totalScore = 0;
        this.maxScore = 0;
    }
    
    /**
     * Aktualisiert die Stats während des Trainings.
     *
     * @param games gespielte Spiele
     * @param score Gesamt-Score
     * @param max maximaler Score
     * @param states Anzahl gelernter States
     * @param epsilon aktueller Epsilon-Wert
     */
    public void update(int games, int score, int max, int states, double epsilon) {
        this.totalGames = games;
        this.totalScore = score;
        this.maxScore = max;
        this.statesLearned = states;
        this.finalEpsilon = epsilon;
    }
    
    /**
     * Speichert Session-Statistiken.
     */
    public void saveSessionStats() {
        try {
            Path dataDir = Paths.get(DATA_DIR);
            Files.createDirectories(dataDir);
            
            long duration = System.currentTimeMillis() - startTime;
            String durationStr = formatDuration(duration);
            double avgScore = totalGames > 0 ? (double) totalScore / totalGames : 0;
            
            String content = String.format(
                "=== TRAINING SESSION ===\n" +
                "Date: %s\n" +
                "Duration: %s\n" +
                "Games Played: %,d\n" +
                "Average Score: %.2f\n" +
                "Max Score: %d\n" +
                "States Learned: %d\n" +
                "Final Epsilon: %.4f\n",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                durationStr,
                totalGames,
                avgScore,
                maxScore,
                statesLearned,
                finalEpsilon
            );
            
            Files.writeString(Paths.get(SESSION_FILE), content);
            GameLogger.info("Session stats saved to " + SESSION_FILE);
            
        } catch (IOException e) {
            GameLogger.severe("Failed to save session stats: " + e.getMessage());
        }
    }
    
    /**
     * Fügt Session zur History hinzu.
     */
    public void appendToHistory() {
        try {
            Path dataDir = Paths.get(DATA_DIR);
            Files.createDirectories(dataDir);
            
            double avgScore = totalGames > 0 ? (double) totalScore / totalGames : 0;
            
            String line = String.format(
                "%s | Games: %,d | Avg: %.2f | Max: %d | States: %d | Epsilon: %.4f\n",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                totalGames,
                avgScore,
                maxScore,
                statesLearned,
                finalEpsilon
            );
            
            Files.writeString(
                Paths.get(HISTORY_FILE), 
                line, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.APPEND
            );
            
            GameLogger.info("Session added to training history");
            
        } catch (IOException e) {
            GameLogger.severe("Failed to append to history: " + e.getMessage());
        }
    }
    
    /**
     * Speichert beide Statistik-Dateien.
     */
    public void saveAll() {
        saveSessionStats();
        appendToHistory();
    }
    
    /**
     * Formatiert Millisekunden zu lesbarem String.
     *
     * @param millis Millisekunden
     * @return formatierter String
     */
    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
