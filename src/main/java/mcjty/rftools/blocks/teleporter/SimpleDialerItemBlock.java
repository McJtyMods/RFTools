package mcjty.rftools.blocks.teleporter;

import mcjty.container.GenericItemBlock;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class SimpleDialerItemBlock extends GenericItemBlock {
    public SimpleDialerItemBlock(Block block) {
        super(block);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        TileEntity te = world.getTileEntity(x, y, z);
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }

        // @@@@@@@@@@@@@@@@@@@@@@@@@@ @todo access control!

        if (te instanceof MatterTransmitterTileEntity) {
            MatterTransmitterTileEntity matterTransmitterTileEntity = (MatterTransmitterTileEntity) te;

            tagCompound.setInteger("transX", matterTransmitterTileEntity.xCoord);
            tagCompound.setInteger("transY", matterTransmitterTileEntity.yCoord);
            tagCompound.setInteger("transZ", matterTransmitterTileEntity.zCoord);
            tagCompound.setInteger("transDim", world.provider.dimensionId);

            if (matterTransmitterTileEntity.isDialed()) {
                Integer id = matterTransmitterTileEntity.getTeleportId();
                tagCompound.setInteger("receiver", id);
            }
        } else if (te instanceof MatterReceiverTileEntity) {
            MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;

            Integer id;
            if (world.isRemote) {
                id = matterReceiverTileEntity.getId();
            } else {
                id = matterReceiverTileEntity.getOrCalculateID();
            }
            tagCompound.setInteger("receiver", id);
        }

        stack.setTagCompound(tagCompound);
        return true;
    }
}
