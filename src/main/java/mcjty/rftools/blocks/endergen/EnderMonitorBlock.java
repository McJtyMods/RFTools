package mcjty.rftools.blocks.endergen;

import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.List;

public class EnderMonitorBlock extends LogicSlabBlock {

    public EnderMonitorBlock() {
        super("ender_monitor", new BlockBuilder().tileEntitySupplier(EnderMonitorTileEntity::new));
    }

//    @Override
//    public int getGuiID() {
//        return GuiProxy.GUI_ENDERMONITOR;
//    }
//
//    @Override
//    public BiFunction<EnderMonitorTileEntity, EmptyContainer, GenericGuiContainer<? super EnderMonitorTileEntity>> getGuiFactory() {
//        return GuiEnderMonitor::new;
//    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable IBlockReader world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            int mode = tagCompound.getInt("mode");
            String smode = EnderMonitorMode.values()[mode].getDescription();
            list.add(new StringTextComponent(TextFormatting.GREEN + "Mode: " + smode));
        }
    }

}
