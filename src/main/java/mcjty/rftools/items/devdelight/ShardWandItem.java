package mcjty.rftools.items.devdelight;

import mcjty.base.GeneralConfig;
import mcjty.entity.GenericTileEntity;
import mcjty.rftools.RFTools;
import mcjty.api.Infusable;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ShardWandItem extends Item {

    public ShardWandItem() {
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        if (!world.isRemote) {
            Block block = world.getBlock(x, y, z);
            if (block instanceof Infusable) {
                TileEntity te = world.getTileEntity(x, y, z);
                if (te instanceof GenericTileEntity) {
                    GenericTileEntity genericTileEntity = (GenericTileEntity) te;
                    int infused = genericTileEntity.getInfused();
                    if (infused < GeneralConfig.maxInfuse) {
                        infused = GeneralConfig.maxInfuse;
                        RFTools.message(player, "Maximized infusion level!");
                    } else {
                        infused = 0;
                        RFTools.message(player, "Cleared infusion level!");
                    }
                    genericTileEntity.setInfused(infused);
                } else {
                    RFTools.message(player, "This block doesn't have the right tile entity!");
                }
            } else {
                RFTools.message(player, "This block is not infusable!");
            }
            return true;
        }
        return true;
    }
}