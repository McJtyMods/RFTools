package mcjty.rftools.blocks.logic.wireless;

import mcjty.lib.McJtyLib;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;

import java.util.List;

public class RedstoneReceiverBlock extends RedstoneChannelBlock {

    public RedstoneReceiverBlock() {
        super("redstone_receiver_block", new BlockBuilder()
            .tileEntitySupplier(RedstoneReceiverTileEntity::new));
    }

//    @Override
//    public BiFunction<RedstoneReceiverTileEntity, EmptyContainer, GenericGuiContainer<? super RedstoneReceiverTileEntity>> getGuiFactory() {
//        return GuiRedstoneReceiver::new;
//    }

    @Override
    public void addInformation(ItemStack itemStack, IBlockReader world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(new StringTextComponent(TextFormatting.WHITE + "This logic block sends redstone signals from"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "a linked transmitter. Right click on a transmitter"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "(or other receiver) to link"));
        } else {
            list.add(new StringTextComponent(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE));
        }
    }
}
