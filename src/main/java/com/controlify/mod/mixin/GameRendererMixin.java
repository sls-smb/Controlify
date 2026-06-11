package com.controlify.mod.mixin;

import com.controlify.mod.ControlifyClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Placeholder mixin — kept so the mixin config validates correctly.
 * Actual HUD rendering uses the HudRenderCallback event, not this mixin.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void controlify$onRenderTail(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        // reserved for future renderer hooks
    }
}
