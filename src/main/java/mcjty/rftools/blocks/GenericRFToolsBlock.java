package mcjty.rftools.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.security.OrphaningCardItem;
import mcjty.rftools.blocks.security.SecurityChannels;
import mcjty.rftools.blocks.security.SecurityConfiguration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public abstract class GenericRFToolsBlock extends BaseBlock {

    public GenericRFToolsBlock(String name, BlockBuilder builder) {
        super(name, builder);
    }

    //    public GenericRFToolsBlock(Material material, Class<? extends T> tileEntityClass, BiFunction<PlayerEntity, IInventory, C> containerFactory, String name, boolean isContainer) {
//        super(RFTools.instance, material, tileEntityClass, containerFactory, name, isContainer);
//        setCreativeTab(RFTools.setup.getTab());
//    }
//
//    public GenericRFToolsBlock(Material material, Class<? extends T> tileEntityClass, BiFunction<PlayerEntity, IInventory, C> containerFactory,
//                               Function<Block, ItemBlock> itemBlockFunction, String name, boolean isContainer) {
//        super(RFTools.instance, material, tileEntityClass, containerFactory, itemBlockFunction, name, isContainer);
//        setCreativeTab(RFTools.setup.getTab());
//    }
//


    @Override
    protected boolean checkAccess(World world, PlayerEntity player, TileEntity te) {
        if (SecurityConfiguration.enabled.get() && te instanceof GenericTileEntity) {
            GenericTileEntity genericTileEntity = (GenericTileEntity) te;
            if ((!OrphaningCardItem.isPrivileged(player, world)) && (!player.getUniqueID().equals(genericTileEntity.getOwnerUUID()))) {
                int securityChannel = genericTileEntity.getSecurityChannel();
                if (securityChannel != -1) {
                    SecurityChannels securityChannels = SecurityChannels.getChannels(world);
                    SecurityChannels.SecurityChannel channel = securityChannels.getChannel(securityChannel);
                    // @todo 1.14 check: is this still correct? Use UUID
                    boolean playerListed = channel.getPlayers().contains(player.getDisplayName().getFormattedText());
                    if (channel.isWhitelist() != playerListed) {
                        Logging.message(player, TextFormatting.RED + "You have no permission to use this block!");
                        return true;
                    }
                }
            }
        }
        return false;
    }


}
