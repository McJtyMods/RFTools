package mcjty.rftools.blocks.storage;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.lib.api.Infusable;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class RemoteStorageBlock extends GenericRFToolsBlock implements Infusable {

    public static int RENDERID_REMOTESTORAGE;

    private IIcon overlayIcon;

    public RemoteStorageBlock() {
        super(Material.iron, RemoteStorageTileEntity.class, true);
        setBlockName("remoteStorageBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_REMOTE_STORAGE;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return RENDERID_REMOTESTORAGE;
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineRemoteStorage";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Make storage modules remotely available.");
            list.add(EnumChatFormatting.WHITE + "Requires energy to do this.");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: reduced power consumption");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        overlayIcon = iconRegister.registerIcon(RFTools.MODID + ":" + "modularStorageOverlay");
        super.registerBlockIcons(iconRegister);
    }

    public IIcon getOverlayIcon() {
        return overlayIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        RemoteStorageTileEntity remoteStorageTileEntity = (RemoteStorageTileEntity) tileEntity;
        RemoteStorageContainer remoteStorageContainer = new RemoteStorageContainer(entityPlayer, remoteStorageTileEntity);
        return new GuiRemoteStorage(remoteStorageTileEntity, remoteStorageContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new RemoteStorageContainer(entityPlayer, (RemoteStorageTileEntity) tileEntity);
    }
}
