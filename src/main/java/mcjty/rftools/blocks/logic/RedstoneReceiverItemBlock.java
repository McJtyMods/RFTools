package mcjty.rftools.blocks.logic;

import mcjty.lib.container.GenericItemBlock;
import mcjty.lib.varia.Logging;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class RedstoneReceiverItemBlock extends GenericItemBlock {
    public RedstoneReceiverItemBlock(Block block) {
        super(block);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof RedstoneTransmitterTileEntity) {
            RedstoneTransmitterTileEntity redstoneTransmitterTileEntity = (RedstoneTransmitterTileEntity) te;
            int channel = redstoneTransmitterTileEntity.getChannel();
            if (channel == -1) {
                Logging.message(player, EnumChatFormatting.YELLOW + "This transmitter has no channel!");
            } else {
                NBTTagCompound tagCompound = stack.getTagCompound();
                if (tagCompound == null) {
                    tagCompound = new NBTTagCompound();
                }
                tagCompound.setInteger("channel", channel);
                stack.setTagCompound(tagCompound);
                if (world.isRemote) {
                    Logging.message(player, EnumChatFormatting.YELLOW + "Channel set to " + channel + "!");
                }
            }
        } else if (te instanceof RedstoneReceiverTileEntity) {
            RedstoneReceiverTileEntity redstoneReceiverTileEntity = (RedstoneReceiverTileEntity) te;
            int channel = redstoneReceiverTileEntity.getChannel();
            if (channel == -1) {
                Logging.message(player, EnumChatFormatting.YELLOW + "This receiver has no channel!");
            } else {
                NBTTagCompound tagCompound = stack.getTagCompound();
                if (tagCompound == null) {
                    tagCompound = new NBTTagCompound();
                }
                tagCompound.setInteger("channel", channel);
                stack.setTagCompound(tagCompound);
                if (world.isRemote) {
                    Logging.message(player, EnumChatFormatting.YELLOW + "Channel set to " + channel + "!");
                }
                if (world.isRemote) {
                    Logging.message(player, EnumChatFormatting.YELLOW + "Channel set to " + channel + "!");
                }
            }
        } else {
            return super.onItemUse(stack, player, world, x, y, z, side, sx, sy, sz);
        }
        return true;
    }
}
