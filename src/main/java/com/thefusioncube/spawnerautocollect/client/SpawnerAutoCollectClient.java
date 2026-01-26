package com.thefusioncube.spawnerautocollect.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

import org.lwjgl.glfw.GLFW;

public class SpawnerAutoCollectClient implements ClientModInitializer {

    private static KeyBinding toggleKey;
    private static boolean enabled = false;

    private static long lastRun = 0;
    private static final long INTERVAL = 120_000; // 2 minutes

    @Override
    public void onInitializeClient() {

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Toggle Spawner Auto Collect",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "Spawner Auto Collect"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                System.out.println("[SpawnerAutoCollect] " + (enabled ? "ENABLED" : "DISABLED"));
            }

            if (!enabled || client.player == null || client.world == null) return;

            if (System.currentTimeMillis() - lastRun < INTERVAL) return;
            lastRun = System.currentTimeMillis();

            runSequence(client);
        });
    }

    private void runSequence(MinecraftClient client) {
        ClientPlayerEntity player = client.player;

        // 1. Right click block in front
        if (client.crosshairTarget instanceof BlockHitResult blockHit) {
            client.interactionManager.interactBlock(
                    player,
                    Hand.MAIN_HAND,
                    blockHit
            );
        }

        // Delay a little using thread (simple approach)
        new Thread(() -> {
            try {
                Thread.sleep(300);

                client.execute(() -> {

                    // 2. Click slot 13 if GUI open
                    if (client.currentScreen instanceof HandledScreen<?> screen) {
                        ScreenHandler handler = screen.getScreenHandler();

                        if (handler.slots.size() > 13) {
                            client.interactionManager.clickSlot(
                                    handler.syncId,
                                    13,
                                    0,
                                    net.minecraft.screen.slot.SlotActionType.PICKUP,
                                    player
                            );
                        }

                        // 3. Run /sell
                        player.networkHandler.sendChatCommand("sell");

                        // 4. Move all arrows into container
                        PlayerInventory inv = player.getInventory();

                        for (int i = 0; i < inv.size(); i++) {
                            Slot slot = handler.getSlot(i);
                            if (slot.hasStack() &&
                                    slot.getStack().getName().getString().toLowerCase().contains("arrow")) {

                                client.interactionManager.clickSlot(
                                        handler.syncId,
                                        i,
                                        0,
                                        net.minecraft.screen.slot.SlotActionType.QUICK_MOVE,
                                        player
                                );
                            }
                        }

                        // 5. Close GUI
                        player.closeHandledScreen();
                    }
                });

            } catch (InterruptedException ignored) {}
        }).start();
    }
}
