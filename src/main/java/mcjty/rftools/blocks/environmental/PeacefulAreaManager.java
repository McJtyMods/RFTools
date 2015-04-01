package mcjty.rftools.blocks.environmental;

import mcjty.varia.GlobalCoordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeacefulAreaManager {
    private static final Map<GlobalCoordinate,PeacefulArea> areas = new HashMap<GlobalCoordinate, PeacefulArea>();

    public static void markArea(GlobalCoordinate coordinate, int radius, int miny, int maxy) {
        if (areas.containsKey(coordinate)) {
            areas.get(coordinate).touch().setArea(radius, miny, maxy);
        } else {
            PeacefulArea area = new PeacefulArea(radius, miny, maxy);
            areas.put(coordinate, area);
        }
    }

    public static boolean isPeaceful(GlobalCoordinate coordinate) {
        if (areas.isEmpty()) {
            return false;
        }
        List<GlobalCoordinate> toRemove = new ArrayList<GlobalCoordinate>();
        boolean peaceful = false;
        long curtime = System.currentTimeMillis() - 20;

        for (Map.Entry<GlobalCoordinate, PeacefulArea> entry : areas.entrySet()) {
            PeacefulArea area = entry.getValue();
            GlobalCoordinate entryCoordinate = entry.getKey();
            if (area.in(coordinate, entryCoordinate)) {
                peaceful = true;
            }
            if (area.getLastTouched() < curtime) {
                // Hasn't been touched for at least 20 ticks. Probably no longer valid.
                toRemove.add(entryCoordinate);
            }
        }

        for (GlobalCoordinate globalCoordinate : toRemove) {
            areas.remove(globalCoordinate);
        }

        return peaceful;
    }


    public static class PeacefulArea {
        private float sqradius;
        private int miny;
        private int maxy;
        private long lastTouched;

        public PeacefulArea(float radius, int miny, int maxy) {
            this.sqradius = radius * radius;
            this.miny = miny;
            this.maxy = maxy;
            touch();
        }

        public PeacefulArea setArea(float radius, int miny, int maxy) {
            this.sqradius = radius * radius;
            this.miny = miny;
            this.maxy = maxy;
            return this;
        }

        public long getLastTouched() {
            return lastTouched;
        }

        public PeacefulArea touch() {
            lastTouched = System.currentTimeMillis();
            return this;
        }

        public boolean in(GlobalCoordinate coordinate, GlobalCoordinate thisCoordinate) {
            if (coordinate.getDimension() != thisCoordinate.getDimension()) {
                return false;
            }
            double py = coordinate.getCoordinate().getY();
            if (py < miny || py > maxy) {
                return false;
            }

            double px = coordinate.getCoordinate().getX() - thisCoordinate.getCoordinate().getX();
            double pz = coordinate.getCoordinate().getZ() - thisCoordinate.getCoordinate().getZ();
            double sqdist = px * px + pz * pz;
            return sqdist < sqradius;
        }
    }
}
