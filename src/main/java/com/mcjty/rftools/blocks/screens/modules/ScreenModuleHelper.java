package com.mcjty.rftools.blocks.screens.modules;

public class ScreenModuleHelper {
    private boolean showdiff = false;
    private long prevMillis = 0;
    private long prevContents = 0;

    public Object[] getContentsValue(long millis, long contents, long maxContents) {
        if (showdiff) {
            if (prevMillis == 0 || millis <= prevMillis) {
                prevMillis = millis;
                prevContents = contents;
                return new Object[] { contents, maxContents, 0L };
            } else {
                long diff = millis - prevMillis;
                int ticks = (int) (diff * 20 / 1000);
                if (ticks == 0) {
                    ticks = 1;
                }
                long diffEnergy = contents - prevContents;
                prevMillis = millis;
                prevContents = contents;
                return new Object[] { contents, maxContents, diffEnergy / ticks };
            }
        } else {
            return new Object[] { contents, maxContents, 0L };
        }
    }

    public void setShowdiff(boolean showdiff) {
        this.showdiff = showdiff;
    }
}
