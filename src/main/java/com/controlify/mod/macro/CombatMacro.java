package com.controlify.mod.macro;

import com.controlify.mod.config.ControlifyConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Random;

/**
 * Performs a left-click attack when a mob (non-player living entity) is under
 * the crosshair. Click timing is randomised between minDelay and maxDelay to
 * avoid detectable fixed patterns.
 */
public class CombatMacro {

    private static final double REACH = 6.0;
    private final Random random = new Random();
    private long nextClickTime = 0;

    public void tick(MinecraftClient client, ControlifyConfig config) {
        if (client.player == null || client.world == null) return;
        if (!config.isMacroEnabled()) return;

        if (!isMobTargeted(client)) return;

        long now = System.currentTimeMillis();
        if (now < nextClickTime) return;

        // Simulate an attack key press for exactly one tick
        client.options.attackKey.setPressed(true);
        client.doAttack();
        client.options.attackKey.setPressed(false);

        // Schedule next click with random delay in [minDelay, maxDelay] seconds
        float minMs = config.getMinDelay() * 1000f;
        float maxMs = config.getMaxDelay() * 1000f;
        long delay = (long) (minMs + random.nextFloat() * (maxMs - minMs));
        nextClickTime = now + delay;
    }

    /**
     * Returns true if the player's crosshair is aimed at a non-player living entity
     * within reach, using the game's existing crosshair target first and falling back
     * to a manual raycast.
     */
    private boolean isMobTargeted(MinecraftClient client) {
        // Fast path: use the pre-computed crosshair target
        HitResult hit = client.crosshairTarget;
        if (hit instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            return entity instanceof LivingEntity && !(entity instanceof PlayerEntity);
        }

        // Fallback manual raycast for entities the vanilla target might miss
        return manualEntityRaycast(client);
    }

    private boolean manualEntityRaycast(MinecraftClient client) {
        Vec3d eyePos = client.player.getEyePos();
        Vec3d lookVec = client.player.getRotationVec(1.0f);
        Vec3d end = eyePos.add(lookVec.multiply(REACH));

        Box searchBox = client.player.getBoundingBox().stretch(lookVec.multiply(REACH)).expand(1.0);
        List<Entity> entities = client.world.getOtherEntities(
                client.player, searchBox,
                e -> e instanceof LivingEntity && !(e instanceof PlayerEntity) && e.isAlive()
        );

        for (Entity entity : entities) {
            Box entityBox = entity.getBoundingBox().expand(0.1);
            if (entityBox.raycast(eyePos, end).isPresent()) return true;
        }
        return false;
    }

    public boolean isTargetingMob(MinecraftClient client) {
        if (client.player == null || client.world == null) return false;
        return isMobTargeted(client);
    }

    public void reset() {
        nextClickTime = 0;
    }
}
