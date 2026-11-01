package si.um.feri.manfreda.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import si.um.feri.manfreda.worldunits.WorldUnitsGame;

/** Locen launcher za demonstracijo dimenzij sveta (world units). */
public class WorldUnitsLauncher {

    public static void main(String[] args) {
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new WorldUnitsGame(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("World Units - FitViewport");
        config.useVsync(true);
        config.setForegroundFPS(60);

        // Okno je 800x600 pikslov, svet pa 16x12 enot (1 enota = 50 pikslov)
        config.setWindowedMode(800, 600);
        config.setResizable(true);
        return config;
    }
}
