package mcjty.rftools.blocks.spaceprojector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.RFTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

public class SpaceChamberCardItem extends Item {

    public SpaceChamberCardItem() {
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
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        int channel = -1;
        if (tagCompound != null) {
            channel = tagCompound.getInteger("channel");
        }
        if (channel != -1) {
            list.add(EnumChatFormatting.YELLOW + "Channel: " + channel);
        } else {
            list.add(EnumChatFormatting.YELLOW + "Channel is not set!");
        }
        list.add(EnumChatFormatting.WHITE + "Sneak right-click on a space chamber controller");
        list.add(EnumChatFormatting.WHITE + "to set the channel for this card.");
        list.add(EnumChatFormatting.WHITE + "Insert it in a space projector to project the");
        list.add(EnumChatFormatting.WHITE + "linked area");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        TileEntity te = world.getTileEntity(x, y, z);
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
        }
        int channel = -1;
        if (te instanceof SpaceChamberControllerTileEntity) {
            channel = ((SpaceChamberControllerTileEntity) te).getChannel();
        }

        if (channel == -1) {
            tagCompound.removeTag("channel");
            if (world.isRemote) {
                RFTools.message(player, "Card is cleared");
            }
        } else {
            tagCompound.setInteger("channel", channel);
            if (world.isRemote) {
                RFTools.message(player, "Card is set to channel '" + channel + "'");
            }
        }
        return true;
    }
}