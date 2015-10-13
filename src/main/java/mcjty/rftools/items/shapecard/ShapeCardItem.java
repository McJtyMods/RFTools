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

}