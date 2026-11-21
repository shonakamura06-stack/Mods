package dev.brasschronicles.brass_expeditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

/** Server-authoritative exploration records, isolated by world and player UUID. */
final class ExpeditionTracker {
    static int surveyBiomesGoal() { return ExpeditionsConfig.SURVEY_BIOMES_GOAL.get(); }
    static int visitDimensionsGoal() { return ExpeditionsConfig.VISIT_DIMENSIONS_GOAL.get(); }
    static int elytraFlightBlocksGoal() { return ExpeditionsConfig.ELYTRA_FLIGHT_BLOCKS_GOAL.get(); }
    private static final String FILE_NAME = "brass_expeditions.json";
    private static final Map<MinecraftServer, WorldProgress> WORLDS = new WeakHashMap<>();

    private ExpeditionTracker() {}

    static void sample(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        var biomeKey = player.serverLevel().getBiome(player.blockPosition()).unwrapKey();
        if (biomeKey.isEmpty()) return;

        WorldProgress world = world(server);
        PlayerProgress progress = world.players.computeIfAbsent(player.getUUID(), ignored -> new PlayerProgress());
        int biomesBefore = progress.biomes.size();
        int dimensionsBefore = progress.dimensions.size();
        progress.biomes.add(biomeKey.get().location().toString());
        progress.dimensions.add(player.serverLevel().dimension().location().toString());

        boolean changed = biomesBefore != progress.biomes.size() || dimensionsBefore != progress.dimensions.size();
        boolean surveyComplete = progress.biomes.size() >= surveyBiomesGoal()
                && progress.completed.add("surveyor");
        boolean worldsComplete = progress.dimensions.size() >= visitDimensionsGoal()
                && progress.completed.add("far_horizons");
        if (!changed && !surveyComplete && !worldsComplete) return;

        // Persist the one-time completion flags before issuing configured item rewards.
        world.save();
        if (surveyComplete) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.brass_expeditions.biome_survey_complete", surveyBiomesGoal()), false);
            giveReward(player, ExpeditionsConfig.SURVEY_REWARD_ITEM.get(), ExpeditionsConfig.SURVEY_REWARD_COUNT.get());
        }
        if (worldsComplete) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.brass_expeditions.worlds_complete", visitDimensionsGoal()), false);
            giveReward(player, ExpeditionsConfig.DIMENSIONS_REWARD_ITEM.get(), ExpeditionsConfig.DIMENSIONS_REWARD_COUNT.get());
        }
    }

    static void recordElytraFlight(ServerPlayer player, int blocks) {
        if (blocks <= 0) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;
        WorldProgress world = world(server);
        PlayerProgress progress = world.players.computeIfAbsent(player.getUUID(), ignored -> new PlayerProgress());
        progress.elytraFlightBlocks = Math.min(100_000_000L,
                progress.elytraFlightBlocks + blocks);
        boolean complete = progress.elytraFlightBlocks >= elytraFlightBlocksGoal()
                && progress.completed.add("windborne_surveyor");
        world.save();
        if (complete) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.brass_expeditions.elytra_flight_complete", elytraFlightBlocksGoal()), false);
            giveReward(player, ExpeditionsConfig.ELYTRA_REWARD_ITEM.get(), ExpeditionsConfig.ELYTRA_REWARD_COUNT.get());
        }
    }

    static Counts counts(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return new Counts(0, 0, 0);
        PlayerProgress progress = world(server).players.get(player.getUUID());
        return progress == null ? new Counts(0, 0, 0)
                : new Counts(progress.biomes.size(), progress.dimensions.size(), progress.elytraFlightBlocks);
    }

    private static void giveReward(ServerPlayer player, String itemId, int amount) {
        if (itemId == null || itemId.isBlank() || amount <= 0) return;
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            BrassExpeditions.LOGGER.warn("Invalid expedition reward item ID: {}", itemId);
            return;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        if (item == null) {
            BrassExpeditions.LOGGER.warn("Unknown expedition reward item ID: {}", itemId);
            return;
        }
        int remaining = amount;
        while (remaining > 0) {
            int count = Math.min(remaining, item.getDefaultMaxStackSize());
            ItemStack stack = new ItemStack(item, count);
            player.getInventory().add(stack);
            if (!stack.isEmpty()) player.drop(stack, false);
            remaining -= count;
        }
    }

    private static WorldProgress world(MinecraftServer server) {
        synchronized (WORLDS) {
            return WORLDS.computeIfAbsent(server, ExpeditionTracker::load);
        }
    }

    private static WorldProgress load(MinecraftServer server) {
        Path file = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(FILE_NAME);
        WorldProgress result = new WorldProgress(file);
        if (!Files.isRegularFile(file)) return result;

        try (var reader = Files.newBufferedReader(file)) {
            JsonElement rootElement = JsonParser.parseReader(reader);
            if (!rootElement.isJsonObject()) return result;
            JsonObject root = rootElement.getAsJsonObject();
            if (!root.has("players") || !root.get("players").isJsonObject()) return result;
            for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("players").entrySet()) {
                UUID id;
                try { id = UUID.fromString(entry.getKey()); }
                catch (IllegalArgumentException ignored) { continue; }
                if (!entry.getValue().isJsonObject()) continue;
                JsonObject encoded = entry.getValue().getAsJsonObject();
                PlayerProgress progress = new PlayerProgress();
                readSet(encoded, "biomes", progress.biomes);
                readSet(encoded, "dimensions", progress.dimensions);
                readSet(encoded, "completed", progress.completed);
                if (encoded.has("elytraFlightBlocks") && encoded.get("elytraFlightBlocks").isJsonPrimitive()) {
                    try { progress.elytraFlightBlocks = Math.max(0, encoded.get("elytraFlightBlocks").getAsLong()); }
                    catch (RuntimeException ignored) { progress.elytraFlightBlocks = 0; }
                }
                result.players.put(id, progress);
            }
        } catch (IOException | RuntimeException error) {
            BrassExpeditions.LOGGER.warn("Could not load expedition progress from {}", file, error);
        }
        return result;
    }

    private static void readSet(JsonObject source, String key, Set<String> destination) {
        if (!source.has(key) || !source.get(key).isJsonArray()) return;
        for (JsonElement value : source.getAsJsonArray(key)) {
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                destination.add(value.getAsString());
            }
        }
    }

    record Counts(int uniqueBiomes, int dimensions, long elytraFlightBlocks) {}

    private static final class PlayerProgress {
        private final Set<String> biomes = new HashSet<>();
        private final Set<String> dimensions = new HashSet<>();
        private final Set<String> completed = new HashSet<>();
        private long elytraFlightBlocks;
    }

    private static final class WorldProgress {
        private final Path file;
        private final Map<UUID, PlayerProgress> players = new HashMap<>();

        private WorldProgress(Path file) { this.file = file; }

        private void save() {
            JsonObject root = new JsonObject();
            root.addProperty("format", 1);
            JsonObject encodedPlayers = new JsonObject();
            for (Map.Entry<UUID, PlayerProgress> entry : players.entrySet()) {
                JsonObject encoded = new JsonObject();
                encoded.add("biomes", encodeSet(entry.getValue().biomes));
                encoded.add("dimensions", encodeSet(entry.getValue().dimensions));
                encoded.add("completed", encodeSet(entry.getValue().completed));
                encoded.addProperty("elytraFlightBlocks", entry.getValue().elytraFlightBlocks);
                encodedPlayers.add(entry.getKey().toString(), encoded);
            }
            root.add("players", encodedPlayers);

            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            try {
                Files.createDirectories(file.getParent());
                Files.writeString(temporary, root.toString());
                try {
                    Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException ignored) {
                    Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException error) {
                BrassExpeditions.LOGGER.warn("Could not save expedition progress to {}", file, error);
            }
        }

        private static JsonArray encodeSet(Set<String> values) {
            JsonArray result = new JsonArray();
            values.stream().sorted().forEach(result::add);
            return result;
        }
    }
}
