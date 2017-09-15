package mcjty.rftools.items.builder;

import mcjty.rftools.blocks.shield.ShieldConfiguration;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class Formulas {

    public static class Bounds {
        private BlockPos p1;
        private BlockPos p2;
        private BlockPos offset;

        public Bounds(BlockPos p1, BlockPos p2, BlockPos offset) {
            this.p1 = p1;
            this.p2 = p2;
            this.offset = offset;
        }

        public BlockPos getP1() {
            return p1;
        }

        public BlockPos getP2() {
            return p2;
        }

        public BlockPos getOffset() {
            return offset;
        }

        public boolean in(BlockPos p) {
            int x = p.getX();
            int y = p.getY();
            int z = p.getZ();
            return in(x, y, z);
        }

        public boolean in(int x, int y, int z) {
            return x >= p1.getX() && x < p2.getX() && y >= p1.getY() && y < p2.getY() && z >= p1.getZ() && z < p2.getZ();
        }
    }


    static final IFormulaFactory FORMULA_CUSTOM = new IFormulaFactory() {
        @Override
        public IFormula createFormula() {
            return new IFormula() {
                private BlockPos thisCoordx;
                private BlockPos dimension;
                private BlockPos offset;
                private List<IFormula> formulas = new ArrayList<>();
                private List<Bounds> bounds = new ArrayList<>();
                private List<ShapeModifier> modifiers = new ArrayList<>();

                @Override
                public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset, NBTTagCompound card) {
                    this.thisCoordx = thisCoord;
                    this.dimension = dimension;
                    this.offset = offset;

                    if (card == null) {
                        return;
                    }
                    NBTTagList children = card.getTagList("children", Constants.NBT.TAG_COMPOUND);
                    for (int i = 0 ; i < children.tagCount() ; i++) {
                        NBTTagCompound childTag = children.getCompoundTagAt(i);
                        IFormula formula = ShapeCardItem.createCorrectFormula(childTag);

                        String op = childTag.getString("mod_op");
                        ShapeOperation operation = ShapeOperation.getByName(op);
                        boolean flip = childTag.getBoolean("mod_flipy");
                        String rot = childTag.getString("mod_rot");
                        ShapeRotation rotation = ShapeRotation.getByName(rot);
                        modifiers.add(new ShapeModifier(operation, flip, rotation));

                        BlockPos dim = ShapeCardItem.getClampedDimension(childTag, ShieldConfiguration.maxShieldDimension);
                        BlockPos off = ShapeCardItem.getClampedOffset(childTag, ShieldConfiguration.maxShieldOffset);
                        BlockPos o = off.add(offset);
                        formula.setup(thisCoord, dim, o, childTag);
                        formulas.add(formula);

                        dim = rotation.transformDimension(dim);
                        BlockPos tl = new BlockPos(o.getX() - dim.getX()/2, o.getY() - dim.getY()/2, o.getZ() - dim.getZ()/2);
                        bounds.add(new Bounds(tl, tl.add(dim), o));
                    }
                }

                @Override
                public int isInside(int x, int y, int z) {
                    int ok = 0;
                    for (int i = 0 ; i < formulas.size() ; i++) {
                        IFormula formula = formulas.get(i);
                        Bounds bounds = this.bounds.get(i);
                        ShapeModifier modifier = modifiers.get(i);

                        int inside = 0;
                        if (bounds.in(x, y, z)) {
                            int tx = x;
                            int ty = y;
                            int tz = z;
                            BlockPos o = bounds.getOffset();
                            switch (modifier.getRotation()) {
                                default:
                                case NONE:
                                    break;
                                case X:
                                    tx = x;
                                    ty = (z-o.getZ()) + o.getY();
                                    tz = (y-o.getY()) + o.getZ();
                                    break;
                                case Y:
                                    tx = (z-o.getZ()) + o.getX();;
                                    ty = y;
                                    tz = (x-o.getX()) + o.getZ();
                                    break;
                                case Z:
                                    tx = (y-o.getY()) + o.getX();;
                                    ty = (x-o.getX()) + o.getY();
                                    tz = z;
                                    break;
                            }

                            if (modifier.isFlipY()) {
                                ty = o.getY() - (ty-o.getY());
                            }

                            inside = formula.isInside(tx, ty, tz);
                        }

                        switch (modifier.getOperation()) {
                            case UNION:
                                if (inside == 1) {
                                    ok = 1;
                                }
                                break;
                            case SUBTRACT:
                                if (inside == 1) {
                                    ok = 0;
                                }
                                break;
                            case INTERSECT:
                                if (inside == 1 && ok == 1) {
                                    ok = 1;
                                } else {
                                    ok = 0;
                                }
                                break;
                        }
                    }
                    return ok;
                }

                @Override
                public boolean isCustom() {
                    return true;
                }
            };
        }
    };

    static final IFormulaFactory FORMULA_TORUS = () -> new IFormula() {
        private float smallRadius;
        private float bigRadius;
        private float centerx;
        private float centery;
        private float centerz;

        @Override
        public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset, NBTTagCompound card) {
            int dx = dimension.getX();
            int dy = dimension.getY();
            int dz = dimension.getZ();
            smallRadius = (dy - 2) / 2.0f;
            bigRadius = (dx - 2) / 2.0f - smallRadius;

            int xCoord = thisCoord.getX();
            int yCoord = thisCoord.getY();
            int zCoord = thisCoord.getZ();
            centerx = xCoord + offset.getX() + ((dx % 2 != 0) ? 0.0f : -.5f);
            centery = yCoord + offset.getY() + ((dy % 2 != 0) ? 0.0f : -.5f);
            centerz = zCoord + offset.getZ() + ((dz % 2 != 0) ? 0.0f : -.5f);
        }

        @Override
        public int isInside(int x, int y, int z) {
            double rr = bigRadius - Math.sqrt((x - centerx) * (x - centerx) + (z - centerz) * (z - centerz));
            double f = rr * rr + (y - centery) * (y - centery) - smallRadius * smallRadius;
            if (f < 0) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    static final IFormulaFactory FORMULA_HEART = () -> new IFormula() {
        private float centerx;
        private float centery;
        private float centerz;
        private int dx;
        private int dy;
        private int dz;

        @Override
        public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset, NBTTagCompound card) {
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
            double xx = (x - centerx) * 2.6 / dx + .1;
            double zz = (y - centery) * 2.4 / dy + .2;
            double yy = (z - centerz) * 1.6 / dz + .1;
            double f1 = Math.pow(xx * xx + (9.0 / 4.0) * yy * yy + zz * zz - 1, 3.0);
            double f2 = xx * xx * zz * zz * zz;
            double f3 = (9.0 / 80.0) * yy * yy * zz * zz * zz;
            double f = f1 - f2 - f3;
            if (f < 0) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    static final IFormulaFactory FORMULA_SPHERE = () -> new IFormula() {
        private float centerx;
        private float centery;
        private float centerz;
        private float dx2;
        private float dy2;
        private float dz2;
        private int davg;

        @Override
        public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset, NBTTagCompound card) {
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

    static final IFormulaFactory FORMULA_TOPDOME = () -> new IFormula() {
        private float centerx;
        private float centery;
        private float centerz;
        private float dx2;
        private float dy2;
        private float dz2;
        private int davg;

        @Override
        public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset, NBTTagCompound card) {
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
            if (y < centery) {
                return 0;
            }
            double distance = Math.sqrt(squaredDistance3D(centerx, centery, centerz, x, y, z, dx2, dy2, dz2));
            return ((int) (distance * (davg / 2 + 1))) <= (davg / 2 - 1) ? 1 : 0;
        }
    };

    static final IFormulaFactory FORMULA_BOTTOMDOME = () -> new IFormula() {
        private float centerx;
        private float centery;
        private float centerz;
        private float dx2;
        private float dy2;
        private float dz2;
        private int davg;

        @Override
        public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset, NBTTagCompound card) {
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
            if (y > centery) {
                return 0;
            }
            double distance = Math.sqrt(squaredDistance3D(centerx, centery, centerz, x, y, z, dx2, dy2, dz2));
            return ((int) (distance * (davg / 2 + 1))) <= (davg / 2 - 1) ? 1 : 0;
        }
    };

    static final IFormulaFactory FORMULA_BOX = () -> new IFormula() {
        private int x1;
        private int y1;
        private int z1;
        private int x2;
        private int y2;
        private int z2;

        @Override
        public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset, NBTTagCompound card) {
            int dx = dimension.getX();
            int dy = dimension.getY();
            int dz = dimension.getZ();
            int xCoord = thisCoord.getX();
            int yCoord = thisCoord.getY();
            int zCoord = thisCoord.getZ();
            BlockPos tl = new BlockPos(xCoord - dx / 2 + offset.getX(), yCoord - dy / 2 + offset.getY(), zCoord - dz / 2 + offset.getZ());
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

        @Override
        public boolean isBorder(int x, int y, int z) {
            return (x == x1 || x == x2-1) || (y == y1 || y == y2-1) || (z == z1 || z == z2-1);
        }
    };

    static final IFormulaFactory FORMULA_CAPPED_CYLINDER = () -> new IFormula() {
        private float centerx;
        private float centerz;
        private float dx2;
        private float dz2;
        private int davg;
        private int y1;
        private int y2;

        @Override
        public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset, NBTTagCompound card) {
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

            y1 = yCoord - dy / 2 + offset.getY();
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

    static final IFormulaFactory FORMULA_CYLINDER = () -> new IFormula() {
        private float centerx;
        private float centerz;
        private float dx2;
        private float dz2;
        private int davg;

        @Override
        public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset, NBTTagCompound card) {
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

    static final IFormulaFactory FORMULA_PRISM = () -> new IFormula() {
        private int x1;
        private int y1;
        private int z1;
        private int x2;
        private int y2;
        private int z2;

        @Override
        public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset, NBTTagCompound card) {
            int dx = dimension.getX();
            int dy = dimension.getY();
            int dz = dimension.getZ();
            int xCoord = thisCoord.getX();
            int yCoord = thisCoord.getY();
            int zCoord = thisCoord.getZ();
            BlockPos tl = new BlockPos(xCoord - dx / 2 + offset.getX(), yCoord - dy / 2 + offset.getY(), zCoord - dz / 2 + offset.getZ());
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
            int dy = y - y1;
            return (x >= x1 + dy && x < x2 - dy && z >= z1 + dy && z < z2 - dy) ? 1 : 0;
        }
    };

    private static float squaredDistance3D(float cx, float cy, float cz, float x1, float y1, float z1, float dx2, float dy2, float dz2) {
        return (x1 - cx) * (x1 - cx) / dx2 + (y1 - cy) * (y1 - cy) / dy2 + (z1 - cz) * (z1 - cz) / dz2;
    }

    private static float squaredDistance2D(float cx, float cz, float x1, float z1, float dx2, float dz2) {
        return (x1 - cx) * (x1 - cx) / dx2 + (z1 - cz) * (z1 - cz) / dz2;
    }

}
