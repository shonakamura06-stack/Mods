package dev.brasschronicles.brass_cartography;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** NeoForge entry point. Feature code is kept in this module so the JAR stays independently optional. */
@Mod("brass_cartography")
public final class BrassCartography {
    public static final String MOD_ID = "brass_cartography";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public BrassCartography() {
        LOGGER.info("Brass Cartography initialized");
    }
}
