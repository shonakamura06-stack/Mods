package dev.brasschronicles.brass_cartography;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.lwjgl.glfw.GLFW;

/** Client-only input and HUD integration; never loaded on a dedicated server. */
@EventBusSubscriber(modid = BrassCartography.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class CartographyModEvents {
    private static final ResourceLocation PANEL = ResourceLocation.fromNamespaceAndPath(
            BrassCartography.MOD_ID, "textures/gui/brass_parchment_panel.png");
    private static final ResourceLocation MINIMAP_LAYER = ResourceLocation.fromNamespaceAndPath(
            BrassCartography.MOD_ID, "minimap");
    private static final KeyMapping OPEN_ATLAS = new KeyMapping(
            "key.brass_cartography.open_atlas", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M,
            "key.categories.brass_cartography");
    private static final KeyMapping TOGGLE_MINIMAP = new KeyMapping(
            "key.brass_cartography.toggle_minimap", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N,
            "key.categories.brass_cartography");

    private CartographyModEvents() {}

    public static KeyMapping openAtlasKey() { return OPEN_ATLAS; }
    public static KeyMapping toggleMinimapKey() { return TOGGLE_MINIMAP; }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_ATLAS);
        event.register(TOGGLE_MINIMAP);
    }

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, MINIMAP_LAYER, CartographyModEvents::renderMinimap);
    }

    private static void renderMinimap(GuiGraphics graphics, net.minecraft.client.DeltaTracker partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!CartographyState.minimapVisible() || minecraft.screen != null
                || minecraft.player == null || minecraft.level == null || minecraft.options.hideGui) return;

        int size = 144;
        int left = minecraft.getWindow().getGuiScaledWidth() - size - 8;
        int top = 8;
        graphics.blit(PANEL, left, top, 0, 0, size, size, 384, 384);
        int tile = 6;
        int mapLeft = left + 21;
        int mapTop = top + 19;
        MapSampler map = CartographyState.MAP;
        for (int y = 0; y < MapSampler.SIDE; y++) {
            for (int x = 0; x < MapSampler.SIDE; x++) {
                graphics.fill(mapLeft + x * tile, mapTop + y * tile,
                        mapLeft + (x + 1) * tile, mapTop + (y + 1) * tile,
                        map.colorAt(x, y) | 0xFF000000);
            }
        }
        graphics.fill(mapLeft + MapSampler.CENTER * tile + 1, mapTop + MapSampler.CENTER * tile + 1,
                mapLeft + MapSampler.CENTER * tile + 5, mapTop + MapSampler.CENTER * tile + 5, 0xFFE0B55E);
        if (CartographyState.deathMarkerVisible()
                && CartographyState.deathIsIn(minecraft.level.dimension().location())) {
            int dx = map.deathCellX(CartographyState.deathX());
            int dy = map.deathCellY(CartographyState.deathZ());
            if (dx >= 0 && dx < MapSampler.SIDE && dy >= 0 && dy < MapSampler.SIDE) {
                int mx = mapLeft + dx * tile + tile / 2;
                int my = mapTop + dy * tile + tile / 2;
                graphics.fill(mx - 2, my, mx + 3, my + 1, 0xFF9B3528);
                graphics.fill(mx, my - 2, mx + 1, my + 3, 0xFF9B3528);
            }
        }
        graphics.drawString(minecraft.font, Component.literal("N"), left + size - 19, top + 8, 0xFF4B3520, false);
    }
}
