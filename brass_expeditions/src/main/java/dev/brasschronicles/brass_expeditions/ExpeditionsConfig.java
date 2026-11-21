package dev.brasschronicles.brass_expeditions;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Server-side campaign thresholds and optional item rewards. Empty reward IDs disable an item prize. */
public final class ExpeditionsConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue SURVEY_BIOMES_GOAL = BUILDER
            .comment("Distinct registered biomes required for the Surveyor expedition.")
            .defineInRange("surveyBiomesGoal", 10, 1, 10000);
    public static final ModConfigSpec.IntValue VISIT_DIMENSIONS_GOAL = BUILDER
            .comment("Distinct registered dimensions required for the Far Horizons expedition.")
            .defineInRange("visitDimensionsGoal", 3, 1, 1000);
    public static final ModConfigSpec.IntValue ELYTRA_FLIGHT_BLOCKS_GOAL = BUILDER
            .comment("Server-observed elytra flight distance in blocks required for Windborne Surveyor.")
            .defineInRange("elytraFlightBlocksGoal", 5000, 1, 100000000);
    public static final ModConfigSpec.ConfigValue<String> SURVEY_REWARD_ITEM = BUILDER
            .comment("Optional item ID awarded once for completing Surveyor; empty disables the item reward.")
            .define("surveyRewardItem", "", ExpeditionsConfig::isOptionalResourceLocation);
    public static final ModConfigSpec.IntValue SURVEY_REWARD_COUNT = BUILDER
            .comment("Number of Surveyor reward items, when an item ID is configured.")
            .defineInRange("surveyRewardCount", 1, 1, 64);
    public static final ModConfigSpec.ConfigValue<String> DIMENSIONS_REWARD_ITEM = BUILDER
            .comment("Optional item ID awarded once for completing Far Horizons; empty disables the item reward.")
            .define("dimensionsRewardItem", "", ExpeditionsConfig::isOptionalResourceLocation);
    public static final ModConfigSpec.IntValue DIMENSIONS_REWARD_COUNT = BUILDER
            .comment("Number of Far Horizons reward items, when an item ID is configured.")
            .defineInRange("dimensionsRewardCount", 1, 1, 64);
    public static final ModConfigSpec.ConfigValue<String> ELYTRA_REWARD_ITEM = BUILDER
            .comment("Optional item ID awarded once for Windborne Surveyor; empty disables the item reward.")
            .define("elytraRewardItem", "", ExpeditionsConfig::isOptionalResourceLocation);
    public static final ModConfigSpec.IntValue ELYTRA_REWARD_COUNT = BUILDER
            .comment("Number of Windborne Surveyor reward items, when an item ID is configured.")
            .defineInRange("elytraRewardCount", 1, 1, 64);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ExpeditionsConfig() {}

    private static boolean isOptionalResourceLocation(Object value) {
        return value instanceof String text && (text.isBlank() || ResourceLocation.tryParse(text) != null);
    }
}
