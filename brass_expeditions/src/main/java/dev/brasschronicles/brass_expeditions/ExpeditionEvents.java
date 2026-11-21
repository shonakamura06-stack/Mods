package dev.brasschronicles.brass_expeditions;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Server-only discovery sampling and a per-player progress command. */
@EventBusSubscriber(modid = BrassExpeditions.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class ExpeditionEvents {
    private ExpeditionEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) return;
        if (player.serverLevel().getGameTime() % 40L != 0L) return;
        ExpeditionTracker.sample(player);
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) return;
        if (player.isFallFlying() && player.tickCount % 20 == 0) {
            ExpeditionTracker.recordElytraFlight(player, 40);
        }
    }

    @SubscribeEvent
    public static void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.isSpectator()) {
            // Record short dimension visits immediately instead of waiting for the periodic sample.
            ExpeditionTracker.sample(player);
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("expeditions")
                .executes(context -> showProgress(context.getSource().getPlayerOrException())));
    }

    private static int showProgress(ServerPlayer player) {
        ExpeditionTracker.Counts counts = ExpeditionTracker.counts(player);
        player.sendSystemMessage(Component.translatable("command.brass_expeditions.progress"));
        player.sendSystemMessage(Component.translatable("command.brass_expeditions.biomes",
                counts.uniqueBiomes(), ExpeditionTracker.surveyBiomesGoal()));
        player.sendSystemMessage(Component.translatable("command.brass_expeditions.dimensions",
                counts.dimensions(), ExpeditionTracker.visitDimensionsGoal()));
        player.sendSystemMessage(Component.translatable("command.brass_expeditions.elytra_distance",
                counts.elytraFlightBlocks(), ExpeditionTracker.elytraFlightBlocksGoal()));
        return 1;
    }
}
