package mcjty.rftools.blocks;

import cofh.api.item.IToolHammer;
import mcjty.lib.container.GenericBlock;
import mcjty.lib.container.WrenchUsage;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public abstract class GenericRFToolsBlock extends GenericBlock {

    public GenericRFToolsBlock(Material material, Class<? extends TileEntity> tileEntityClass, boolean isContainer) {
        super(RFTools.instance, material, tileEntityClass, isContainer);
    }

    @Override
    protected WrenchUsage getWrenchUsage(BlockPos pos, EntityPlayer player, ItemStack itemStack, WrenchUsage wrenchUsed, Item item) {
        WrenchUsage usage = super.getWrenchUsage(pos, player, itemStack, wrenchUsed, item);
        if (item instanceof IToolHammer && usage == WrenchUsage.DISABLED) {
            // It is still possible it is a smart wrench.
//            if (item instanceof SmartWrench) {
//                SmartWrench smartWrench = (SmartWrench) item;
//                SmartWrenchMode mode = smartWrench.getMode(itemStack);
//                if (mode.equals(SmartWrenchMode.MODE_SELECT)) {
//                    if (player.isSneaking()) {
//                        usage = WrenchUsage.SNEAK_SELECT;
//                    } else {
//                        usage = WrenchUsage.SELECT;
//                    }
//                }
//            }
        }
        return usage;
    }

    @Override
    protected boolean checkAccess(World world, EntityPlayer player, TileEntity te) {
        if (te instanceof GenericTileEntity) {
//            GenericTileEntity genericTileEntity = (GenericTileEntity) te;
//            if ((!SecurityTools.isAdmin(player)) && (!player.getPersistentID().equals(genericTileEntity.getOwnerUUID()))) {
//                int securityChannel = genericTileEntity.getSecurityChannel();
//                if (securityChannel != -1) {
//                    SecurityChannels securityChannels = SecurityChannels.getChannels(world);
//                    SecurityChannels.SecurityChannel channel = securityChannels.getChannel(securityChannel);
//                    boolean playerListed = channel.getPlayers().contains(player.getDisplayName());
//                    if (channel.isWhitelist() != playerListed) {
//                        Logging.message(player, EnumChatFormatting.RED + "You have no permission to use this block!");
//                        return true;
//                    }
//                }
//            }
        }
        return false;
    }


}
