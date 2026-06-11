package com.controlify.mod.macro;

import com.controlify.mod.ControlifyClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

/**
 * Detects server-forced position changes and unexpected hotbar slot switches
 * that indicate external interference. When triggered, disables the macro and
 * shows a red STOP overlay for up to 3 seconds.
 */
public class SafetyDetector {

    private int lastSlot = -1;
    private Vec3d lastPosition = null;
    // threshold: teleport if moved more than this distance in one tick without sprinting
    private static final double TELEPORT_THRESHOLD = 10.0;

    private boolean stopActive = false;
    private long stopUntil = 0;
    private int anvilSoundTick = 0;

    public void tick(MinecraftClient client) {
        if (client.player == null) return;

        boolean triggered = false;

        // --- hotbar slot check ---
        int currentSlot = client.player.getInventory().selectedSlot;
        if (lastSlot != -1 && currentSlot != lastSlot) {
            // Only flag if the player didn't press a slot key themselves.
            // We detect this by checking if no number key is pressed.
            if (!isPlayerChangingSlot(client)) {
                triggered = true;
            }
        }
        lastSlot = currentSlot;

        // --- position / teleport check ---
        Vec3d currentPos = client.player.getPos();
        if (lastPosition != null) {
            double delta = currentPos.distanceTo(lastPosition);
            boolean isOnGround = client.player.isOnGround();
            boolean isSprinting = client.player.isSprinting();
            // Ignore normal movement; flag only sudden large jumps
            if (delta > TELEPORT_THRESHOLD && !client.player.hasVehicle()) {
                triggered = true;
            }
        }
        lastPosition = currentPos;

        // --- trigger stop ---
        if (triggered) {
            activateStop(client);
        }

        // --- tick stop timer and sounds ---
        tickStopState(client);
    }

    private boolean isPlayerChangingSlot(MinecraftClient client) {
        // Check vanilla key bindings for slots 1-9
        for (int i = 0; i < 9; i++) {
            if (client.options.hotbarKeys[i].isPressed()) return true;
        }
        return false;
    }

    private void activateStop(MinecraftClient client) {
        // Disable the macro immediately
        ControlifyClient.INSTANCE.getConfig().setMacroEnabled(false);
        ControlifyClient.INSTANCE.getMacro().reset();

        stopActive = true;
        stopUntil = System.currentTimeMillis() + 3000;
        anvilSoundTick = 0;
    }

    private void tickStopState(MinecraftClient client) {
        if (!stopActive) return;

        long now = System.currentTimeMillis();
        if (now >= stopUntil) {
            stopActive = false;
            return;
        }

        // Play anvil sound every ~20 ticks (1 second)
        anvilSoundTick++;
        if (anvilSoundTick >= 20) {
            anvilSoundTick = 0;
            if (client.player != null) {
                client.player.playSound(SoundEvents.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
            }
        }
    }

    public boolean isStopActive() { return stopActive; }
    public long getStopUntil() { return stopUntil; }

    public void reset() {
        lastSlot = -1;
        lastPosition = null;
        stopActive = false;
    }
}
