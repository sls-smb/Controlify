package com.controlify.mod.gui;

import com.controlify.mod.ControlifyClient;
import com.controlify.mod.config.ControlifyConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ControlifyScreen extends Screen {

    private static final int PANEL_W = 300;
    private static final int PANEL_H = 220;
    private static final int BG_COLOR = 0xE0101820;
    private static final int ACCENT   = 0xFF00D4FF;
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int ON_COLOR  = 0xFF00FF88;
    private static final int OFF_COLOR = 0xFFFF4444;

    private final ControlifyConfig config;
    private Button toggleButton;

    public ControlifyScreen() {
        super(Component.literal("Controlify"));
        this.config = ControlifyClient.INSTANCE.getConfig();
    }

    @Override
    protected void init() {
        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2;

        toggleButton = Button.builder(getToggleText(), btn -> {
            config.setMacroEnabled(!config.isMacroEnabled());
            btn.setMessage(getToggleText());
        }).bounds(px + 20, py + 60, 260, 28).build();
        addRenderableWidget(toggleButton);

        addRenderableWidget(new DelaySlider(px + 20, py + 110, 120, 24, "Min Delay", config.getMinDelay()) {
            @Override protected void onValueChange(float val) { config.setMinDelay(val); }
        });

        addRenderableWidget(new DelaySlider(px + 160, py + 110, 120, 24, "Max Delay", config.getMaxDelay()) {
            @Override protected void onValueChange(float val) { config.setMaxDelay(val); }
        });

        addRenderableWidget(Button.builder(Component.literal("Close"), btn -> onClose())
                .bounds(px + 90, py + 170, 120, 24).build());
    }

    private Component getToggleText() {
        return config.isMacroEnabled()
                ? Component.literal("Combat Macro: ON").withStyle(s -> s.withColor(ON_COLOR))
                : Component.literal("Combat Macro: OFF").withStyle(s -> s.withColor(OFF_COLOR));
    }

    /**
     * Draw the blur + our dark panel here so it happens exactly once per frame,
     * before widgets are rendered by super.render().
     */
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(graphics, mouseX, mouseY, delta); // blur — called once

        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2;

        graphics.fill(px, py, px + PANEL_W, py + PANEL_H, BG_COLOR);
        graphics.fill(px, py, px + PANEL_W, py + 4, ACCENT);
        graphics.fill(px, py + PANEL_H - 4, px + PANEL_W, py + PANEL_H, ACCENT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // super.render() calls renderBackground() then draws all widgets
        super.render(graphics, mouseX, mouseY, delta);

        // Text labels drawn on top of widgets
        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2;

        graphics.drawCenteredString(font, "Controlify", width / 2, py + 14, ACCENT);
        graphics.drawString(font, "Click Delay (seconds)", px + 20, py + 96, TEXT_COLOR);

        String minVal = String.format("%.2f s", config.getMinDelay());
        String maxVal = String.format("%.2f s", config.getMaxDelay());
        graphics.drawCenteredString(font, minVal, px + 80, py + 138, TEXT_COLOR);
        graphics.drawCenteredString(font, maxVal, px + 220, py + 138, TEXT_COLOR);

        boolean targeting = ControlifyClient.INSTANCE.getMacro().isTargetingMob(minecraft);
        String status = config.isMacroEnabled()
                ? (targeting ? "Targeting mob" : "Idle — no mob in sight")
                : "Macro disabled";
        int statusColor = config.isMacroEnabled() ? (targeting ? ON_COLOR : 0xFFFFAA00) : OFF_COLOR;
        graphics.drawCenteredString(font, status, width / 2, py + 155, statusColor);

        toggleButton.setMessage(getToggleText());
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ------------------------------------------------------------------
    // Slider
    // ------------------------------------------------------------------
    abstract static class DelaySlider extends AbstractSliderButton {
        private static final float MIN = 0.1f;
        private static final float MAX = 5.0f;
        private final String label;

        DelaySlider(int x, int y, int w, int h, String label, float initial) {
            super(x, y, w, h, Component.empty(), (initial - MIN) / (MAX - MIN));
            this.label = label;
            updateMessage();
        }

        float getValue() { return MIN + (float) value * (MAX - MIN); }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(label + ": " + String.format("%.2f", getValue())));
        }

        @Override
        protected void applyValue() { onValueChange(getValue()); }

        protected abstract void onValueChange(float val);
    }
}
