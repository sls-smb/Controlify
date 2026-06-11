package com.controlify.mod.macro;

import com.controlify.mod.config.ControlifyConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

/**
 * Attacks the mob under the crosshair at a random interval between minDelay
 * and maxDelay (in seconds). Uses client.gameMode.attack() so no access
 * widener is needed.
 */
public class CombatMacro {

    private static final double REACH = 6.0;
    private final Random random = new Random();
    private long nextClickTime = 0;

    public void tick(Minecraft client, ControlifyConfig config) {
        if (client.player == null || client.level == null || client.gameMode == null) return;
        if (!config.isMacroEnabled()) return;

        Entity target = getMobTarget(client);
        if (target == null) return;

        long now = System.currentTimeMillis();
        if (now < nextClickTime) return;

        client.gameMode.attack(client.player, target);
        client.player.resetAttackStrengthTicker();

        float minMs = config.getMinDelay() * 1000f;
        float maxMs = config.getMaxDelay() * 1000f;
        nextClickTime = now + (long) (minMs + random.nextFloat() * (maxMs - minMs));
    }

    /**
     * Returns the mob entity under the crosshair, or null if none is targeted.
     */
    private Entity getMobTarget(Minecraft client) {
        // Fast path: use pre-computed crosshair target
        HitResult hit = client.hitResult;
        if (hit instanceof EntityHitResult ehr) {
            Entity e = ehr.getEntity();
            if (e instanceof LivingEntity && !(e instanceof Player)) return e;
        }

        // Fallback: manual AABB raycast
        return manualEntityRaycast(client);
    }

    private Entity manualEntityRaycast(Minecraft client) {
        Vec3 eyePos = client.player.getEyePosition();
        Vec3 lookVec = client.player.getViewVector(1.0f);
        Vec3 end = eyePos.add(lookVec.scale(REACH));

        AABB searchBox = client.player.getBoundingBox().expandTowards(lookVec.scale(REACH)).inflate(1.0);
        List<Entity> entities = client.level.getEntities(
                client.player, searchBox,
                e -> e instanceof LivingEntity && !(e instanceof Player) && e.isAlive()
        );

        for (Entity entity : entities) {
            AABB box = entity.getBoundingBox().inflate(0.1);
            if (box.clip(eyePos, end).isPresent()) return entity;
        }
        return null;
    }

    public boolean isTargetingMob(Minecraft client) {
        if (client == null || client.player == null || client.level == null) return false;
        return getMobTarget(client) != null;
    }

    public void reset() {
        nextClickTime = 0;
    }
}
