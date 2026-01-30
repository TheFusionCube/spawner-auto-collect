package com.thefusioncube.spawnerautocollect.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.lwjgl.glfw.GLFW;

public class SpawnerAutoCollectClient implements ClientModInitializer {

    private static KeyBinding toggleKey;
    private static boolean enabled = false;

    private static long lastRun = 0;

    private static int step = 0;
    private static int delayTicks = 0;

    private static boolean lastScreenWasConfig = false;

    @Override
    public void onInitializeClient() {

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Toggle Spawner Auto Collect",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "Spawner Auto Collect"
        ));

        // 🔴 Auto-disable when disconnecting from a world or server
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            enabled = false;
            step = 0;
            delayTicks = 0;

            if (client.player != null) {
                client.player.sendMessage(Text.literal("§cSpawner Auto Collect disabled (disconnected)."), false);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            Screen current = client.currentScreen;

            // Detect Cloth Config open
            if (current != null && current.getClass().getName().contains("ClothConfigScreen")) {
                lastScreenWasConfig = true;
            }

            // Reload config after closing ClothConfig
            if (current == null && lastScreenWasConfig) {
                ConfigManager.reload();
                lastScreenWasConfig = false;

                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§aSpawner Auto Collect config reloaded."), true);
                }
            }

            // Toggle key
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                delayTicks = 0;
                step = 0;

                if (client.player != null) {
                    client.player.sendMessage(
                            Text.literal("Spawner Auto Collect: " +
                                    (enabled ? "§aENABLED" : "§cDISABLED")),
                            true
                    );
                }
            }

            if (!enabled || client.player == null) return;

            if (delayTicks > 0) {
                delayTicks--;
                return;
            }

            if (step == 0 && !isIntervalReady()) return;

            runSteps(client);
        });
    }

    private static long getIntervalMs() {
        return ConfigManager.get().loop_interval_seconds * 1000L;
    }

    private static boolean isIntervalReady() {
        return (System.currentTimeMillis() - lastRun) >= getIntervalMs();
    }

    private void runSteps(MinecraftClient client) {

        ClientPlayerEntity player = client.player;

        switch (step) {

            case 0 -> {
                if (client.crosshairTarget instanceof BlockHitResult hit) {
                    client.interactionManager.interactBlock(player, Hand.MAIN_HAND, hit);
                    step++;
                    delayTicks = 10;
                }
            }

            case 1 -> {
                if (client.currentScreen instanceof HandledScreen<?> screen) {
                    ScreenHandler handler = screen.getScreenHandler();

                    client.interactionManager.clickSlot(
                            handler.syncId,
                            ConfigManager.get().target_slot,
                            0,
                            SlotActionType.PICKUP,
                            player
                    );

                    step++;
                    delayTicks = 10;
                }
            }

            case 2 -> {
                player.networkHandler.sendChatCommand("sell");
                step++;
                delayTicks = 10;
            }

            case 3 -> {
                if (client.currentScreen instanceof HandledScreen<?> screen) {

                    ScreenHandler handler = screen.getScreenHandler();
                    int start = handler.slots.size() - 36;

                    for (int i = start; i < handler.slots.size(); i++) {
                        Slot slot = handler.slots.get(i);

                        if (slot.hasStack() && slot.getStack().isOf(Items.ARROW)) {

                            client.interactionManager.clickSlot(
                                    handler.syncId,
                                    i,
                                    0,
                                    SlotActionType.QUICK_MOVE,
                                    player
                            );
                        }
                    }

                    step++;
                    delayTicks = 10;
                }
            }

            case 4 -> {
                player.closeHandledScreen();
                lastRun = System.currentTimeMillis();
                step = 0;
            }
        }
    }
}
