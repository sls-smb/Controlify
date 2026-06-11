package com.controlify.mod.hud;

import com.controlify.mod.ControlifyClient;
import com.controlify.mod.macro.SafetyDetector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Draws the STOP overlay when the safety detector is active.
 */
public class ControlifyHud {

    public void render(DrawContext context, MinecraftClient client) {
        SafetyDetector detector = ControlifyClient.INSTANCE.getSafetyDetector();
        if (!detector.isStopActive()) return;

        TextRenderer font = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        Text stopText = Text.literal("STOP");
        int textWidth = font.getWidth(stopText);

        // Fade alpha based on remaining time
        long remaining = detector.getStopUntil() - System.currentTimeMillis();
        float alpha = Math.min(1.0f, remaining / 500f); // fade out last 500ms
        int a = (int) (alpha * 255);
        int color = (a << 24) | 0xFF0000; // red with dynamic alpha

        int x = (screenWidth - textWidth * 4) / 2;
        int y = screenHeight / 2 - 10;

        context.getMatrices().push();
        context.getMatrices().scale(4f, 4f, 1f);
        context.drawText(font, stopText, x / 4, y / 4, color, true);
        context.getMatrices().pop();
    }
}
