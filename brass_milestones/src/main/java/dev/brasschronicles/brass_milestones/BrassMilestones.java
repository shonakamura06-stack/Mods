package dev.brasschronicles.brass_milestones;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** NeoForge entry point. Feature code is kept in this module so the JAR stays independently optional. */
@Mod("brass_milestones")
public final class BrassMilestones {
    public static final String MOD_ID = "brass_milestones";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public BrassMilestones() {
        LOGGER.info("Brass Milestones initialized");
    }
}
