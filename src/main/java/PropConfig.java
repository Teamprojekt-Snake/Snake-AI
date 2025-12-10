import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropConfig {
   static Properties properties = new Properties();

    public static void loadProperties() {
        try {
            FileInputStream fin = new FileInputStream("config.properties");
            properties.load(fin);

        } catch (IOException e) {
            System.out.println("config.properties nicht gefunden. Erstelle neue Datei mit Defaults.");

            //Defualt Wert
            properties.setProperty("Highscore", "0");
            //Datei erstellen
            saveProperties("Highscore", "0");
        }
    }

    public static void saveProperties(String key, String value) {
        try {
            FileOutputStream fout = new FileOutputStream("config.properties");

            properties.setProperty(key, value);
            properties.store(fout, "saved");
            fout.close();
        } catch(IOException e){
            e.printStackTrace();
        }

    }
}
