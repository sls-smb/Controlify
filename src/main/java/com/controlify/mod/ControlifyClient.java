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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ControlifyClient implements ClientModInitializer {

    public static ControlifyClient INSTANCE;

    private final ControlifyConfig config = new ControlifyConfig();
    private final CombatMacro macro = new CombatMacro();
    private final SafetyDetector safetyDetector = new SafetyDetector();
    private final ControlifyHud hud = new ControlifyHud();

    private KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;

        // Register keybinding (default: X)
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.controlify.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "Controlify"
        ));

        // Main tick loop
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);

        // HUD overlay
        HudRenderCallback.EVENT.register((context, tickDelta) ->
                hud.render(context, MinecraftClient.getInstance()));
    }

    private void onEndTick(MinecraftClient client) {
        if (client.player == null) return;

        // Safety checks run every tick regardless of macro state
        safetyDetector.tick(client);

        // GUI open key
        while (openGuiKey.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new ControlifyScreen());
            }
        }

        // Combat macro tick
        if (config.isMacroEnabled() && !safetyDetector.isStopActive()) {
            // Don't run the macro while a GUI is open
            if (client.currentScreen == null) {
                macro.tick(client, config);
            }
        }
    }

    public ControlifyConfig getConfig() { return config; }
    public CombatMacro getMacro() { return macro; }
    public SafetyDetector getSafetyDetector() { return safetyDetector; }
}
