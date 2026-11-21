package dev.brasschronicles.brass_cartography;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;

/** Parchment atlas backed by the player's bounded, persistent survey history. */
public final class AtlasScreen extends Screen {
    private static final ResourceLocation PANEL = ResourceLocation.fromNamespaceAndPath(
            BrassCartography.MOD_ID, "textures/gui/brass_parchment_panel.png");
    private static final int PANEL_SIZE = 384;
    private static final int GRID_SIDE = 31;
    private static final int GRID_CENTER = GRID_SIDE / 2;
    private static final int TILE = 8;
    private static int zoom = 0;
    private int centerCellX;
    private int centerCellZ;

    public AtlasScreen() { super(Component.translatable("screen.brass_cartography.atlas")); }

    @Override
    protected void init() {
        centerOnPlayer();
        int left = (width - PANEL_SIZE) / 2;
        int top = (height - PANEL_SIZE) / 2;
        addRenderableWidget(Button.builder(Component.literal("−"), b -> zoom = Math.max(0, zoom - 1))
                .bounds(left + 44, top + 320, 62, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), b -> zoom = Math.min(4, zoom + 1))
                .bounds(left + 278, top + 320, 62, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.brass_cartography.recenter"), b -> centerOnPlayer())
                .bounds(left + 136, top + 320, 112, 20).build());
        addRenderableWidget(Button.builder(deathLabel(), b -> {
            CartographyState.toggleDeathMarker();
            b.setMessage(deathLabel());
        }).bounds(left + 8, top + 350, 84, 20).build());
        Button clearDeath = Button.builder(Component.translatable("screen.brass_cartography.clear_death"), b -> {
            CartographyState.clearDeathMarker();
            b.active = false;
        }).bounds(left + 104, top + 350, 84, 20).build();
        clearDeath.active = CartographyState.hasDeath();
        addRenderableWidget(clearDeath);
        addRenderableWidget(Button.builder(minimapLabel(), b -> {
            CartographyState.toggleMinimap();
            b.setMessage(minimapLabel());
        }).bounds(left + 200, top + 350, 84, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(left + 296, top + 350, 84, 20).build());
    }

    private void centerOnPlayer() {
        if (minecraft == null || minecraft.player == null) return;
        centerCellX = Math.floorDiv(minecraft.player.blockPosition().getX(), MapSampler.BLOCKS_PER_CELL);
        centerCellZ = Math.floorDiv(minecraft.player.blockPosition().getZ(), MapSampler.BLOCKS_PER_CELL);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int step = 1 << zoom;
        if (keyCode == GLFW.GLFW_KEY_LEFT) centerCellX -= step;
        else if (keyCode == GLFW.GLFW_KEY_RIGHT) centerCellX += step;
        else if (keyCode == GLFW.GLFW_KEY_UP) centerCellZ -= step;
        else if (keyCode == GLFW.GLFW_KEY_DOWN) centerCellZ += step;
        else if (keyCode == GLFW.GLFW_KEY_R) centerOnPlayer();
        else return super.keyPressed(keyCode, scanCode, modifiers);
        return true;
    }

    private static Component deathLabel() {
        return Component.translatable(CartographyState.deathMarkerVisible()
                ? "screen.brass_cartography.death_on" : "screen.brass_cartography.death_off");
    }

    private static Component minimapLabel() {
        return Component.translatable(CartographyState.minimapVisible()
                ? "screen.brass_cartography.minimap_on" : "screen.brass_cartography.minimap_off");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int left = (width - PANEL_SIZE) / 2;
        int top = (height - PANEL_SIZE) / 2;
        graphics.blit(PANEL, left, top, 0, 0, PANEL_SIZE, PANEL_SIZE, PANEL_SIZE, PANEL_SIZE);
        graphics.drawCenteredString(font, title, width / 2, top + 22, 0x3D2D1E);

        MapSampler map = CartographyState.MAP;
        int mapLeft = left + 68;
        int mapTop = top + 58;
        int scale = 1 << zoom;
        int playerCellX = minecraft != null && minecraft.player != null
                ? Math.floorDiv(minecraft.player.blockPosition().getX(), MapSampler.BLOCKS_PER_CELL) : 0;
        int playerCellZ = minecraft != null && minecraft.player != null
                ? Math.floorDiv(minecraft.player.blockPosition().getZ(), MapSampler.BLOCKS_PER_CELL) : 0;

        for (int gy = 0; gy < GRID_SIDE; gy++) {
            for (int gx = 0; gx < GRID_SIDE; gx++) {
                int cellX = centerCellX + (gx - GRID_CENTER) * scale;
                int cellZ = centerCellZ + (gy - GRID_CENTER) * scale;
                int surveyed = map.exploredColor(cellX, cellZ);
                int color = surveyed < 0 ? 0xFF62503B : (surveyed | 0xFF000000);
                int x1 = mapLeft + gx * TILE;
                int y1 = mapTop + gy * TILE;
                graphics.fill(x1, y1, x1 + TILE, y1 + TILE, color);
                if (surveyed < 0) {
                    graphics.fill(x1, y1, x1 + TILE, y1 + TILE, 0x553A2D20);
                }
            }
        }

        // Player marker, north reference, and remembered death location.
        if (minecraft != null && minecraft.player != null) {
            int playerGridX = GRID_CENTER + Math.floorDiv(playerCellX - centerCellX, scale);
            int playerGridY = GRID_CENTER + Math.floorDiv(playerCellZ - centerCellZ, scale);
            if (playerGridX >= 0 && playerGridX < GRID_SIDE && playerGridY >= 0 && playerGridY < GRID_SIDE) {
                int playerMarkerX = mapLeft + playerGridX * TILE + TILE / 2;
                int playerMarkerY = mapTop + playerGridY * TILE + TILE / 2;
                graphics.fill(playerMarkerX - 4, playerMarkerY - 4, playerMarkerX + 5, playerMarkerY + 5, 0xFF3B2B1B);
                graphics.fill(playerMarkerX - 2, playerMarkerY - 2, playerMarkerX + 3, playerMarkerY + 3, 0xFFE0B55E);
            }
        }
        graphics.drawString(font, Component.literal("N ↑"), mapLeft + GRID_SIDE * TILE - 30, mapTop - 13, 0xFF4B3520, false);

        Level level = minecraft == null ? null : minecraft.level;
        if (level != null && minecraft.player != null && CartographyState.deathMarkerVisible()
                && CartographyState.deathIsIn(level.dimension().location())) {
            int deathCellX = Math.floorDiv(CartographyState.deathX(), MapSampler.BLOCKS_PER_CELL);
            int deathCellZ = Math.floorDiv(CartographyState.deathZ(), MapSampler.BLOCKS_PER_CELL);
            int gx = GRID_CENTER + Math.floorDiv(deathCellX - centerCellX, scale);
            int gy = GRID_CENTER + Math.floorDiv(deathCellZ - centerCellZ, scale);
            if (gx >= 0 && gx < GRID_SIDE && gy >= 0 && gy < GRID_SIDE) {
                int mx = mapLeft + gx * TILE + TILE / 2;
                int my = mapTop + gy * TILE + TILE / 2;
                graphics.fill(mx - 3, my - 1, mx + 4, my + 2, 0xFF9B3528);
                graphics.fill(mx - 1, my - 3, mx + 2, my + 4, 0xFF9B3528);
            }
        }
        if (level != null) {
            for (WaypointMarker marker : CartographyState.waystones()) {
                if (!marker.dimension().equals(level.dimension().location().toString())) continue;
                int cellX = Math.floorDiv(marker.x(), MapSampler.BLOCKS_PER_CELL);
                int cellZ = Math.floorDiv(marker.z(), MapSampler.BLOCKS_PER_CELL);
                int gx = GRID_CENTER + Math.floorDiv(cellX - centerCellX, scale);
                int gy = GRID_CENTER + Math.floorDiv(cellZ - centerCellZ, scale);
                if (gx < 0 || gx >= GRID_SIDE || gy < 0 || gy >= GRID_SIDE) continue;
                int mx = mapLeft + gx * TILE + TILE / 2;
                int my = mapTop + gy * TILE + TILE / 2;
                graphics.fill(mx - 3, my - 3, mx + 4, my + 4, 0xFF8E542D);
                String label = marker.name().length() > 12 ? marker.name().substring(0, 12) : marker.name();
                graphics.drawString(font, label, mx + 5, my - 3, 0xFF3D2D1E, true);
            }
        }

        if (minecraft != null && minecraft.player != null) {
            String coords = "X " + (centerCellX * MapSampler.BLOCKS_PER_CELL) + "   Z "
                    + (centerCellZ * MapSampler.BLOCKS_PER_CELL) + "   1:" + (8 * scale);
            graphics.drawCenteredString(font, coords, width / 2, top + 309, 0x493724);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
