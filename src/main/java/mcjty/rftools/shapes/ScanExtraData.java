package mcjty.rftools.shapes;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Extra transient data that can be added to a scan (by the Locator for example)
 */
public class ScanExtraData {

    private final List<Beacon> beacons = new ArrayList<>();
    private long birthTime;

    public ScanExtraData() {
        this.birthTime = System.currentTimeMillis();
    }

    public void clear() {
        beacons.clear();
    }

    public void addBeacon(BlockPos beacon, BeaconType type) {
        beacons.add(new Beacon(beacon, type));
    }

    public void touch() {
        birthTime = System.currentTimeMillis();
    }

    public long getBirthTime() {
        return birthTime;
    }

    public List<Beacon> getBeacons() {
        return beacons;
    }

    public static class Beacon {
        private final BlockPos pos;
        private final BeaconType type;

        public Beacon(BlockPos pos, BeaconType type) {
            this.pos = pos;
            this.type = type;
        }

        public BlockPos getPos() {
            return pos;
        }

        public BeaconType getType() {
            return type;
        }
    }
}
