package mcjty.rftools.blocks.endergen;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.BiFunction;

public class EnderMonitorBlock extends LogicSlabBlock<EnderMonitorTileEntity, EmptyContainer> {

    public EnderMonitorBlock() {
        super(RFTools.instance, Material.IRON, EnderMonitorTileEntity.class, EmptyContainer::new, "ender_monitor", false);
        setCreativeTab(RFTools.setup.getTab());
    }

    @Override
    public boolean needsRedstoneCheck() {
        return false;
    }

    @Override
    public int getGuiID() {
        return GuiProxy.GUI_ENDERMONITOR;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BiFunction<EnderMonitorTileEntity, EmptyContainer, GenericGuiContainer<? super EnderMonitorTileEntity>> getGuiFactory() {
        return GuiEnderMonitor::new;
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
