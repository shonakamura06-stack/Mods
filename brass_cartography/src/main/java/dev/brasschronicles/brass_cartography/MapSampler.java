package dev.brasschronicles.brass_cartography;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;

/** Samples only already-loaded terrain; it never asks the client to load or generate a chunk. */
public final class MapSampler {
    public static final int SIDE = 17;
    public static final int CENTER = SIDE / 2;
    public static final int BLOCKS_PER_CELL = 8;
    private static final int UNKNOWN = 0x6C5944;

    private final int[][] colors = new int[SIDE][SIDE];
    private final boolean[][] known = new boolean[SIDE][SIDE];
    private Level dimension;
    private int originX;
    private int originY;
    private int originZ;
    private long lastSampleTick = Long.MIN_VALUE;

    public void tick(Level level, int playerX, int playerY, int playerZ, long gameTime) {
        boolean changedDimension = dimension != level;
        boolean moved = Math.abs(playerX - originX) >= 4 || Math.abs(playerZ - originZ) >= 4;
        if (!changedDimension && !moved && gameTime - lastSampleTick < 10) return;

        dimension = level;
        originX = playerX;
        originY = playerY;
        originZ = playerZ;
        lastSampleTick = gameTime;
        for (int gy = 0; gy < SIDE; gy++) {
            for (int gx = 0; gx < SIDE; gx++) {
                int x = originX + (gx - CENTER) * BLOCKS_PER_CELL;
                int z = originZ + (gy - CENTER) * BLOCKS_PER_CELL;
                BlockPos probe = new BlockPos(x, level.getMinBuildHeight(), z);
                if (!level.hasChunkAt(probe)) {
                    known[gy][gx] = false;
                    colors[gy][gx] = UNKNOWN;
                    continue;
                }

                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                int y = Math.max(level.getMinBuildHeight(), surfaceY - 1);
                BlockPos surface = new BlockPos(x, y, z);
                BlockState state = level.getBlockState(surface);
                MapColor mapColor = state.getMapColor(level, surface);
                int rgb = mapColor.col & 0x00FFFFFF;
                if (rgb == 0) rgb = UNKNOWN;

                // A restrained height tint makes cliffs legible without changing material hues.
                int shade = Math.max(168, Math.min(255, 220 + (y - originY) / 5));
                colors[gy][gx] = tint(rgb, shade);
                known[gy][gx] = true;
            }
        }
    }

    private static int tint(int rgb, int shade) {
        int r = ((rgb >> 16) & 255) * shade / 255;
        int g = ((rgb >> 8) & 255) * shade / 255;
        int b = (rgb & 255) * shade / 255;
        return (r << 16) | (g << 8) | b;
    }

    public int colorAt(int x, int y) { return colors[y][x]; }
    public boolean isKnown(int x, int y) { return known[y][x]; }
    public Level dimension() { return dimension; }
    public int originX() { return originX; }
    public int originZ() { return originZ; }

    public int deathCellX(int worldX) {
        return CENTER + Math.floorDiv(worldX - originX, BLOCKS_PER_CELL);
    }

    public int deathCellY(int worldZ) {
        return CENTER + Math.floorDiv(worldZ - originZ, BLOCKS_PER_CELL);
    }
}
