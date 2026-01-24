package com.thefusioncube.spawnerautocollect;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpawnerAutoCollectMod implements ModInitializer {

    public static final String MOD_ID = "spawner_auto_collect";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Spawner Auto Collect mod loaded successfully!");
    }
}
