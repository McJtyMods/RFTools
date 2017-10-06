package mcjty.rftools.shapes;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Extra transient data that can be added to a scan (by the Locator for example)
 */
public class ScanExtraData {

    private final List<BlockPos> beacons = new ArrayList<>();

    public void clear() {
        beacons.clear();
    }

    public void addBeacon(BlockPos beacon) {
        beacons.add(beacon);
    }

    public List<BlockPos> getBeacons() {
        return beacons;
    }
}
