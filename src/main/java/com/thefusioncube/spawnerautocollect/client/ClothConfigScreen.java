package com.thefusioncube.spawnerautocollect.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClothConfigScreen {

    public static Screen build(Screen parent) {
        ConfigManager config = ConfigManager.get();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Spawner Auto Collect Configuration"));

        builder.setSavingRunnable(ConfigManager::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));

        // Loop interval
        general.addEntry(entryBuilder
                .startIntField(Text.literal("Loop Interval (seconds)"), config.loop_interval_seconds)
                .setDefaultValue(120)
                .setSaveConsumer(newValue -> config.loop_interval_seconds = newValue) // IMPORTANT
                .build()
        );

        // Target slot
        general.addEntry(entryBuilder
                .startIntField(Text.literal("Target Slot"), config.target_slot)
                .setDefaultValue(13)
                .setSaveConsumer(newValue -> config.target_slot = newValue)
                .build()
        );

        // Keybind
        general.addEntry(entryBuilder
                .startStrField(Text.literal("Keybind"), config.keybind)
                .setDefaultValue("P")
                .setSaveConsumer(newValue -> config.keybind = newValue)
                .build()
        );

        return builder.build();
    }
}
