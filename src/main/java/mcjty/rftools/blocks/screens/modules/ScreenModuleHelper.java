package mcjty.rftools.blocks.screens.modules;

public class ScreenModuleHelper {
    private boolean showdiff = false;
    private long prevMillis = 0;
    private long prevContents = 0;
    private long lastPerTick = 0;

    public Object[] getContentsValue(long millis, long contents, long maxContents) {
        if (showdiff) {
            if (prevMillis == 0 || millis <= prevMillis + 100) {        // <= prevMillis + 100 to make sure we show last value if the timing is too short
                prevMillis = millis;
                prevContents = contents;
                return new Object[] { contents, maxContents, lastPerTick };
            } else {
                long diff = millis - prevMillis;
                int ticks = (int) (diff * 20 / 1000);
                if (ticks == 0) {
                    ticks = 1;
                }
                long diffEnergy = contents - prevContents;
                prevMillis = millis;
                prevContents = contents;
                lastPerTick = diffEnergy / ticks;
                return new Object[] { contents, maxContents, lastPerTick };
            }
        } else {
            return new Object[] { contents, maxContents, 0L };
        }
    }

    public void setShowdiff(boolean showdiff) {
        this.showdiff = showdiff;
    }
}
