package dev.brasschronicles.brass_expeditions;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** NeoForge entry point. Feature code is kept in this module so the JAR stays independently optional. */
@Mod("brass_expeditions")
public final class BrassExpeditions {
    public static final String MOD_ID = "brass_expeditions";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public BrassExpeditions() {
        LOGGER.info("Brass Expeditions initialized");
    }
}
