package com.controlify.mod.macro;

import com.controlify.mod.ControlifyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

/**
 * Detects server-forced position changes and unexpected hotbar slot switches.
 * When triggered: disables the macro, shows a red STOP overlay for 3 seconds,
 * and plays anvil sounds once per second.
 */
public class SafetyDetector {

    private int lastSlot = -1;
    private Vec3 lastPosition = null;
    private static final double TELEPORT_THRESHOLD = 10.0;

    private boolean stopActive = false;
    private long stopUntil = 0;
    private int anvilSoundTick = 0;

    public void tick(Minecraft client) {
        if (client.player == null) return;

        boolean triggered = false;

        // hotbar slot check
        int currentSlot = client.player.getInventory().selected;
        if (lastSlot != -1 && currentSlot != lastSlot && !isPlayerChangingSlot(client)) {
            triggered = true;
        }
        lastSlot = currentSlot;

        // teleport / position check
        Vec3 currentPos = client.player.position();
        if (lastPosition != null) {
            double delta = currentPos.distanceTo(lastPosition);
            if (delta > TELEPORT_THRESHOLD && !client.player.isPassenger()) {
                triggered = true;
            }
        }
        lastPosition = currentPos;

        if (triggered) activateStop(client);

        tickStopState(client);
    }

    private boolean isPlayerChangingSlot(Minecraft client) {
        for (int i = 0; i < 9; i++) {
            if (client.options.keyHotbarSlots[i].isDown()) return true;
        }
        return false;
    }

    private void activateStop(Minecraft client) {
        ControlifyClient.INSTANCE.getConfig().setMacroEnabled(false);
        ControlifyClient.INSTANCE.getMacro().reset();
        stopActive = true;
        stopUntil = System.currentTimeMillis() + 3000;
        anvilSoundTick = 0;
    }

    private void tickStopState(Minecraft client) {
        if (!stopActive) return;
        if (System.currentTimeMillis() >= stopUntil) {
            stopActive = false;
            return;
        }
        anvilSoundTick++;
        if (anvilSoundTick >= 20) {
            anvilSoundTick = 0;
            if (client.player != null) {
                client.player.playSound(SoundEvents.ANVIL_LAND, 1.0f, 1.0f);
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
