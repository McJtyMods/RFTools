package mcjty.rftools.blocks.storage;

import mcjty.lib.network.clientinfo.PacketGetInfoFromServer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.blocks.storage.modules.TypeModule;
import mcjty.rftools.network.RFToolsMessages;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

//import mcjty.rftools.items.storage.DimletTypeItem;
//import mcjty.rftools.items.storage.GenericTypeItem;
//import mcjty.rftools.items.storage.OreDictTypeItem;

public class ModularStorageBlock extends GenericRFToolsBlock {

//    private Map<Class<? extends TypeModule>, IIcon> icons = new HashMap<Class<? extends TypeModule>, IIcon>();

    public ModularStorageBlock() {
        super(Material.iron, ModularStorageTileEntity.class, ModularStorageContainer.class, GuiModularStorage.class, "modular_storage", true);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_MODULAR_STORAGE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This modular storage system can store a lot");
            list.add(EnumChatFormatting.WHITE + "of items and allows easy searching and filtering.");
            list.add(EnumChatFormatting.WHITE + "You must first insert a storage module item before");
            list.add(EnumChatFormatting.WHITE + "you can use it");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    private static long lastTime = 0;

    @Override
    public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
        return getBlockLayer() == EnumWorldBlockLayer.SOLID || getBlockLayer() == EnumWorldBlockLayer.CUTOUT;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof ModularStorageTileEntity) {
            ModularStorageTileEntity modularStorageTileEntity = (ModularStorageTileEntity) te;
            int maxSize = modularStorageTileEntity.getMaxSize();
            if (maxSize == 0) {
                currenttip.add(EnumChatFormatting.YELLOW + "No storage module!");
            } else {
                if (System.currentTimeMillis() - lastTime > 500) {
                    lastTime = System.currentTimeMillis();
                    RFToolsMessages.INSTANCE.sendToServer(new PacketGetInfoFromServer(RFTools.MODID, new StorageInfoPacketServer(modularStorageTileEntity.getWorld().provider.getDimensionId(),
                            modularStorageTileEntity.getPos())));
                }
                int stacks = StorageInfoPacketClient.cntReceived;
                if (stacks == -1) {
                    currenttip.add(EnumChatFormatting.YELLOW + "Maximum size: " + maxSize);
                } else {
                    currenttip.add(EnumChatFormatting.GREEN + "" + stacks + " out of " + maxSize);
                }
            }
        }
        return currenttip;
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        // Make sure the client has sufficient information to show the data.
        ((ModularStorageTileEntity) tileEntity).markDirtyClient();
        return super.createServerContainer(entityPlayer, tileEntity);
    }
}
