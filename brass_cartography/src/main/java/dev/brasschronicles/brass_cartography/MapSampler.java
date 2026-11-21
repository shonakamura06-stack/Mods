package dev.brasschronicles.brass_cartography;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.fml.loading.FMLPaths;

/**
 * Samples a small terrain window and keeps a bounded, client-local record of explored map cells.
 * It reads only already-loaded chunks and never requests a chunk load or generation.
 */
public final class MapSampler {
    public static final int SIDE = 17;
    public static final int CENTER = SIDE / 2;
    public static final int BLOCKS_PER_CELL = 8;
    private static final int UNKNOWN = 0x6C5944;
    private static final int FILE_VERSION = 1;
    private static final int MAX_CELLS_PER_DIMENSION = 50_000;

    private final int[][] colors = new int[SIDE][SIDE];
    private final boolean[][] known = new boolean[SIDE][SIDE];
    private final LinkedHashMap<Long, Integer> explored = new LinkedHashMap<>();
    private String dimensionId = "";
    private int originX;
    private int originY;
    private int originZ;
    private long lastSampleTick = Long.MIN_VALUE;
    private long lastSaveTick = Long.MIN_VALUE;
    private boolean dirty;

    public void tick(Level level, int playerX, int playerY, int playerZ, long gameTime) {
        String nextDimension = level.dimension().location().toString();
        boolean changedDimension = !dimensionId.equals(nextDimension);
        boolean moved = Math.abs(playerX - originX) >= 4 || Math.abs(playerZ - originZ) >= 4;
        if (!changedDimension && !moved && gameTime - lastSampleTick < 10) {
            saveIfDue(gameTime, false);
            return;
        }

        if (changedDimension) {
            saveIfDue(gameTime, true);
            dimensionId = nextDimension;
            explored.clear();
            loadHistory();
        }
        originX = playerX;
        originY = playerY;
        originZ = playerZ;
        lastSampleTick = gameTime;
        for (int gy = 0; gy < SIDE; gy++) {
            for (int gx = 0; gx < SIDE; gx++) {
                int x = originX + (gx - CENTER) * BLOCKS_PER_CELL;
                int z = originZ + (gy - CENTER) * BLOCKS_PER_CELL;
                int cellX = Math.floorDiv(x, BLOCKS_PER_CELL);
                int cellZ = Math.floorDiv(z, BLOCKS_PER_CELL);
                long key = cellKey(cellX, cellZ);
                BlockPos probe = new BlockPos(x, level.getMinBuildHeight(), z);
                if (!level.hasChunkAt(probe)) {
                    Integer previous = explored.get(key);
                    known[gy][gx] = previous != null;
                    colors[gy][gx] = previous == null ? UNKNOWN : previous;
                    continue;
                }

                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                int y = Math.max(level.getMinBuildHeight(), surfaceY - 1);
                BlockPos surface = new BlockPos(x, y, z);
                BlockState state = level.getBlockState(surface);
                MapColor mapColor = state.getMapColor(level, surface);
                int rgb = mapColor.col & 0x00FFFFFF;
                if (rgb == 0) rgb = UNKNOWN;
                int shade = Math.max(168, Math.min(255, 220 + (y - originY) / 5));
                int color = tint(rgb, shade);
                colors[gy][gx] = color;
                known[gy][gx] = true;
                if (!Integer.valueOf(color).equals(explored.put(key, color))) dirty = true;
            }
        }
        trimHistory();
        saveIfDue(gameTime, false);
    }

    private static long cellKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    private static int tint(int rgb, int shade) {
        int r = ((rgb >> 16) & 255) * shade / 255;
        int g = ((rgb >> 8) & 255) * shade / 255;
        int b = (rgb & 255) * shade / 255;
        return (r << 16) | (g << 8) | b;
    }

    private void trimHistory() {
        while (explored.size() > MAX_CELLS_PER_DIMENSION) {
            Iterator<Long> oldest = explored.keySet().iterator();
            oldest.next();
            oldest.remove();
            dirty = true;
        }
    }

    private Path historyPath() {
        String safeName = dimensionId.replace(':', '_').replace('/', '_').replace('\\', '_');
        return FMLPaths.CONFIGDIR.get().resolve("brass_cartography/maps").resolve(safeName + ".dat");
    }

    private void loadHistory() {
        Path path = historyPath();
        if (!Files.isRegularFile(path)) return;
        try (DataInputStream in = new DataInputStream(Files.newInputStream(path))) {
            if (in.readInt() != FILE_VERSION) return;
            int count = Math.min(in.readInt(), MAX_CELLS_PER_DIMENSION);
            for (int i = 0; i < count; i++) {
                int x = in.readInt();
                int z = in.readInt();
                int rgb = in.readInt() & 0x00FFFFFF;
                explored.put(cellKey(x, z), rgb);
            }
        } catch (IOException | RuntimeException e) {
            BrassCartography.LOGGER.warn("Could not load local map history for {}", dimensionId, e);
            explored.clear();
        }
    }

    public void flush() { saveIfDue(lastSampleTick, true); }

    private void saveIfDue(long gameTime, boolean force) {
        if (!dirty || dimensionId.isEmpty()) return;
        if (!force && gameTime - lastSaveTick < 100) return;
        Path path = historyPath();
        try {
            Files.createDirectories(path.getParent());
            try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(path))) {
                out.writeInt(FILE_VERSION);
                out.writeInt(explored.size());
                for (Map.Entry<Long, Integer> entry : explored.entrySet()) {
                    long key = entry.getKey();
                    out.writeInt((int) (key >> 32));
                    out.writeInt((int) key);
                    out.writeInt(entry.getValue());
                }
            }
            dirty = false;
            lastSaveTick = gameTime;
        } catch (IOException e) {
            BrassCartography.LOGGER.warn("Could not save local map history for {}", dimensionId, e);
        }
    }

    public int colorAt(int x, int y) { return colors[y][x]; }
    public boolean isKnown(int x, int y) { return known[y][x]; }

    /** Returns a previously surveyed cell's RGB value, or -1 if it has never been observed. */
    public int exploredColor(int cellX, int cellZ) {
        Integer color = explored.get(cellKey(cellX, cellZ));
        return color == null ? -1 : color;
    }

    public int originX() { return originX; }
    public int originZ() { return originZ; }

    public int deathCellX(int worldX) {
        return CENTER + Math.floorDiv(worldX - originX, BLOCKS_PER_CELL);
    }

    public int deathCellY(int worldZ) {
        return CENTER + Math.floorDiv(worldZ - originZ, BLOCKS_PER_CELL);
    }
}
