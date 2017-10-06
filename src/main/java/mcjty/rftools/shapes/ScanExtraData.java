package mcjty.rftools.shapes;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Extra transient data that can be added to a scan (by the Locator for example)
 */
public class ScanExtraData {

    private final List<Beacon> beacons = new ArrayList<>();

    public void clear() {
        beacons.clear();
    }

    public void addBeacon(BlockPos beacon, float r, float g, float b) {
        beacons.add(new Beacon(beacon, r, g, b));
    }

    public List<Beacon> getBeacons() {
        return beacons;
    }

    public static class Beacon {
        private final BlockPos pos;
        private final float r;
        private final float g;
        private final float b;

        public Beacon(BlockPos pos, float r, float g, float b) {
            this.pos = pos;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public BlockPos getPos() {
            return pos;
        }

        public float getR() {
            return r;
        }

        public float getG() {
            return g;
        }

        public float getB() {
            return b;
        }
    }
}
