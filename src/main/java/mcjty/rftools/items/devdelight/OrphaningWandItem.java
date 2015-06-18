package mcjty.rftools.items.devdelight;

import mcjty.api.Infusable;
import mcjty.entity.GenericTileEntity;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class OrphaningWandItem extends Item {

    public OrphaningWandItem() {
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof GenericTileEntity) {
                GenericTileEntity genericTileEntity = (GenericTileEntity) te;
                if (genericTileEntity.getOwnerUUID() == null) {
                    RFTools.message(player, "This block has no owner!");
                } else {
                    genericTileEntity.clearOwner();
                    RFTools.message(player, "Cleared owner!");
                }
            } else {
                RFTools.message(player, "This block doesn't have the right tile entity!");
            }
            return true;
        }
        return true;
    }
}