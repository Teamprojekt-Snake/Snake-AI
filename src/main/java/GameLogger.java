import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.*;

public class GameLogger {

    private static final Logger logger = Logger.getLogger("SnakeGame");

    static {
        try {
            String projectRoot = System.getProperty("user.dir");
            System.out.println("Working Directory: " + projectRoot);

            Path logsDir = Paths.get(projectRoot, "logs");

            // Ordner erstellen
            Files.createDirectories(logsDir);
            System.out.println("Logs directory created at: " + logsDir.toAbsolutePath());

            Path logFile = logsDir.resolve("snake_game.log");
            System.out.println("Log file path: " + logFile.toAbsolutePath());

            FileHandler fileHandler = new FileHandler(logFile.toString(), 10485760, 3, false);
            fileHandler.setLevel(Level.INFO);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            logger.addHandler(consoleHandler);

            logger.setLevel(Level.INFO);
            logger.setUseParentHandlers(false);

            logger.info("Logging initialized successfully!");

        } catch (IOException e) {
            System.err.println("FEHLER beim Logger erstellen:");
            e.printStackTrace();
        }
    }

    // Methoden für Logging
    public static void info(String msg) {
        logger.info(msg);
    }

    public static void fine(String msg) {
        logger.fine(msg);
    }

    public static void warning(String msg) {
        logger.warning(msg);
    }

    public static void severe(String msg) {
        logger.severe(msg);
    }

    public static void threadStatus(Thread t) {
        if (t != null) {
            info("Thread " + t.getName() + " alive: " + t.isAlive());
        } else {
            info("Thread is null");
        }
    }
}