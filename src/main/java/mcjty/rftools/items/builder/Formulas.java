package mcjty.rftools.items.builder;

import net.minecraft.util.math.BlockPos;

public class Formulas {

    static final IFormula FORMULA_CUSTOM = new IFormula() {
        @Override
        public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset) {

        }

        @Override
        public int isInside(int x, int y, int z) {
            return 0;
        }

        @Override
        public boolean isCustom() {
            return true;
        }
    };

    static final IFormula FORMULA_TORUS = new IFormula() {
        private float smallRadius;
        private float bigRadius;
        private float centerx;
        private float centery;
        private float centerz;

        @Override
        public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset) {
            int dx = dimension.getX();
            int dy = dimension.getY();
            int dz = dimension.getZ();
            smallRadius = (dy-2)/2.0f;
            bigRadius = (dx-2)/2.0f - smallRadius;

            int xCoord = thisCoord.getX();
            int yCoord = thisCoord.getY();
            int zCoord = thisCoord.getZ();
            centerx = xCoord + offset.getX() + ((dx % 2 != 0) ? 0.0f : -.5f);
            centery = yCoord + offset.getY() + ((dy % 2 != 0) ? 0.0f : -.5f);
            centerz = zCoord + offset.getZ() + ((dz % 2 != 0) ? 0.0f : -.5f);
        }

        @Override
        public int isInside(int x, int y, int z) {
            double rr = bigRadius - Math.sqrt((x - centerx)*(x - centerx) + (z - centerz)*(z - centerz));
            double f = rr*rr + (y - centery) * (y - centery) - smallRadius * smallRadius;
            if (f < 0) {
                return 1;
            } else {
                return 0;
            }
        }
    };


    static final IFormula FORMULA_HEART = new IFormula() {
        private float centerx;
        private float centery;
        private float centerz;
        private int dx;
        private int dy;
        private int dz;

        @Override
        public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset) {
            dx = dimension.getX();
            dy = dimension.getY();
            dz = dimension.getZ();
            int xCoord = thisCoord.getX();
            int yCoord = thisCoord.getY();
            int zCoord = thisCoord.getZ();
            centerx = xCoord + offset.getX() + ((dx % 2 != 0) ? 0.0f : -.5f);
            centery = yCoord + offset.getY() + ((dy % 2 != 0) ? 0.0f : -.5f);
            centerz = zCoord + offset.getZ() + ((dz % 2 != 0) ? 0.0f : -.5f);
        }

        @Override
        public int isInside(int x, int y, int z) {
            double xx = (x-centerx) * 2.6 / dx + .1;
            double zz = (y-centery) * 2.4 / dy + .2;
            double yy = (z-centerz) * 1.6 / dz + .1;
            double f1 = Math.pow(xx * xx + (9.0/4.0) * yy * yy + zz * zz - 1, 3.0);
            double f2 = xx * xx * zz* zz * zz;
            double f3 = (9.0 / 80.0) * yy * yy * zz * zz * zz;
            double f = f1 - f2 - f3;
            if (f < 0) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    static final IFormula FORMULA_SPHERE = new IFormula() {
        private float centerx;
        private float centery;
        private float centerz;
        private float dx2;
        private float dy2;
        private float dz2;
        private int davg;

        @Override
        public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset) {
            int dx = dimension.getX();
            int dy = dimension.getY();
            int dz = dimension.getZ();
            int xCoord = thisCoord.getX();
            int yCoord = thisCoord.getY();
            int zCoord = thisCoord.getZ();
            centerx = xCoord + offset.getX() + ((dx % 2 != 0) ? 0.0f : -.5f);
            centery = yCoord + offset.getY() + ((dy % 2 != 0) ? 0.0f : -.5f);
            centerz = zCoord + offset.getZ() + ((dz % 2 != 0) ? 0.0f : -.5f);

            float factor = 1.8f;
            dx2 = dx == 0 ? .5f : ((dx + factor) * (dx + factor)) / 4.0f;
            dy2 = dy == 0 ? .5f : ((dy + factor) * (dy + factor)) / 4.0f;
            dz2 = dz == 0 ? .5f : ((dz + factor) * (dz + factor)) / 4.0f;
            davg = (int) ((dx + dy + dz + factor * 3) / 3);
        }

        @Override
        public int isInside(int x, int y, int z) {
            double distance = Math.sqrt(squaredDistance3D(centerx, centery, centerz, x, y, z, dx2, dy2, dz2));
            return ((int) (distance * (davg / 2 + 1))) <= (davg / 2 - 1) ? 1 : 0;
        }
    };

    static final IFormula FORMULA_BOX = new IFormula() {
        private int x1;
        private int y1;
        private int z1;
        private int x2;
        private int y2;
        private int z2;

        @Override
        public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset) {
            int dx = dimension.getX();
            int dy = dimension.getY();
            int dz = dimension.getZ();
            int xCoord = thisCoord.getX();
            int yCoord = thisCoord.getY();
            int zCoord = thisCoord.getZ();
            BlockPos tl = new BlockPos(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());
            x1 = tl.getX();
            y1 = tl.getY();
            z1 = tl.getZ();
            x2 = x1 + dx;
            y2 = y1 + dy;
            z2 = z1 + dz;
        }

        @Override
        public int isInside(int x, int y, int z) {
            return (x >= x1 && x < x2 && y >= y1 && y < y2 && z >= z1 && z < z2) ? 1 : 0;
        }
    };

    static final IFormula FORMULA_CAPPED_CYLINDER = new IFormula() {
        private float centerx;
        private float centerz;
        private float dx2;
        private float dz2;
        private int davg;
        private int y1;
        private int y2;

        @Override
        public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset) {
            int dx = dimension.getX();
            int dy = dimension.getY();
            int dz = dimension.getZ();
            int xCoord = thisCoord.getX();
            int yCoord = thisCoord.getY();
            int zCoord = thisCoord.getZ();
            centerx = xCoord + offset.getX() + ((dx % 2 != 0) ? 0.0f : -.5f);
            centerz = zCoord + offset.getZ() + ((dz % 2 != 0) ? 0.0f : -.5f);

            float factor = 1.7f;
            dx2 = dx == 0 ? .5f : ((dx + factor) * (dx + factor)) / 4.0f;
            dz2 = dz == 0 ? .5f : ((dz + factor) * (dz + factor)) / 4.0f;
            davg = (int) ((dx + dz + factor * 2) / 2);

            y1 = yCoord - dy/2 + offset.getY();
            y2 = y1 + dy;
        }

        @Override
        public int isInside(int x, int y, int z) {
             if (y < y1 || y >= y2) {
                 return 0;
             }
            double distance = Math.sqrt(squaredDistance2D(centerx, centerz, x, z, dx2, dz2));
            return ((int) (distance * (davg / 2 + 1))) <= (davg / 2 - 1) ? 1 : 0;
        }
    };

    static final IFormula FORMULA_CYLINDER = new IFormula() {
        private float centerx;
        private float centerz;
        private float dx2;
        private float dz2;
        private int davg;

        @Override
        public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset) {
            int dx = dimension.getX();
            int dy = dimension.getY();
            int dz = dimension.getZ();
            int xCoord = thisCoord.getX();
            int yCoord = thisCoord.getY();
            int zCoord = thisCoord.getZ();
            centerx = xCoord + offset.getX() + ((dx % 2 != 0) ? 0.0f : -.5f);
            centerz = zCoord + offset.getZ() + ((dz % 2 != 0) ? 0.0f : -.5f);

            float factor = 1.7f;
            dx2 = dx == 0 ? .5f : ((dx + factor) * (dx + factor)) / 4.0f;
            dz2 = dz == 0 ? .5f : ((dz + factor) * (dz + factor)) / 4.0f;
            davg = (int) ((dx + dz + factor * 2) / 2);
        }

        @Override
        public int isInside(int x, int y, int z) {
            double distance = Math.sqrt(squaredDistance2D(centerx, centerz, x, z, dx2, dz2));
            return ((int) (distance * (davg / 2 + 1))) <= (davg / 2 - 1) ? 1 : 0;
        }
    };

    static final IFormula FORMULA_PRISM = new IFormula() {
        private int x1;
        private int y1;
        private int z1;
        private int x2;
        private int y2;
        private int z2;

        @Override
        public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset) {
            int dx = dimension.getX();
            int dy = dimension.getY();
            int dz = dimension.getZ();
            int xCoord = thisCoord.getX();
            int yCoord = thisCoord.getY();
            int zCoord = thisCoord.getZ();
            BlockPos tl = new BlockPos(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());
            x1 = tl.getX();
            y1 = tl.getY();
            z1 = tl.getZ();
            x2 = x1 + dx;
            y2 = y1 + dy;
            z2 = z1 + dz;
        }

        @Override
        public int isInside(int x, int y, int z) {
            if (y < y1 || y >= y2) {
                return 0;
            }
            int dy = y-y1;
            return (x >= x1+dy && x < x2-dy && z >= z1+dy && z < z2-dy) ? 1 : 0;
        }
    };

    private static float squaredDistance3D(float cx, float cy, float cz, float x1, float y1, float z1, float dx2, float dy2, float dz2) {
        return (x1-cx) * (x1-cx) / dx2 + (y1-cy) * (y1-cy) / dy2 + (z1-cz) * (z1-cz) / dz2;
    }

    private static float squaredDistance2D(float cx, float cz, float x1, float z1, float dx2, float dz2) {
        return (x1-cx) * (x1-cx) / dx2 + (z1-cz) * (z1-cz) / dz2;
    }

}
