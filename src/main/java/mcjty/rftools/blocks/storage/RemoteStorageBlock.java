package mcjty.rftools.blocks.storage;

import mcjty.lib.api.Infusable;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.gui.GuiProxy;
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

public class RemoteStorageBlock extends GenericRFToolsBlock<RemoteStorageTileEntity, RemoteStorageContainer> implements Infusable {

    public RemoteStorageBlock() {
        super(Material.IRON, RemoteStorageTileEntity.class, RemoteStorageContainer::new, "remote_storage", true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BiFunction<RemoteStorageTileEntity, RemoteStorageContainer, GenericGuiContainer<? super RemoteStorageTileEntity>> getGuiFactory() {
        return GuiRemoteStorage::new;
    }

    @Override
    public int getGuiID() {
        return GuiProxy.GUI_REMOTE_STORAGE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "Make storage modules remotely available.");
            list.add(TextFormatting.WHITE + "Requires energy to do this.");
            list.add(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption");
        } else {
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }
}
