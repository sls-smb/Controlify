package com.controlify.mod.hud;

import com.controlify.mod.ControlifyClient;
import com.controlify.mod.macro.SafetyDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ControlifyHud {

    public void render(GuiGraphics graphics, Minecraft client) {
        SafetyDetector detector = ControlifyClient.INSTANCE.getSafetyDetector();
        if (!detector.isStopActive()) return;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        Component stopText = Component.literal("STOP");
        int textWidth = client.font.width(stopText);

        long remaining = detector.getStopUntil() - System.currentTimeMillis();
        float alpha = Math.min(1.0f, remaining / 500f);
        int a = (int) (alpha * 255);
        int color = (a << 24) | 0xFF0000;

        int x = (screenWidth - textWidth * 4) / 2;
        int y = screenHeight / 2 - 10;

        graphics.pose().push();
        graphics.pose().scale(4f, 4f);
        graphics.drawString(client.font, stopText, x / 4, y / 4, color, true);
        graphics.pose().pop();
    }
}
