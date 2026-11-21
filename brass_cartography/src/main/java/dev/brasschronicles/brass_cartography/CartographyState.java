package dev.brasschronicles.brass_cartography;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

/** Client-only settings and the most recent death waypoint. */
public final class CartographyState {
    public static final MapSampler MAP = new MapSampler();
    private static final Path CONFIG = FMLPaths.CONFIGDIR.get().resolve("brass_cartography.properties");
    private static boolean deathMarkerVisible = true;
    private static boolean minimapVisible = true;
    private static boolean previousAlive = true;
    private static int deathX;
    private static int deathY;
    private static int deathZ;
    private static String deathDimension = "";
    private static boolean hasDeath;
    private static List<WaypointMarker> waystones = List.of();

    private CartographyState() {}

    public static void load() {
        Properties p = new Properties();
        if (Files.isRegularFile(CONFIG)) {
            try (var in = Files.newInputStream(CONFIG)) { p.load(in); }
            catch (IOException e) { BrassCartography.LOGGER.warn("Could not read cartography settings", e); }
        }
        deathMarkerVisible = Boolean.parseBoolean(p.getProperty("deathMarkerVisible", "true"));
        minimapVisible = Boolean.parseBoolean(p.getProperty("minimapVisible", "true"));
        hasDeath = Boolean.parseBoolean(p.getProperty("hasDeath", "false"));
        deathX = parseInt(p.getProperty("deathX"));
        deathY = parseInt(p.getProperty("deathY"));
        deathZ = parseInt(p.getProperty("deathZ"));
        deathDimension = p.getProperty("deathDimension", "");
    }

    private static int parseInt(String value) {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException ignored) { return 0; }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("deathMarkerVisible", Boolean.toString(deathMarkerVisible));
        p.setProperty("minimapVisible", Boolean.toString(minimapVisible));
        p.setProperty("hasDeath", Boolean.toString(hasDeath));
        p.setProperty("deathX", Integer.toString(deathX));
        p.setProperty("deathY", Integer.toString(deathY));
        p.setProperty("deathZ", Integer.toString(deathZ));
        p.setProperty("deathDimension", deathDimension);
        try {
            Files.createDirectories(CONFIG.getParent());
            try (var out = Files.newOutputStream(CONFIG)) { p.store(out, "Brass Cartography client settings"); }
        } catch (IOException e) {
            BrassCartography.LOGGER.warn("Could not save cartography settings", e);
        }
    }

    public static void toggleDeathMarker() { deathMarkerVisible = !deathMarkerVisible; save(); }
    public static void clearDeathMarker() { hasDeath = false; deathDimension = ""; save(); }
    public static void toggleMinimap() { minimapVisible = !minimapVisible; save(); }
    public static boolean deathMarkerVisible() { return deathMarkerVisible; }
    public static boolean minimapVisible() { return minimapVisible; }
    public static boolean hasDeath() { return hasDeath; }
    public static int deathX() { return deathX; }
    public static int deathY() { return deathY; }
    public static int deathZ() { return deathZ; }
    public static List<WaypointMarker> waystones() { return waystones; }
    public static void setWaystones(List<WaypointMarker> value) { waystones = List.copyOf(value); }
    public static boolean deathIsIn(ResourceLocation dimension) {
        return hasDeath && dimension != null && dimension.toString().equals(deathDimension);
    }

    public static void observeLife(boolean alive, int x, int y, int z, ResourceLocation dimension) {
        if (!alive && previousAlive && dimension != null) {
            deathX = x;
            deathY = y;
            deathZ = z;
            deathDimension = dimension.toString();
            hasDeath = true;
            save();
        }
        previousAlive = alive;
    }
}
