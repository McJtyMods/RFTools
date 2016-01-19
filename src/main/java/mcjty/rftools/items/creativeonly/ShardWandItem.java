package mcjty.rftools.items.creativeonly;

import mcjty.lib.api.Infusable;
import mcjty.lib.base.GeneralConfig;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.varia.Logging;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ShardWandItem extends GenericRFToolsItem {

    public ShardWandItem() {
        super("shard_wand");
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            Block block = world.getBlockState(pos).getBlock();
            if (block instanceof Infusable) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof GenericTileEntity) {
                    GenericTileEntity genericTileEntity = (GenericTileEntity) te;
                    int infused = genericTileEntity.getInfused();
                    if (infused < GeneralConfig.maxInfuse) {
                        infused = GeneralConfig.maxInfuse;
                        Logging.message(player, "Maximized infusion level!");
                    } else {
                        infused = 0;
                        Logging.message(player, "Cleared infusion level!");
                    }
                    genericTileEntity.setInfused(infused);
                } else {
                    Logging.message(player, "This block doesn't have the right tile entity!");
                }
            } else {
                Logging.message(player, "This block is not infusable!");
            }
            return true;
        }
        return true;
    }
}