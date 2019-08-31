package mcjty.rftools.items.creativeonly;

import mcjty.lib.api.Infusable;
import mcjty.lib.base.GeneralConfig;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShardWandItem extends Item {

    public ShardWandItem() {
        super(new Properties().maxStackSize(1).defaultMaxDamage(1).group(RFTools.setup.getTab()));
        setRegistryName("shard_wand");
    }


    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        if (!world.isRemote) {
            BlockPos pos = context.getPos();
            PlayerEntity player = context.getPlayer();
            Block block = world.getBlockState(pos).getBlock();
            if (block instanceof Infusable) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof GenericTileEntity) {
                    GenericTileEntity genericTileEntity = (GenericTileEntity) te;
                    int infused = genericTileEntity.getInfused();
                    if (infused < GeneralConfig.maxInfuse.get()) {
                        infused = GeneralConfig.maxInfuse.get();
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
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.SUCCESS;
    }
}