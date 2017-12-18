package mcjty.rftools.varia;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RFToolsTools {

    public static boolean chunkLoaded(World world, BlockPos pos) {
        if (world == null || pos == null) {
            return false;
        }
        return world.isBlockLoaded(pos);
    }

    public static boolean hasModuleTarget(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return false;
        }
        return stack.getTagCompound().hasKey("monitorx");
    }

    public static int getDimensionFromModule(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return 0;
        }
        return stack.getTagCompound().getInteger("monitordim");
    }

    public static void setPositionInModule(ItemStack stack, Integer dimension, BlockPos pos, String name) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        if (dimension != null) {
            stack.getTagCompound().setInteger("monitordim", dimension);
        }
        if (name != null) {
            stack.getTagCompound().setString("monitorname", name);
        }
        stack.getTagCompound().setInteger("monitorx", pos.getX());
        stack.getTagCompound().setInteger("monitory", pos.getY());
        stack.getTagCompound().setInteger("monitorz", pos.getZ());
    }

    public static void clearPositionInModule(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound tagCompound = stack.getTagCompound();
        tagCompound.removeTag("monitordim");
        tagCompound.removeTag("monitorx");
        tagCompound.removeTag("monitory");
        tagCompound.removeTag("monitorz");
        tagCompound.removeTag("monitorname");
    }

    public static BlockPos getPositionFromModule(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound tagCompound = stack.getTagCompound();
        int monitorx = tagCompound.getInteger("monitorx");
        int monitory = tagCompound.getInteger("monitory");
        int monitorz = tagCompound.getInteger("monitorz");
        return new BlockPos(monitorx, monitory, monitorz);
    }
}
