package mcjty.rftools.blocks.teleporter;

import mcjty.lib.varia.GlobalCoordinate;
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

public class SimpleDialerItemBlock extends LogicItemBlock {
    public SimpleDialerItemBlock(Block block) {
        super(block);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        TileEntity te = world.getTileEntity(pos);
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }

        if (te instanceof MatterTransmitterTileEntity) {
            if (!world.isRemote) {
                MatterTransmitterTileEntity matterTransmitterTileEntity = (MatterTransmitterTileEntity) te;

                if (!matterTransmitterTileEntity.checkAccess(player.getName())) {
                    Logging.message(player, TextFormatting.RED + "You have no access to this matter transmitter!");
                    return EnumActionResult.FAIL;
                }

                tagCompound.setInteger("transX", matterTransmitterTileEntity.getPos().getX());
                tagCompound.setInteger("transY", matterTransmitterTileEntity.getPos().getY());
                tagCompound.setInteger("transZ", matterTransmitterTileEntity.getPos().getZ());
                tagCompound.setInteger("transDim", world.provider.getDimension());

                if (matterTransmitterTileEntity.isDialed()) {
                    Integer id = matterTransmitterTileEntity.getTeleportId();
                    boolean access = checkReceiverAccess(player, world, id);
                    if (!access) {
                        Logging.message(player, TextFormatting.RED + "You have no access to the matter receiver!");
                        return EnumActionResult.FAIL;
                    }

                    tagCompound.setInteger("receiver", id);
                    Logging.message(player, TextFormatting.YELLOW + "Receiver set!");
                }

                Logging.message(player, TextFormatting.YELLOW + "Transmitter set!");
            }
        } else if (te instanceof MatterReceiverTileEntity) {
            if (!world.isRemote) {
                MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;

                Integer id  = matterReceiverTileEntity.getOrCalculateID();
                boolean access = checkReceiverAccess(player, world, id);
                if (!access) {
                    Logging.message(player, TextFormatting.RED + "You have no access to this matter receiver!");
                    return EnumActionResult.FAIL;
                }

                tagCompound.setInteger("receiver", id);
                Logging.message(player, TextFormatting.YELLOW + "Receiver set!");
            }
        } else {
            return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
        }

        stack.setTagCompound(tagCompound);
        return EnumActionResult.SUCCESS;
    }

    private boolean checkReceiverAccess(EntityPlayer player, World world, Integer id) {
        boolean access = true;
        TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
        GlobalCoordinate coordinate = destinations.getCoordinateForId(id);
        if (coordinate != null) {
            TeleportDestination destination = destinations.getDestination(coordinate);
            if (destination != null) {
                World worldForDimension = TeleportationTools.getWorldForDimension(world, destination.getDimension());
                if (worldForDimension != null) {
                    TileEntity recTe = worldForDimension.getTileEntity(destination.getCoordinate());
                    if (recTe instanceof MatterReceiverTileEntity) {
                        MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) recTe;
                        if (!matterReceiverTileEntity.checkAccess(player.getName())) {
                            access = false;
                        }
                    }
                }
            }
        }
        return access;
    }
}
