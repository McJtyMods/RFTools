package mcjty.rftools.blocks.logic.wireless;

import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.logic.generic.LogicItemBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class RedstoneReceiverItemBlock extends LogicItemBlock {
    public RedstoneReceiverItemBlock(Block block) {
        super(block);
    }

    @Override
    protected EnumActionResult clOnItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof RedstoneTransmitterTileEntity) {
            RedstoneTransmitterTileEntity redstoneTransmitterTileEntity = (RedstoneTransmitterTileEntity) te;
            int channel = redstoneTransmitterTileEntity.getChannel();
            if (channel == -1) {
                Logging.message(player, TextFormatting.YELLOW + "This transmitter has no channel!");
            } else {
                NBTTagCompound tagCompound = stack.getTagCompound();
                if (tagCompound == null) {
                    tagCompound = new NBTTagCompound();
                }
                tagCompound.setInteger("channel", channel);
                stack.setTagCompound(tagCompound);
                if (world.isRemote) {
                    Logging.message(player, TextFormatting.YELLOW + "Channel set to " + channel + "!");
                }
            }
        } else if (te instanceof RedstoneReceiverTileEntity) {
            RedstoneReceiverTileEntity redstoneReceiverTileEntity = (RedstoneReceiverTileEntity) te;
            int channel = redstoneReceiverTileEntity.getChannel();
            if (channel == -1) {
                Logging.message(player, TextFormatting.YELLOW + "This receiver has no channel!");
            } else {
                NBTTagCompound tagCompound = stack.getTagCompound();
                if (tagCompound == null) {
                    tagCompound = new NBTTagCompound();
                }
                tagCompound.setInteger("channel", channel);
                stack.setTagCompound(tagCompound);
                if (world.isRemote) {
                    Logging.message(player, TextFormatting.YELLOW + "Channel set to " + channel + "!");
                }
                if (world.isRemote) {
                    Logging.message(player, TextFormatting.YELLOW + "Channel set to " + channel + "!");
                }
            }
        } else {
            return super.clOnItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
        }
        return EnumActionResult.SUCCESS;
    }
}
