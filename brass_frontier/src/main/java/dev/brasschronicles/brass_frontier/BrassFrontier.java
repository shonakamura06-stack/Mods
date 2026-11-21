package dev.brasschronicles.brass_frontier;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** NeoForge entry point. Feature code is kept in this module so the JAR stays independently optional. */
@Mod("brass_frontier")
public final class BrassFrontier {
    public static final String MOD_ID = "brass_frontier";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public BrassFrontier() {
        LOGGER.info("Brass Frontier initialized");
    }
}
