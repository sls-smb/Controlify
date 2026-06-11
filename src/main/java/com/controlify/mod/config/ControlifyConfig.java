package com.controlify.mod.config;

public class ControlifyConfig {

    private boolean macroEnabled = false;
    // delay range in seconds between clicks
    private float minDelay = 1.3f;
    private float maxDelay = 1.8f;

    public boolean isMacroEnabled() { return macroEnabled; }
    public void setMacroEnabled(boolean macroEnabled) { this.macroEnabled = macroEnabled; }

    public float getMinDelay() { return minDelay; }
    public void setMinDelay(float minDelay) { this.minDelay = Math.max(0.1f, Math.min(minDelay, maxDelay)); }

    public float getMaxDelay() { return maxDelay; }
    public void setMaxDelay(float maxDelay) { this.maxDelay = Math.max(minDelay, Math.min(maxDelay, 5.0f)); }
}
