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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

/** Server-authoritative exploration records, isolated by world and player UUID. */
final class ExpeditionTracker {
    static final int SURVEY_BIOMES_GOAL = 10;
    static final int VISIT_DIMENSIONS_GOAL = 3;
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
        if (!changed) return;

        world.save();
        if (biomesBefore < SURVEY_BIOMES_GOAL && progress.biomes.size() >= SURVEY_BIOMES_GOAL) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.brass_expeditions.biome_survey_complete", SURVEY_BIOMES_GOAL), false);
        }
        if (dimensionsBefore < VISIT_DIMENSIONS_GOAL && progress.dimensions.size() >= VISIT_DIMENSIONS_GOAL) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.brass_expeditions.worlds_complete", VISIT_DIMENSIONS_GOAL), false);
        }
    }

    static Counts counts(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return new Counts(0, 0);
        PlayerProgress progress = world(server).players.get(player.getUUID());
        return progress == null ? new Counts(0, 0) : new Counts(progress.biomes.size(), progress.dimensions.size());
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

    record Counts(int uniqueBiomes, int dimensions) {}

    private static final class PlayerProgress {
        private final Set<String> biomes = new HashSet<>();
        private final Set<String> dimensions = new HashSet<>();
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
