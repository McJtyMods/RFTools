package mcjty.rftools.blocks.storagemonitor;

import mcjty.lib.api.Infusable;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class StorageScannerBlock extends GenericRFToolsBlock<StorageScannerTileEntity, StorageScannerContainer> implements Infusable {

    public StorageScannerBlock() {
        super(Material.IRON, StorageScannerTileEntity.class, StorageScannerContainer.class, "storage_scanner", true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<GuiStorageScanner> getGuiClass() {
        return GuiStorageScanner.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag advancedToolTip) {
        super.addInformation(itemStack, player, list, advancedToolTip);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This machine will scan all nearby inventories");
            list.add(TextFormatting.WHITE + "and show them in a list. You can then search");
            list.add(TextFormatting.WHITE + "for items in all those inventories.");
            list.add(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_STORAGE_SCANNER;
    }
}
