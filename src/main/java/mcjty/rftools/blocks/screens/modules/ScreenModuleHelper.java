package mcjty.rftools.blocks.screens.modules;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IModuleData;

public class ScreenModuleHelper {
    private boolean showdiff = false;
    private long prevMillis = 0;
    private long prevContents = 0;
    private long lastPerTick = 0;

    public static class ModuleDataContents implements IModuleData {

        public static final String ID = RFTools.MODID + ":contents";

        private final long contents;
        private final long maxContents;
        private final long lastPerTick;

        @Override
        public String getId() {
            return ID;
        }

        public ModuleDataContents(long contents, long maxContents, long lastPerTick) {
            this.contents = contents;
            this.maxContents = maxContents;
            this.lastPerTick = lastPerTick;
        }

        public ModuleDataContents(ByteBuf buf) {
            contents = buf.readLong();
            maxContents = buf.readLong();
            lastPerTick = buf.readLong();
        }

        public long getContents() {
            return contents;
        }

        public long getMaxContents() {
            return maxContents;
        }

        public long getLastPerTick() {
            return lastPerTick;
        }

        @Override
        public void writeToBuf(ByteBuf buf) {
            buf.writeLong(contents);
            buf.writeLong(maxContents);
            buf.writeLong(lastPerTick);
        }
    }


    public ScreenModuleHelper.ModuleDataContents getContentsValue(long millis, long contents, long maxContents) {
        if (showdiff) {
            if (prevMillis == 0 || millis <= prevMillis + 100) {        // <= prevMillis + 100 to make sure we show last value if the timing is too short
                prevMillis = millis;
                prevContents = contents;
                return new ModuleDataContents(contents, maxContents, lastPerTick);
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
                return new ModuleDataContents(contents, maxContents, lastPerTick);
            }
        } else {
            return new ModuleDataContents(contents, maxContents, 0L);
        }
    }

    public void setShowdiff(boolean showdiff) {
        this.showdiff = showdiff;
    }
}
