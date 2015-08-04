package mcjty.rftools.blocks.storage;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.blocks.storage.modules.TypeModule;
import mcjty.rftools.items.storage.DimletTypeItem;
import mcjty.rftools.items.storage.GenericTypeItem;
import mcjty.rftools.items.storage.OreDictTypeItem;
import mcjty.rftools.network.RFToolsMessages;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModularStorageBlock extends GenericRFToolsBlock {

    public static int RENDERID_MODULARSTORAGE;

    private IIcon overlayIcon;
    private Map<Class<? extends TypeModule>, IIcon> icons = new HashMap<Class<? extends TypeModule>, IIcon>();

    public ModularStorageBlock() {
        super(Material.iron, ModularStorageTileEntity.class, true);
        setBlockName("modularStorageBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_MODULAR_STORAGE;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return RENDERID_MODULARSTORAGE;
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineModularStorage";
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
                    RFToolsMessages.INSTANCE.sendToServer(new PacketGetCountInfo(modularStorageTileEntity.getWorldObj().provider.dimensionId,
                            modularStorageTileEntity.xCoord, modularStorageTileEntity.yCoord, modularStorageTileEntity.zCoord));
                }
                int stacks = ReturnCountInfoHelper.cnt;
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
    public void registerBlockIcons(IIconRegister iconRegister) {
        icons.put(DimletTypeItem.class, iconRegister.registerIcon(RFTools.MODID + ":" + "machineModularStorageDimlet"));
        icons.put(OreDictTypeItem.class, iconRegister.registerIcon(RFTools.MODID + ":" + "machineModularStorageOre"));
        icons.put(GenericTypeItem.class, iconRegister.registerIcon(RFTools.MODID + ":" + "machineModularStorageGeneric"));
        overlayIcon = iconRegister.registerIcon(RFTools.MODID + ":" + "modularStorageOverlay");
        super.registerBlockIcons(iconRegister);
    }

    public IIcon getOverlayIcon() {
        return overlayIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        ModularStorageTileEntity modularStorageTileEntity = (ModularStorageTileEntity) tileEntity;
        ModularStorageContainer modularStorageContainer = new ModularStorageContainer(entityPlayer, modularStorageTileEntity);
        return new GuiModularStorage(modularStorageTileEntity, modularStorageContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        ModularStorageTileEntity modularStorageTileEntity = (ModularStorageTileEntity) tileEntity;
        // Make sure the client has sufficient information to show the data.
        modularStorageTileEntity.syncToClient();
        return new ModularStorageContainer(entityPlayer, modularStorageTileEntity);
    }

    @Override
    public IIcon getIconInd(IBlockAccess blockAccess, int x, int y, int z, int meta) {
        TileEntity te = blockAccess.getTileEntity(x, y, z);
        if (te instanceof ModularStorageTileEntity) {
            ModularStorageTileEntity modularStorageTileEntity = (ModularStorageTileEntity) te;
            ItemStack stack = modularStorageTileEntity.getStackInSlot(ModularStorageContainer.SLOT_TYPE_MODULE);
            if (stack != null && stack.stackSize > 0 && stack.getItem() instanceof TypeModule) {
                IIcon icon = icons.get(stack.getItem().getClass());
                if (icon != null) {
                    return icon;
                }
            }
        }
        return super.getIconInd(blockAccess, x, y, z, meta);
    }
}
