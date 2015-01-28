package com.mcjty.rftools.blocks.screens.modules;

public class ScreenModuleHelper {
    private boolean showdiff = false;
    private long prevMillis = 0;
    private int prevContents = 0;

    public String[] getContentsValue(long millis, int contents, int maxContents) {
        if (showdiff) {
            if (prevMillis == 0 || millis <= prevMillis) {
                prevMillis = millis;
                prevContents = contents;
                return new String[] { String.valueOf(contents), String.valueOf(maxContents), "0" };
            } else {
                long diff = millis - prevMillis;
                int ticks = (int) (diff * 20 / 1000);
                if (ticks == 0) {
                    ticks = 1;
                }
                int diffEnergy = contents - prevContents;
                prevMillis = millis;
                prevContents = contents;
                return new String[] { String.valueOf(contents), String.valueOf(maxContents), String.valueOf(diffEnergy / ticks) };
            }
        } else {
            return new String[] { String.valueOf(contents), String.valueOf(maxContents), "0" };
        }
    }

    public void setShowdiff(boolean showdiff) {
        this.showdiff = showdiff;
    }
}
