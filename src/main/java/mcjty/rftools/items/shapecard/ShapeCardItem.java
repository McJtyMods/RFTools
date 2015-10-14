package mcjty.rftools.items.shapecard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.shield.ShieldConfiguration;
import mcjty.varia.Coordinate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapeCardItem extends Item {

    public enum Shape {
        SHAPE_BOX(0, "Box"),
        SHAPE_TOPDOME(1, "Top Dome"),
        SHAPE_BOTTOMDOME(2, "Bottom Dome"),
        SHAPE_SPHERE(3, "Sphere");

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
            list.add(EnumChatFormatting.WHITE + "This item can be configured as a shape. You");
            list.add(EnumChatFormatting.WHITE + "can then use it in the shield projector to make");
            list.add(EnumChatFormatting.WHITE + "a shield of that shape.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }


    public static Shape getShape(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return Shape.SHAPE_BOX;
        }
        int shape = tagCompound.getInteger("shape");
        return Shape.getShape(shape);
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

    private static float squaredDistance(Coordinate c, int x1, int y1, int z1) {
        int x = c.getX();
        int y = c.getY();
        int z = c.getZ();
        return (x1-x) * (x1-x) + (y1-y) * (y1-y) + (z1-z) * (z1-z);
    }

    public static void composeShape(Shape shape, World worldObj, Coordinate thisCoord, Coordinate dimension, Coordinate offset, Collection<Coordinate> blocks, int maxSize) {
        switch (shape) {
            case SHAPE_BOX:
                composeBox(worldObj, thisCoord, dimension, offset, blocks, maxSize);
                break;
            case SHAPE_TOPDOME:
                break;
            case SHAPE_BOTTOMDOME:
                break;
            case SHAPE_SPHERE:
                composeSphere(worldObj, thisCoord, dimension, offset, blocks, maxSize);
                break;
        }
    }

    private static void composeSphere(World worldObj, Coordinate thisCoord, Coordinate dimension, Coordinate offset, Collection<Coordinate> blocks, int maxSize) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        Coordinate center = new Coordinate(xCoord + offset.getX(), yCoord + offset.getY(), zCoord + offset.getZ());
        Coordinate tl = new Coordinate(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());

        for (int ox = 0 ; ox < dx ; ox++) {
            for (int oy = 0 ; oy < dy ; oy++) {
                for (int oz = 0 ; oz < dz ; oz++) {
                    int x = tl.getX() + ox;
                    int y = tl.getY() + oy;
                    int z = tl.getZ() + oz;
                    double distance = Math.sqrt(squaredDistance(center, x, y, z));
//                    System.out.println("distance = " + distance + " (" + (int)distance + ") dx = " + dx);
                    if (((int)distance) == (dx/2-1)){
                        if (worldObj.isAirBlock(x, y, z) && blocks.size() < maxSize - 1) {
                            if (y >= 0 && y < 255) {
                                blocks.add(new Coordinate(x, y, z));
                            }
                        }
                    }
                }
            }
        }
    }

    private static void composeBox(World worldObj, Coordinate thisCoord, Coordinate dimension, Coordinate offset, Collection<Coordinate> blocks, int maxSize) {
        int xCoord = thisCoord.getX();
        int yCoord = thisCoord.getY();
        int zCoord = thisCoord.getZ();
        int dx = dimension.getX();
        int dy = dimension.getY();
        int dz = dimension.getZ();
        Coordinate tl = new Coordinate(xCoord - dx/2 + offset.getX(), yCoord - dy/2 + offset.getY(), zCoord - dz/2 + offset.getZ());

        for (int ox = 0 ; ox < dx ; ox++) {
            for (int oy = 0 ; oy < dy ; oy++) {
                for (int oz = 0 ; oz < dz ; oz++) {
                    if (ox == 0 || oy == 0 || oz == 0 || ox == (dx-1) || oy == (dy-1) || oz == (dz-1)) {
                        int x = tl.getX() + ox;
                        int y = tl.getY() + oy;
                        int z = tl.getZ() + oz;
                        if (worldObj.isAirBlock(x, y, z) && blocks.size() < maxSize - 1) {
                            blocks.add(new Coordinate(x, y, z));
                        }
                    }
                }
            }
        }
    }



}