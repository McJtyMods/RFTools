package mcjty.rftools.items.shapecard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.lib.varia.Coordinate;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.shield.ShieldConfiguration;
import mcjty.rftools.blocks.spaceprojector.BuilderTileEntity;
import mcjty.rftools.blocks.spaceprojector.SpaceProjectorConfiguration;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class ShapeCardItem extends Item {

    public static final int CARD_UNKNOWN = -2;          // Not known yet
    public static final int CARD_SPACE = -1;            // Not a shape card but a space card instead
    public static final int CARD_SHAPE = 0;
    public static final int CARD_VOID = 1;
    public static final int CARD_QUARRY = 2;
    public static final int CARD_QUARRY_SILK = 3;
    public static final int CARD_QUARRY_FORTUNE = 4;

    private final IIcon[] icons = new IIcon[5];

    public enum Shape {
        SHAPE_BOX(0, "Box"),
        SHAPE_TOPDOME(1, "Top Dome"),
        SHAPE_BOTTOMDOME(2, "Bottom Dome"),
        SHAPE_SPHERE(3, "Sphere"),
        SHAPE_CYLINDER(4, "Cylinder"),
        SHAPE_CAPPEDCYLINDER(5, "Capped Cylinder"),
        SHAPE_PRISM(6, "Prism"),
        SHAPE_TORUS(7, "Torus"),
        SHAPE_SOLIDBOX(100, "Solid Box"),
        SHAPE_SOLIDSPHERE(103, "Solid Sphere"),
        SHAPE_SOLIDCYLINDER(104, "Solid Cylinder"),
        SHAPE_SOLIDTORUS(107, "Solid Torus");


        private final int index;
        private final String description;

        private static Map<Integer,Shape> shapes;
        private static Map<String,Shape> shapesByDescription;

        static {
            shapesByDescription = new HashMap<String, Shape>();
            shapes = new HashMap<Integer, Shape>();
            for (Shape shape : values()) {
                shapes.put(shape.getIndex(), shape);
                shapesByDescription.put(shape.getDescription(), shape);
            }
        }


        Shape(int index, String description) {
            this.index = index;
            this.description = description;
        }

        public int getIndex() {
            return index;
        }

        public String getDescription() {
            return description;
        }

        public static Shape getShape(int index) {
            return shapes.get(index);
        }

        public static Shape getShape(String description) {
            return shapesByDescription.get(description);
        }
    }

    public ShapeCardItem() {
        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        Shape shape = getShape(itemStack);
        list.add(EnumChatFormatting.GREEN + "Shape " + shape.getDescription());
        list.add(EnumChatFormatting.GREEN + "Dimension " + getDimension(itemStack));
        list.add(EnumChatFormatting.GREEN + "Offset " + getOffset(itemStack));

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            int type = itemStack.getItemDamage();
            switch (type) {
                case CARD_VOID:
                    list.add(EnumChatFormatting.WHITE + "This item will cause the builder to void");
                    list.add(EnumChatFormatting.WHITE + "all blocks in the configured space.");
                    list.add(EnumChatFormatting.GREEN + "Base cost: " + (int)(SpaceProjectorConfiguration.builderRfPerQuarry * SpaceProjectorConfiguration.voidShapeCardFactor) + " RF/t per block");
                    list.add(EnumChatFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_SHAPE:
                    list.add(EnumChatFormatting.WHITE + "This item can be configured as a shape. You");
                    list.add(EnumChatFormatting.WHITE + "can then use it in the shield projector to make");
                    list.add(EnumChatFormatting.WHITE + "a shield of that shape or in the builder to");
                    list.add(EnumChatFormatting.WHITE + "actually build the shape");
                    list.add(EnumChatFormatting.GREEN + "Base cost: " + SpaceProjectorConfiguration.builderRfPerOperation + " RF/t per block");
                    list.add(EnumChatFormatting.GREEN + "(final cost depends on infusion level)");
                    break;
                case CARD_QUARRY_SILK:
                    list.add(EnumChatFormatting.WHITE + "This item will cause the builder to quarry");
                    list.add(EnumChatFormatting.WHITE + "all blocks in the configured space.");
                    list.add(EnumChatFormatting.WHITE + "Blocks are harvested with silk touch");
                    list.add(EnumChatFormatting.GREEN + "Base cost: " + (int)(SpaceProjectorConfiguration.builderRfPerQuarry * SpaceProjectorConfiguration.silkquarryShapeCardFactor) + " RF/t per block");
                    list.add(EnumChatFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_QUARRY_FORTUNE:
                    list.add(EnumChatFormatting.WHITE + "This item will cause the builder to quarry");
                    list.add(EnumChatFormatting.WHITE + "all blocks in the configured space.");
                    list.add(EnumChatFormatting.WHITE + "Blocks are harvested with fortune");
                    list.add(EnumChatFormatting.GREEN + "Base cost: " + (int)(SpaceProjectorConfiguration.builderRfPerQuarry * SpaceProjectorConfiguration.fortunequarryShapeCardFactor) + " RF/t per block");
                    list.add(EnumChatFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
                case CARD_QUARRY:
                    list.add(EnumChatFormatting.WHITE + "This item will cause the builder to quarry");
                    list.add(EnumChatFormatting.WHITE + "all blocks in the configured space");
                    list.add(EnumChatFormatting.GREEN + "Base cost: " + SpaceProjectorConfiguration.builderRfPerQuarry + " RF/t per block");
                    list.add(EnumChatFormatting.GREEN + "(final cost depends on infusion level and block hardness)");
                    break;
            }
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    public static boolean isQuarry(int type) {
        return type == CARD_QUARRY || type == CARD_QUARRY_SILK || type == CARD_QUARRY_FORTUNE;
    }

    /**
     * Return true if the card is a normal card (not a quarry or void card)
     * @param stack
     * @return
     */
    public static boolean isNormalShapeCard(ItemStack stack) {
        return stack.getItemDamage() == CARD_SHAPE;
    }

    public static Shape getShape(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return Shape.SHAPE_BOX;
        }
        int shape = tagCompound.getInteger("shape");
        Shape s = Shape.getShape(shape);
        if (s == null) {
            return Shape.SHAPE_BOX;
        }
        return s;
    }

    public static void setShape(ItemStack stack, Shape shape) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
        }
        tagCompound.setInteger("shape", shape.getIndex());
    }

    public static Coordinate getDimension(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return new Coordinate(5, 5, 5);
        }
        int dimX = tagCompound.getInteger("dimX");
        int dimY = tagCompound.getInteger("dimY");
        int dimZ = tagCompound.getInteger("dimZ");
        return new Coordinate(clampDimension(dimX), clampDimension(dimY), clampDimension(dimZ));
    }

    private static int clampDimension(int o) {
        if (o > ShieldConfiguration.maxShieldDimension) {
            o = ShieldConfiguration.maxShieldDimension;
        } else if (o < 0) {
            o = 0;
        }
        return o;
    }

    public static Coordinate getOffset(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return new Coordinate(0, 0, 0);
        }
        int offsetX = tagCompound.getInteger("offsetX");
        int offsetY = tagCompound.getInteger("offsetY");
        int offsetZ = tagCompound.getInteger("offsetZ");
        return new Coordinate(clampOffset(offsetX), clampOffset(offsetY), clampOffset(offsetZ));
    }

    private static int clampOffset(int o) {
        if (o < -ShieldConfiguration.maxShieldOffset) {
            o = -ShieldConfiguration.maxShieldOffset;
        } else if (o > ShieldConfiguration.maxShieldOffset) {
            o = ShieldConfiguration.maxShieldOffset;
        }
        return o;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_SHAPECARD, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
            return stack;
        }
        return stack;
    }

    public static Coordinate getMinCorner(Coordinate thisCoord, Coordinate dimension, Coordinate offset) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        return new Coordinate(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());
    }

    public static Coordinate getMaxCorner(Coordinate thisCoord, Coordinate dimension, Coordinate offset) {
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        Coordinate minCorner = getMinCorner(thisCoord, dimension, offset);
        return new Coordinate(minCorner.getX() + dx, minCorner.getY() + dy, minCorner.getZ() + dz);
    }

    public static int countBlocks(Shape shape, Coordinate dimension) {
        final int[] cnt = {0};
        Coordinate offset = new Coordinate(0, 128, 0);
        composeShape(shape, null, new Coordinate(0, 0, 0), dimension, offset, new AbstractCollection<Coordinate>() {
            @Override
            public Iterator<Coordinate> iterator() {
                return null;
            }

            @Override
            public boolean add(Coordinate coordinate) {
                cnt[0]++;
                return true;
            }

            @Override
            public int size() {
                return 0;
            }
        }, 1000000000, false);
        return cnt[0];
    }

    public static void composeShape(Shape shape, World worldObj, Coordinate thisCoord, Coordinate dimension, Coordinate offset, Collection<Coordinate> blocks, int maxSize, boolean forquarry) {
        switch (shape) {
            case SHAPE_BOX:
                composeBox(worldObj, thisCoord, dimension, offset, blocks, maxSize, false, forquarry);
                break;
            case SHAPE_SOLIDBOX:
                composeBox(worldObj, thisCoord, dimension, offset, blocks, maxSize, true, forquarry);
                break;
            case SHAPE_TOPDOME:
                composeSphere(worldObj, thisCoord, dimension, offset, blocks, maxSize, 1, false, forquarry);
                break;
            case SHAPE_BOTTOMDOME:
                composeSphere(worldObj, thisCoord, dimension, offset, blocks, maxSize, -1, false, forquarry);
                break;
            case SHAPE_SPHERE:
                composeSphere(worldObj, thisCoord, dimension, offset, blocks, maxSize, 0, false, forquarry);
                break;
            case SHAPE_SOLIDSPHERE:
                composeSphere(worldObj, thisCoord, dimension, offset, blocks, maxSize, 0, true, forquarry);
                break;
            case SHAPE_CYLINDER:
                composeCylinder(worldObj, thisCoord, dimension, offset, blocks, maxSize, false, false, forquarry);
                break;
            case SHAPE_SOLIDCYLINDER:
                composeCylinder(worldObj, thisCoord, dimension, offset, blocks, maxSize, false, true, forquarry);
                break;
            case SHAPE_CAPPEDCYLINDER:
                composeCylinder(worldObj, thisCoord, dimension, offset, blocks, maxSize, true, false, forquarry);
                break;
            case SHAPE_PRISM:
                composePrism(worldObj, thisCoord, dimension, offset, blocks, maxSize, forquarry);
                break;
            case SHAPE_TORUS:
                composeTorus(worldObj, thisCoord, dimension, offset, blocks, maxSize, false, forquarry);
                break;
            case SHAPE_SOLIDTORUS:
                composeTorus(worldObj, thisCoord, dimension, offset, blocks, maxSize, true, forquarry);
                break;
        }
    }

    private static void placeBlockIfPossible(World worldObj, Collection<Coordinate> blocks, int maxSize, int x, int y, int z, boolean forquarry) {
        if (worldObj == null) {
            blocks.add(new Coordinate(x, y, z));
            return;
        }
        if (forquarry) {
            if (worldObj.isAirBlock(x, y, z)) {
                return;
            }
            blocks.add(new Coordinate(x, y, z));
        } else {
            if (BuilderTileEntity.isEmptyOrReplacable(worldObj, x, y, z) && blocks.size() < maxSize - 1) {
                blocks.add(new Coordinate(x, y, z));
            }
        }
    }

    private static void composeSphere(World worldObj, Coordinate thisCoord, Coordinate dimension, Coordinate offset, Collection<Coordinate> blocks, int maxSize, int side, boolean solid, boolean forquarry) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        float centerx = xCoord + offset.getX() + 0.5f;
        float centery = yCoord + offset.getY() + 0.5f;
        float centerz = zCoord + offset.getZ() + 0.5f;
        Coordinate tl = new Coordinate(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());

        float dx2 = dx == 0 ? .5f : (dx * dx) / 4.0f;
        float dy2 = dy == 0 ? .5f : (dy * dy) / 4.0f;
        float dz2 = dz == 0 ? .5f : (dz * dz) / 4.0f;

        int davg = (dx + dy + dz) / 3;

        for (int ox = 0 ; ox < dx ; ox++) {
            int x = tl.getX() + ox;
            for (int oy = 0 ; oy < dy ; oy++) {
                int y = tl.getY() + oy;
                if (y >= 0 && y < 255) {
                    if (side == 0 || (side == 1 && y >= yCoord + offset.getY()) || (side == -1 && y <= yCoord + offset.getY())) {
                        for (int oz = 0; oz < dz; oz++) {
                            int z = tl.getZ() + oz;
                            if (isInside3D(centerx, centery, centerz, x, y, z, dx2, dy2, dz2, davg) == 1) {
                                int cnt;
                                if (solid) {
                                    cnt = 0;
                                } else {
                                    cnt = isInside3D(centerx, centery, centerz, x - 1, y, z, dx2, dy2, dz2, davg);
                                    cnt += isInside3D(centerx, centery, centerz, x + 1, y, z, dx2, dy2, dz2, davg);
                                    cnt += isInside3D(centerx, centery, centerz, x, y - 1, z, dx2, dy2, dz2, davg);
                                    cnt += isInside3D(centerx, centery, centerz, x, y + 1, z, dx2, dy2, dz2, davg);
                                    cnt += isInside3D(centerx, centery, centerz, x, y, z - 1, dx2, dy2, dz2, davg);
                                    cnt += isInside3D(centerx, centery, centerz, x, y, z + 1, dx2, dy2, dz2, davg);
                                }
                                if (cnt != 6) {
                                    placeBlockIfPossible(worldObj, blocks, maxSize, x, y, z, forquarry);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static float squaredDistance3D(float cx, float cy, float cz, float x1, float y1, float z1, float dx2, float dy2, float dz2) {
        return (x1-cx) * (x1-cx) / dx2 + (y1-cy) * (y1-cy) / dy2 + (z1-cz) * (z1-cz) / dz2;
    }

    private static float squaredDistance2D(float cx, float cz, float x1, float z1, float dx2, float dz2) {
        return (x1-cx) * (x1-cx) / dx2 + (z1-cz) * (z1-cz) / dz2;
    }

    private static int isInside2D(float centerx, float centerz, int x, int z, float dx2, float dz2, int davg) {
        double distance = Math.sqrt(squaredDistance2D(centerx, centerz, x, z, dx2, dz2));
        return ((int) (distance * (davg / 2 + 1))) <= (davg / 2 - 1) ? 1 : 0;
    }

    private static int isInside3D(float centerx, float centery, float centerz, int x, int y, int z, float dx2, float dy2, float dz2, int davg) {
        double distance = Math.sqrt(squaredDistance3D(centerx, centery, centerz, x, y, z, dx2, dy2, dz2));
        return ((int) (distance * (davg / 2 + 1))) <= (davg / 2 - 1) ? 1 : 0;
    }

    private static void composeCylinder(World worldObj, Coordinate thisCoord, Coordinate dimension, Coordinate offset, Collection<Coordinate> blocks, int maxSize, boolean capped, boolean solid, boolean forquarry) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        float centerx = xCoord + offset.getX() + 0.5f;
        float centerz = zCoord + offset.getZ() + 0.5f;
        Coordinate tl = new Coordinate(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());

        float dx2 = dx == 0 ? .5f : (dx * dx) / 4.0f;
        float dz2 = dz == 0 ? .5f : (dz * dz) / 4.0f;

        int davg = (dx + dz) / 2;

        for (int ox = 0 ; ox < dx ; ox++) {
            int x = tl.getX() + ox;
            for (int oy = 0 ; oy < dy ; oy++) {
                int y = tl.getY() + oy;
                if (y >= 0 && y < 255) {
                    for (int oz = 0; oz < dz; oz++) {
                        int z = tl.getZ() + oz;
                        if (isInside2D(centerx, centerz, x, z, dx2, dz2, davg) == 1) {
                            int cnt;
                            if (solid) {
                                cnt = 0;
                            } else {
                                cnt = isInside2D(centerx, centerz, x - 1, z, dx2, dz2, davg);
                                cnt += isInside2D(centerx, centerz, x + 1, z, dx2, dz2, davg);
                                cnt += isInside2D(centerx, centerz, x, z - 1, dx2, dz2, davg);
                                cnt += isInside2D(centerx, centerz, x, z + 1, dx2, dz2, davg);
                            }
                            if (cnt != 4 || (capped && (oy == 0 || oy == dy-1))) {
                                placeBlockIfPossible(worldObj, blocks, maxSize, x, y, z, forquarry);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void composeBox(World worldObj, Coordinate thisCoord, Coordinate dimension, Coordinate offset, Collection<Coordinate> blocks, int maxSize, boolean solid, boolean forquarry) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        Coordinate tl = new Coordinate(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());

        for (int ox = 0 ; ox < dx ; ox++) {
            int x = tl.getX() + ox;
            for (int oy = 0 ; oy < dy ; oy++) {
                int y = tl.getY() + oy;
                if (y >= 0 && y < 255) {
                    for (int oz = 0 ; oz < dz ; oz++) {
                        int z = tl.getZ() + oz;
                        if (solid || ox == 0 || oy == 0 || oz == 0 || ox == (dx - 1) || oy == (dy - 1) || oz == (dz - 1)) {
                            placeBlockIfPossible(worldObj, blocks, maxSize, x, y, z, forquarry);
                        }
                    }
                }
            }
        }
    }

    private static void composePrism(World worldObj, Coordinate thisCoord, Coordinate dimension, Coordinate offset, Collection<Coordinate> blocks, int maxSize, boolean forquarry) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        Coordinate tl = new Coordinate(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());

        for (int oy = 0 ; oy < dy ; oy++) {
            int y = tl.getY() + oy;
            if (y >= 0 && y < 255) {
                int yoffset = oy;
                for (int ox = yoffset ; ox < dx-yoffset ; ox++) {
                    int x = tl.getX() + ox;
                    for (int oz = yoffset ; oz < dz-yoffset ; oz++) {
                        int z = tl.getZ() + oz;
                        if (ox == yoffset || oy == 0 || oz == yoffset || ox == (dx - yoffset - 1) || oz == (dz - yoffset - 1)) {
                            placeBlockIfPossible(worldObj, blocks, maxSize, x, y, z, forquarry);
                        }
                    }
                }
            }
        }
    }

    private static int isInsideTorus(float centerx, float centery, float centerz, int x, int y, int z, float bigRadius, float smallRadius) {
        double rr = bigRadius - Math.sqrt((x-centerx)*(x-centerx) + (z-centerz)*(z-centerz));
        double f = rr*rr + (y-centery) * (y-centery) - smallRadius * smallRadius;
        if (f <= 0) {
            return 1;
        } else {
            return 0;
        }
    }

    private static void composeTorus(World worldObj, Coordinate thisCoord, Coordinate dimension, Coordinate offset, Collection<Coordinate> blocks, int maxSize, boolean solid, boolean forquarry) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        float centerx = xCoord + offset.getX();
        float centery = yCoord + offset.getY();
        float centerz = zCoord + offset.getZ();
        Coordinate tl = new Coordinate(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());

        float smallRadius = (dy-2)/2.0f;
        float bigRadius = (dx-2)/2.0f - smallRadius;

        for (int ox = 0 ; ox < dx ; ox++) {
            int x = tl.getX() + ox;
            for (int oy = 0 ; oy < dy ; oy++) {
                int y = tl.getY() + oy;
                if (y >= 0 && y < 255) {
                    for (int oz = 0; oz < dz; oz++) {
                        int z = tl.getZ() + oz;
                        if (isInsideTorus(centerx, centery, centerz, x, y, z, bigRadius, smallRadius) == 1) {
                            int cnt;
                            if (solid) {
                                cnt = 0;
                            } else {
                                cnt  = isInsideTorus(centerx, centery, centerz, x - 1, y, z, bigRadius, smallRadius);
                                cnt += isInsideTorus(centerx, centery, centerz, x + 1, y, z, bigRadius, smallRadius);
                                cnt += isInsideTorus(centerx, centery, centerz, x, y, z - 1, bigRadius, smallRadius);
                                cnt += isInsideTorus(centerx, centery, centerz, x, y, z + 1, bigRadius, smallRadius);
                                cnt += isInsideTorus(centerx, centery, centerz, x, y - 1, z, bigRadius, smallRadius);
                                cnt += isInsideTorus(centerx, centery, centerz, x, y + 1, z, bigRadius, smallRadius);
                            }
                            if (cnt != 6) {
                                placeBlockIfPossible(worldObj, blocks, maxSize, x, y, z, forquarry);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        icons[CARD_SHAPE] = iconRegister.registerIcon(RFTools.MODID + ":shapeCardItem");
        icons[CARD_VOID] = iconRegister.registerIcon(RFTools.MODID + ":shapeCardVoidItem");
        icons[CARD_QUARRY] = iconRegister.registerIcon(RFTools.MODID + ":shapeCardQuarryItem");
        icons[CARD_QUARRY_SILK] = iconRegister.registerIcon(RFTools.MODID + ":shapeCardSilkItem");
        icons[CARD_QUARRY_FORTUNE] = iconRegister.registerIcon(RFTools.MODID + ":shapeCardFortuneItem");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconIndex(ItemStack stack) {
        int damage = stack.getItemDamage();
        return icons[damage];
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        if (itemStack.getItemDamage() == 0) {
            return super.getUnlocalizedName(itemStack);
        } else {
            return super.getUnlocalizedName(itemStack) + itemStack.getItemDamage();
        }
    }

    @Override
    public void getSubItems(Item item, CreativeTabs creativeTabs, List list) {
        for (int i = 0 ; i < icons.length ; i++) {
            list.add(new ItemStack(this, 1, i));
        }
    }

}