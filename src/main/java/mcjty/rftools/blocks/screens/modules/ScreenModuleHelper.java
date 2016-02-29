package mcjty.rftools.blocks.screens.modules;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.data.IModuleDataContents;

public class ScreenModuleHelper {
    public static final double SMOOTHING = 0.5;
    private boolean showdiff = false;
    private long prevMillis = 0;
    private long prevContents = 0;
    private long lastPerTick = 0;

    public static class ModuleDataContents implements IModuleDataContents {

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

        @Override
        public long getContents() {
            return contents;
        }

        @Override
        public long getMaxContents() {
            return maxContents;
        }

        @Override
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


    public IModuleDataContents getContentsValue(long millis, long contents, long maxContents) {
        if (showdiff) {
            if (prevMillis == 0) {
                prevMillis = millis;
                prevContents = contents;
                return new ModuleDataContents(contents, maxContents, lastPerTick);
            } else {
                if (millis > prevMillis + 500) {
                    long diffEnergy = contents - prevContents;
                    long diff = millis - prevMillis;
                    int ticks = (int) (diff * 20 / 1000);
                    if (ticks == 0) {
                        ticks = 1;
                    }

                    long measurement = diffEnergy / ticks;
                    lastPerTick = (long) ((lastPerTick * SMOOTHING) + (measurement * (1.0 - SMOOTHING)));

                    prevMillis = millis;
                    prevContents = contents;
                }
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
