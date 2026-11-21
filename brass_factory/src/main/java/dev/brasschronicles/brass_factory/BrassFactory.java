package dev.brasschronicles.brass_factory;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** NeoForge entry point. Feature code is kept in this module so the JAR stays independently optional. */
@Mod("brass_factory")
public final class BrassFactory {
    public static final String MOD_ID = "brass_factory";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public BrassFactory() {
        LOGGER.info("Brass Factory initialized");
    }
}
