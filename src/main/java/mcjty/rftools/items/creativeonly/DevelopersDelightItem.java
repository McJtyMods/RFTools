package mcjty.rftools.items.creativeonly;

import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.GenericRFToolsItem;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DevelopersDelightItem extends GenericRFToolsItem {

    public DevelopersDelightItem() {
        super("developers_delight");
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            dumpInfo(world, pos);
            GuiDevelopersDelight.setSelected(pos);
            player.openGui(RFTools.instance, RFTools.GUI_DEVELOPERS_DELIGHT, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.SUCCESS;
    }

    private void dumpInfo(World world, BlockPos pos) {
        if (world.isAirBlock(pos)) {
            return;
        }
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        int meta = block.getMetaFromState(state);
        String modid = RFToolsTools.getModidForBlock(block);
        Logging.log("Block: " + block.getUnlocalizedName() + ", Meta: " + meta + ", Mod: " + modid);
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null) {
            NBTTagCompound tag = new NBTTagCompound();
            try {
                tileEntity.writeToNBT(tag);
                StringBuffer buffer = new StringBuffer();
                RFToolsTools.convertNBTtoJson(buffer, tag, 0);
                Logging.log(buffer.toString());
            } catch (Exception e) {
                Logging.log("Catched a crash during dumping of NBT");
            }
        }
    }


}
