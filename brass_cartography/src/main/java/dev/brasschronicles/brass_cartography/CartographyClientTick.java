package dev.brasschronicles.brass_cartography;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Client-side sampler and atlas key handler. The sampler only reads chunks already in memory. */
@EventBusSubscriber(modid = BrassCartography.MOD_ID, value = Dist.CLIENT)
public final class CartographyClientTick {
    private static boolean settingsLoaded;

    private CartographyClientTick() {}

    @SubscribeEvent
    public static void afterClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!settingsLoaded) {
            CartographyState.load();
            settingsLoaded = true;
        }
        if (minecraft.level == null || minecraft.player == null) {
            CartographyState.MAP.flush();
            CartographyState.setWaystones(java.util.List.of());
            return;
        }

        var player = minecraft.player;
        var position = player.blockPosition();
        CartographyState.MAP.tick(minecraft.level, position.getX(), position.getY(), position.getZ(), minecraft.level.getGameTime());
        if (minecraft.level.getGameTime() % 40 == 0) {
            CartographyState.setWaystones(WaystonesCompat.readActivated(player));
        }
        CartographyState.observeLife(player.isAlive(), position.getX(), position.getY(), position.getZ(),
                minecraft.level.dimension().location());

        if (CartographyModEvents.openAtlasKey().consumeClick()) {
            minecraft.setScreen(new AtlasScreen());
        }
        while (CartographyModEvents.toggleMinimapKey().consumeClick()) {
            CartographyState.toggleMinimap();
        }
    }
}
