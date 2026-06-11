package com.controlify.mod.gui;

import com.controlify.mod.ControlifyClient;
import com.controlify.mod.config.ControlifyConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class ControlifyScreen extends Screen {

    private static final int PANEL_W = 300;
    private static final int PANEL_H = 220;
    private static final int BG_COLOR = 0xE0101820;
    private static final int ACCENT = 0xFF00D4FF;
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int ON_COLOR = 0xFF00FF88;
    private static final int OFF_COLOR = 0xFFFF4444;

    private final ControlifyConfig config;
    private ButtonWidget toggleButton;
    private DelaySlider minDelaySlider;
    private DelaySlider maxDelaySlider;

    public ControlifyScreen() {
        super(Text.literal("Controlify"));
        this.config = ControlifyClient.INSTANCE.getConfig();
    }

    @Override
    protected void init() {
        int panelX = (width - PANEL_W) / 2;
        int panelY = (height - PANEL_H) / 2;

        // Toggle button
        toggleButton = ButtonWidget.builder(
                getToggleText(),
                btn -> {
                    config.setMacroEnabled(!config.isMacroEnabled());
                    btn.setMessage(getToggleText());
                }
        ).dimensions(panelX + 20, panelY + 60, 260, 28).build();
        addDrawableChild(toggleButton);

        // Min delay slider
        minDelaySlider = new DelaySlider(
                panelX + 20, panelY + 110, 120, 24,
                "Min Delay", config.getMinDelay(), 0.1f, 5.0f
        ) {
            @Override protected void onValueChange(float val) { config.setMinDelay(val); }
        };
        addDrawableChild(minDelaySlider);

        // Max delay slider
        maxDelaySlider = new DelaySlider(
                panelX + 160, panelY + 110, 120, 24,
                "Max Delay", config.getMaxDelay(), 0.1f, 5.0f
        ) {
            @Override protected void onValueChange(float val) { config.setMaxDelay(val); }
        };
        addDrawableChild(maxDelaySlider);

        // Close button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Close"),
                btn -> close()
        ).dimensions(panelX + 90, panelY + 170, 120, 24).build());
    }

    private Text getToggleText() {
        if (config.isMacroEnabled()) {
            return Text.literal("Combat Macro: ").append(Text.literal("ON").withColor(ON_COLOR));
        } else {
            return Text.literal("Combat Macro: ").append(Text.literal("OFF").withColor(OFF_COLOR));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dim background
        renderBackground(context, mouseX, mouseY, delta);

        int panelX = (width - PANEL_W) / 2;
        int panelY = (height - PANEL_H) / 2;

        // Panel background
        context.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, BG_COLOR);
        // Top accent bar
        context.fill(panelX, panelY, panelX + PANEL_W, panelY + 4, ACCENT);
        // Bottom accent bar
        context.fill(panelX, panelY + PANEL_H - 4, panelX + PANEL_W, panelY + PANEL_H, ACCENT);

        // Title
        context.drawCenteredTextWithShadow(textRenderer, "Controlify", width / 2, panelY + 14, ACCENT);

        // Section label
        context.drawTextWithShadow(textRenderer,
                Text.literal("Click Delay (seconds)"), panelX + 20, panelY + 96, TEXT_COLOR);

        // Delay values under sliders
        String minVal = String.format("%.2f s", config.getMinDelay());
        String maxVal = String.format("%.2f s", config.getMaxDelay());
        context.drawCenteredTextWithShadow(textRenderer, minVal, panelX + 80, panelY + 138, TEXT_COLOR);
        context.drawCenteredTextWithShadow(textRenderer, maxVal, panelX + 220, panelY + 138, TEXT_COLOR);

        // Live status indicator
        boolean targeting = ControlifyClient.INSTANCE.getMacro()
                .isTargetingMob(client);
        String status = config.isMacroEnabled()
                ? (targeting ? "Targeting mob" : "Idle — no mob in sight")
                : "Macro disabled";
        int statusColor = config.isMacroEnabled() ? (targeting ? ON_COLOR : 0xFFFFAA00) : OFF_COLOR;
        context.drawCenteredTextWithShadow(textRenderer, status, width / 2, panelY + 155, statusColor);

        // Refresh toggle button label each frame so it stays in sync
        toggleButton.setMessage(getToggleText());

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }

    // ------------------------------------------------------------------
    // Inner slider class
    // ------------------------------------------------------------------
    abstract static class DelaySlider extends SliderWidget {
        private final String label;
        private final float min;
        private final float range;

        DelaySlider(int x, int y, int width, int height,
                    String label, float initial, float min, float max) {
            super(x, y, width, height, Text.empty(), (initial - min) / (max - min));
            this.label = label;
            this.min = min;
            this.range = max - min;
            updateMessage();
        }

        float getValue() {
            return min + (float) value * range;
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal(label + ": " + String.format("%.2f", getValue())));
        }

        @Override
        protected void applyValue() {
            onValueChange(getValue());
        }

        protected abstract void onValueChange(float val);
    }
}
