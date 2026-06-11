package com.controlify.mod;

import com.controlify.mod.config.ControlifyConfig;
import com.controlify.mod.gui.ControlifyScreen;
import com.controlify.mod.hud.ControlifyHud;
import com.controlify.mod.macro.CombatMacro;
import com.controlify.mod.macro.SafetyDetector;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class ControlifyClient implements ClientModInitializer {

    public static ControlifyClient INSTANCE;

    private final ControlifyConfig config = new ControlifyConfig();
    private final CombatMacro macro = new CombatMacro();
    private final SafetyDetector safetyDetector = new SafetyDetector();
    private final ControlifyHud hud = new ControlifyHud();

    private KeyMapping openGuiKey;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.controlify.open_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "Controlify"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);

        HudRenderCallback.EVENT.register((graphics, tickCounter) ->
                hud.render(graphics, Minecraft.getInstance()));
    }

    private void onEndTick(Minecraft client) {
        if (client.player == null) return;

        safetyDetector.tick(client);

        while (openGuiKey.consumeClick()) {
            if (client.screen == null) {
                client.setScreen(new ControlifyScreen());
            }
        }

        if (config.isMacroEnabled() && !safetyDetector.isStopActive() && client.screen == null) {
            macro.tick(client, config);
        }
    }

    public ControlifyConfig getConfig() { return config; }
    public CombatMacro getMacro() { return macro; }
    public SafetyDetector getSafetyDetector() { return safetyDetector; }
}
