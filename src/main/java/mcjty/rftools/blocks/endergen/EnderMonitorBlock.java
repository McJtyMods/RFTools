package mcjty.rftools.blocks.endergen;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.logic.LogicSlabBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class EnderMonitorBlock extends LogicSlabBlock<EnderMonitorTileEntity, EmptyContainer> {

    public EnderMonitorBlock() {
        super(Material.iron, "ender_monitor", EnderMonitorTileEntity.class, EmptyContainer.class);
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_ENDERMONITOR;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiEnderMonitor.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int mode = tagCompound.getInteger("mode");
            String smode = EnderMonitorMode.values()[mode].getDescription();
            list.add(TextFormatting.GREEN + "Mode: " + smode);
        }
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineEnderMonitorTop";
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        // We don't want to do what LogicSlabBlock does as we don't react on redstone input.
    }
}
