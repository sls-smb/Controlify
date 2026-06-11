package com.controlify.mod.mixin;

import com.controlify.mod.ControlifyClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into position updates to feed the safety detector so it can detect
 * server-forced teleports without polling from the tick loop.
 * The actual detection logic lives in SafetyDetector; this mixin just provides
 * an early intercept point.
 */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void controlify$onTick(CallbackInfo ci) {
        // SafetyDetector.tick() is already called from ControlifyClient's END_CLIENT_TICK,
        // but we ensure the mixin is wired so future hooks can be added here.
    }
}
