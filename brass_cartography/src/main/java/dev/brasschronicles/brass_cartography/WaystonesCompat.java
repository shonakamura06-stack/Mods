package dev.brasschronicles.brass_cartography;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

/**
 * Optional, reflection-only Waystones adapter. It reads only the local player's activated list,
 * so the minimap does not reveal private or undiscovered server waystones.
 */
public final class WaystonesCompat {
    private static final String API = "net.blay09.mods.waystones.api.WaystonesAPI";
    private static final String WAYSTONE = "net.blay09.mods.waystones.api.Waystone";

    private WaystonesCompat() {}

    public static List<WaypointMarker> readActivated(Player player) {
        if (player == null || !ModList.get().isLoaded("waystones")) return List.of();
        try {
            ClassLoader loader = WaystonesCompat.class.getClassLoader();
            Class<?> apiClass = Class.forName(API, false, loader);
            Class<?> waystoneApi = Class.forName(WAYSTONE, false, loader);
            Method getter = apiClass.getMethod("getActivatedWaystones", Player.class);
            Object result = getter.invoke(null, player);
            if (!(result instanceof Collection<?> waystones)) return List.of();

            Method getPos = waystoneApi.getMethod("getPos");
            Method getDimension = waystoneApi.getMethod("getDimension");
            Method getName = waystoneApi.getMethod("getEffectiveName");
            List<WaypointMarker> markers = new ArrayList<>();
            for (Object waystone : waystones) {
                if (waystone == null || !waystoneApi.isInstance(waystone)) continue;
                Object pos = getPos.invoke(waystone);
                Object dimension = getDimension.invoke(waystone);
                Object component = getName.invoke(waystone);
                if (!(pos instanceof BlockPos blockPos) || !(dimension instanceof ResourceKey<?> key)) continue;
                String label = component instanceof Component text ? text.getString() : "Waystone";
                if (label.isBlank()) label = "Waystone";
                markers.add(new WaypointMarker(label, key.location().toString(), blockPos.getX(), blockPos.getZ()));
            }
            return List.copyOf(markers);
        } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
            BrassCartography.LOGGER.debug("Waystones is present but its optional API could not be read", e);
            return List.of();
        }
    }
}
