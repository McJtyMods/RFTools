package mcjty.rftools.blocks.teleporter;

import mcjty.lib.blocks.BaseBlockItem;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class SimpleDialerItemBlock extends BaseBlockItem {
    public SimpleDialerItemBlock(Block block) {
        super(block, new Properties().group(RFTools.setup.getTab()));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ItemStack stack = context.getItem();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();
        TileEntity te = world.getTileEntity(pos);
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }

        if (te instanceof MatterTransmitterTileEntity) {
            if (!world.isRemote) {
                MatterTransmitterTileEntity matterTransmitterTileEntity = (MatterTransmitterTileEntity) te;

                if (!matterTransmitterTileEntity.checkAccess(player.getName().getFormattedText())) {    // @todo 1.14
                    Logging.message(player, TextFormatting.RED + "You have no access to this matter transmitter!");
                    return ActionResultType.FAIL;
                }

                tagCompound.putInt("transX", matterTransmitterTileEntity.getPos().getX());
                tagCompound.putInt("transY", matterTransmitterTileEntity.getPos().getY());
                tagCompound.putInt("transZ", matterTransmitterTileEntity.getPos().getZ());
                tagCompound.putInt("transDim", world.getDimension().getType().getId());

                if (matterTransmitterTileEntity.isDialed()) {
                    Integer id = matterTransmitterTileEntity.getTeleportId();
                    boolean access = checkReceiverAccess(player, world, id);
                    if (!access) {
                        Logging.message(player, TextFormatting.RED + "You have no access to the matter receiver!");
                        return ActionResultType.FAIL;
                    }

                    tagCompound.putInt("receiver", id);
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
                    return ActionResultType.FAIL;
                }

                tagCompound.putInt("receiver", id);
                Logging.message(player, TextFormatting.YELLOW + "Receiver set!");
            }
        } else {
            return super.onItemUse(context);
        }

        stack.setTag(tagCompound);
        return ActionResultType.SUCCESS;
    }

    private boolean checkReceiverAccess(PlayerEntity player, World world, Integer id) {
        boolean access = true;
        TeleportDestinations destinations = TeleportDestinations.get();
        GlobalCoordinate coordinate = destinations.getCoordinateForId(id);
        if (coordinate != null) {
            TeleportDestination destination = destinations.getDestination(coordinate);
            if (destination != null) {
                World worldForDimension = mcjty.lib.varia.TeleportationTools.getWorldForDimension(destination.getDimension());
                if (worldForDimension != null) {
                    TileEntity recTe = worldForDimension.getTileEntity(destination.getCoordinate());
                    if (recTe instanceof MatterReceiverTileEntity) {
                        MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) recTe;
                        if (!matterReceiverTileEntity.checkAccess(player.getName().getFormattedText())) {   // @todo 1.14
                            access = false;
                        }
                    }
                }
            }
        }
        return access;
    }
}
