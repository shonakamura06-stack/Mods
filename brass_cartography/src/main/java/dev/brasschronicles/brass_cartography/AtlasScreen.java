package dev.brasschronicles.brass_cartography;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** The first atlas view. It paints only terrain samples already present in the client cache. */
public final class AtlasScreen extends Screen {
    private static final ResourceLocation PANEL = ResourceLocation.fromNamespaceAndPath(
            BrassCartography.MOD_ID, "textures/gui/brass_parchment_panel.png");
    private static final int PANEL_SIZE = 384;
    private Button deathButton;
    private Button minimapButton;

    public AtlasScreen() { super(Component.translatable("screen.brass_cartography.atlas")); }

    @Override
    protected void init() {
        int left = (width - PANEL_SIZE) / 2;
        int top = (height - PANEL_SIZE) / 2;
        deathButton = Button.builder(deathLabel(), b -> {
            CartographyState.toggleDeathMarker();
            b.setMessage(deathLabel());
        }).bounds(left + 17, top + 350, 110, 20).build();
        minimapButton = Button.builder(minimapLabel(), b -> {
            CartographyState.toggleMinimap();
            b.setMessage(minimapLabel());
        }).bounds(left + 137, top + 350, 110, 20).build();
        addRenderableWidget(deathButton);
        addRenderableWidget(minimapButton);
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(left + 257, top + 350, 110, 20).build());
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

        int tile = 16;
        int mapLeft = left + 56;
        int mapTop = top + 58;
        MapSampler map = CartographyState.MAP;
        for (int y = 0; y < MapSampler.SIDE; y++) {
            for (int x = 0; x < MapSampler.SIDE; x++) {
                int color = map.colorAt(x, y) | 0xFF000000;
                graphics.fill(mapLeft + x * tile, mapTop + y * tile,
                        mapLeft + (x + 1) * tile, mapTop + (y + 1) * tile, color);
                if (!map.isKnown(x, y)) {
                    graphics.fill(mapLeft + x * tile, mapTop + y * tile,
                            mapLeft + (x + 1) * tile, mapTop + (y + 1) * tile, 0x553A2D20);
                }
            }
        }

        // Player marker: an ivory survey pin with a brass center.
        int centerX = mapLeft + MapSampler.CENTER * tile + tile / 2;
        int centerY = mapTop + MapSampler.CENTER * tile + tile / 2;
        graphics.fill(centerX - 4, centerY - 4, centerX + 5, centerY + 5, 0xFF3B2B1B);
        graphics.fill(centerX - 2, centerY - 2, centerX + 3, centerY + 3, 0xFFE0B55E);

        Level level = minecraft == null ? null : minecraft.level;
        if (level != null && CartographyState.deathMarkerVisible()
                && CartographyState.deathIsIn(level.dimension().location())) {
            int dx = map.deathCellX(CartographyState.deathX());
            int dy = map.deathCellY(CartographyState.deathZ());
            if (dx >= 0 && dx < MapSampler.SIDE && dy >= 0 && dy < MapSampler.SIDE) {
                int mx = mapLeft + dx * tile + tile / 2;
                int my = mapTop + dy * tile + tile / 2;
                graphics.fill(mx - 4, my - 1, mx + 5, my + 2, 0xFF9B3528);
                graphics.fill(mx - 1, my - 4, mx + 2, my + 5, 0xFF9B3528);
            }
        }

        if (level != null && minecraft.player != null) {
            String coords = "X " + minecraft.player.blockPosition().getX() + "   Z "
                    + minecraft.player.blockPosition().getZ();
            graphics.drawCenteredString(font, coords, width / 2, top + 335, 0x493724);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
