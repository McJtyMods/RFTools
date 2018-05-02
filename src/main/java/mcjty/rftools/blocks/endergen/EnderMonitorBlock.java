package mcjty.rftools.blocks.endergen;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.LogicSlabBlock;
import mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class EnderMonitorBlock extends LogicSlabBlock<EnderMonitorTileEntity, EmptyContainer> {

    public EnderMonitorBlock() {
        super(RFTools.instance, Material.IRON, EnderMonitorTileEntity.class, EmptyContainer.class, "ender_monitor", false);
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public boolean needsRedstoneCheck() {
        return false;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_ENDERMONITOR;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<GuiEnderMonitor> getGuiClass() {
        return GuiEnderMonitor.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int mode = tagCompound.getInteger("mode");
            String smode = EnderMonitorMode.values()[mode].getDescription();
            list.add(TextFormatting.GREEN + "Mode: " + smode);
        }
    }

}
