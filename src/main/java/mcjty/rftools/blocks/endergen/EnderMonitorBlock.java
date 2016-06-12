package mcjty.rftools.blocks.endergen;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.logic.LogicSlabBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class EnderMonitorBlock extends LogicSlabBlock<EnderMonitorTileEntity, EmptyContainer> {

    public EnderMonitorBlock() {
        super(Material.IRON, "ender_monitor", EnderMonitorTileEntity.class, EmptyContainer.class);
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
}
