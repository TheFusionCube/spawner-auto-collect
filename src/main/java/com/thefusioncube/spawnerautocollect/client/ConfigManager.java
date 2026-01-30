package com.thefusioncube.spawnerautocollect.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("spawner_auto_collect.json");

    private static ConfigManager INSTANCE;

    // Config values with defaults
    public int loop_interval_seconds = 120;
    public int target_slot = 13;
    public String keybind = "P";

    // Singleton accessor
    public static ConfigManager get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    // Load config from disk
    private static ConfigManager load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                return GSON.fromJson(Files.readString(CONFIG_PATH), ConfigManager.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create file if missing
        ConfigManager cfg = new ConfigManager();
        save();
        return cfg;
    }

    // Save config to disk
    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(get()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // NEW: Reload config from disk
    public static void reload() {
        INSTANCE = load();
    }
}
