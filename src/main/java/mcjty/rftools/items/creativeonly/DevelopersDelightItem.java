package mcjty.rftools.items.creativeonly;

import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.NBTTools;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DevelopersDelightItem extends Item {

    public DevelopersDelightItem() {
        super(new Properties().maxStackSize(1).group(RFTools.setup.getTab()));
        setRegistryName("developers_delight");
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        if (world.isRemote) {
            BlockPos pos = context.getPos();
            dumpInfo(world, pos);
            GuiDevelopersDelight.setSelected(pos);
            // @todo 1.14
//            player.openGui(RFTools.instance, GuiProxy.GUI_DEVELOPERS_DELIGHT, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.SUCCESS;
    }

    private void dumpInfo(World world, BlockPos pos) {
        if (world.isAirBlock(pos)) {
            return;
        }
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        String modid = BlockTools.getModidForBlock(block);
        Logging.log("Block: " + block.getTranslationKey() + ", Mod: " + modid);
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null) {
            CompoundNBT tag = new CompoundNBT();
            try {
                tileEntity.write(tag);
                StringBuffer buffer = new StringBuffer();
                NBTTools.convertNBTtoJson(buffer, tag, 0);
                Logging.log(buffer.toString());
            } catch (Exception e) {
                Logging.log("Catched a crash during dumping of NBT");
            }
        }
    }


}
