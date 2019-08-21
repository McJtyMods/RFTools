package mcjty.rftools.blocks.logic.wireless;

import mcjty.lib.blocks.GenericItemBlock;
import mcjty.lib.container.EmptyContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.function.BiFunction;

public class RedstoneReceiverBlock extends RedstoneChannelBlock<RedstoneReceiverTileEntity, EmptyContainer> {

    public RedstoneReceiverBlock() {
        super(Material.IRON, RedstoneReceiverTileEntity.class, EmptyContainer::new, GenericItemBlock::new, "redstone_receiver_block");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BiFunction<RedstoneReceiverTileEntity, EmptyContainer, GenericGuiContainer<? super RedstoneReceiverTileEntity>> getGuiFactory() {
        return GuiRedstoneReceiver::new;
    }

    @Override
    public boolean needsRedstoneCheck() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This logic block sends redstone signals from");
            list.add(TextFormatting.WHITE + "a linked transmitter. Right click on a transmitter");
            list.add(TextFormatting.WHITE + "(or other receiver) to link");
        } else {
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }

    @Override
    public int getGuiID() {
        return GuiProxy.GUI_REDSTONE_RECEIVER;
    }
}
