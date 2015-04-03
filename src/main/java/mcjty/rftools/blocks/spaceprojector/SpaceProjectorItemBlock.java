package mcjty.rftools.blocks.spaceprojector;

import mcjty.container.GenericItemBlock;
import mcjty.rftools.RFTools;
import mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class SpaceProjectorItemBlock extends GenericItemBlock {
    public SpaceProjectorItemBlock(Block block) {
        super(block);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof SpaceChamberControllerTileEntity) {
            if (!world.isRemote) {
                SpaceChamberControllerTileEntity redstoneTransmitterTileEntity = (SpaceChamberControllerTileEntity) te;
                Coordinate minCorner = redstoneTransmitterTileEntity.getMinCorner();
                Coordinate maxCorner = redstoneTransmitterTileEntity.getMaxCorner();
                if (minCorner == null || maxCorner == null) {
                    RFTools.message(player, EnumChatFormatting.YELLOW + "This chamber controller has no formed chamber yet!");
                } else {
                    NBTTagCompound tagCompound = stack.getTagCompound();
                    if (tagCompound == null) {
                        tagCompound = new NBTTagCompound();
                    }
                    Coordinate.writeToNBT(tagCompound, "controller", new Coordinate(x, y, z));
                    tagCompound.setInteger("dim", world.provider.dimensionId);
                    stack.setTagCompound(tagCompound);
                    RFTools.message(player, EnumChatFormatting.WHITE + "Chamber controller target set!");
                }
            }
        } else {
            return super.onItemUse(stack, player, world, x, y, z, side, sx, sy, sz);
        }
        return true;
    }

}
