package com.controlify.mod.macro;

import com.controlify.mod.config.ControlifyConfig;
import net.minecraft.client.KeyMapping;
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

public class CombatMacro {

    private static final double REACH = 6.0;
    private final Random random = new Random();
    private long nextClickTime = 0;

    public void tick(Minecraft client, ControlifyConfig config) {
        if (client.player == null || client.level == null) return;
        if (!config.isMacroEnabled()) return;

        if (!isMobTargeted(client)) return;

        long now = System.currentTimeMillis();
        if (now < nextClickTime) return;

        // Simulate a genuine left-click through the vanilla key-binding system.
        // KeyMapping.click() queues a click that Minecraft.handleKeybinds() processes
        // in the same tick via startAttack() — identical to a real mouse press.
        KeyMapping.click(client.options.keyAttack.key);

        float minMs = config.getMinDelay() * 1000f;
        float maxMs = config.getMaxDelay() * 1000f;
        nextClickTime = now + (long) (minMs + random.nextFloat() * (maxMs - minMs));
    }

    private boolean isMobTargeted(Minecraft client) {
        HitResult hit = client.hitResult;
        if (hit instanceof EntityHitResult ehr) {
            Entity e = ehr.getEntity();
            return e instanceof LivingEntity && !(e instanceof Player);
        }
        return manualEntityRaycast(client);
    }

    private boolean manualEntityRaycast(Minecraft client) {
        Vec3 eyePos = client.player.getEyePosition();
        Vec3 lookVec = client.player.getViewVector(1.0f);
        Vec3 end = eyePos.add(lookVec.scale(REACH));

        AABB searchBox = client.player.getBoundingBox().expandTowards(lookVec.scale(REACH)).inflate(1.0);
        List<Entity> entities = client.level.getEntities(
                client.player, searchBox,
                e -> e instanceof LivingEntity && !(e instanceof Player) && e.isAlive()
        );

        for (Entity entity : entities) {
            if (entity.getBoundingBox().inflate(0.1).clip(eyePos, end).isPresent()) return true;
        }
        return false;
    }

    public boolean isTargetingMob(Minecraft client) {
        if (client == null || client.player == null || client.level == null) return false;
        return isMobTargeted(client);
    }

    public void reset() {
        nextClickTime = 0;
    }
}
