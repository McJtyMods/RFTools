package mcjty.rftools.blocks.storagemonitor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.api.Infusable;
import mcjty.container.EmptyContainer;
import mcjty.container.GenericBlock;
import mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class StorageScannerBlock extends GenericBlock implements Infusable {

    public StorageScannerBlock() {
        super(Material.iron, StorageScannerTileEntity.class, true);
        setBlockName("storageScannerBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineStorageScanner";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This machine will scan all nearby inventories");
            list.add(EnumChatFormatting.WHITE + "and show them in a list. You can then search");
            list.add(EnumChatFormatting.WHITE + "for items in all those inventories.");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: reduced power consumption and");
            list.add(EnumChatFormatting.YELLOW + "increased scanning speed.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_STORAGE_SCANNER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        StorageScannerTileEntity storageScannerTileEntity = (StorageScannerTileEntity) tileEntity;
        EmptyContainer storageScannerContainer = new EmptyContainer(entityPlayer);
        return new GuiStorageScanner(storageScannerTileEntity, storageScannerContainer);
    }
}
