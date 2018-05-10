package mcjty.rftools.gui;

import mcjty.lib.blocks.GenericBlock;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.storage.GuiModularStorage;
import mcjty.rftools.blocks.storage.ModularStorageItemContainer;
import mcjty.rftools.blocks.storage.RemoteStorageItemContainer;
import mcjty.rftools.blocks.storagemonitor.GuiStorageScanner;
import mcjty.rftools.blocks.storagemonitor.StorageScannerContainer;
import mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;
import mcjty.rftools.items.builder.GuiChamberDetails;
import mcjty.rftools.items.builder.GuiShapeCard;
import mcjty.rftools.items.creativeonly.GuiDevelopersDelight;
import mcjty.rftools.items.manual.GuiRFToolsManual;
import mcjty.rftools.items.modifier.GuiModifier;
import mcjty.rftools.items.modifier.ModifierContainer;
import mcjty.rftools.items.netmonitor.GuiNetworkMonitor;
import mcjty.rftools.items.storage.GuiStorageFilter;
import mcjty.rftools.items.storage.StorageFilterContainer;
import mcjty.rftools.items.teleportprobe.GuiAdvancedPorter;
import mcjty.rftools.items.teleportprobe.GuiTeleportProbe;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiProxy implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int guiid, EntityPlayer entityPlayer, World world, int x, int y, int z) {
        if (guiid == RFTools.GUI_MANUAL_MAIN || guiid == RFTools.GUI_MANUAL_SHAPE || guiid == RFTools.GUI_TELEPORTPROBE || guiid == RFTools.GUI_ADVANCEDPORTER
                || guiid == RFTools.GUI_SHAPECARD || guiid == RFTools.GUI_SHAPECARD_COMPOSER
                || guiid == RFTools.GUI_CHAMBER_DETAILS || guiid == RFTools.GUI_DEVELOPERS_DELIGHT || guiid == RFTools.GUI_LIST_BLOCKS) {
            return null;
        } else if (guiid == RFTools.GUI_REMOTE_STORAGE_ITEM) {
            return new RemoteStorageItemContainer(entityPlayer);
        } else if (guiid == RFTools.GUI_REMOTE_STORAGESCANNER_ITEM) {
            // We are in a tablet
            ItemStack tablet = entityPlayer.getHeldItemMainhand();
            int monitordim = RFToolsTools.getDimensionFromModule(tablet);
            BlockPos pos = RFToolsTools.getPositionFromModule(tablet);
            WorldServer w = DimensionManager.getWorld(monitordim);
            if (w == null) {
                return null;
            }
            TileEntity te = w.getTileEntity(pos);
            if (!(te instanceof StorageScannerTileEntity)) {
                return null;
            }
            return new StorageScannerContainer(entityPlayer, (IInventory) te);
        } else if (guiid == RFTools.GUI_MODULAR_STORAGE_ITEM) {
            return new ModularStorageItemContainer(entityPlayer);
        } else if (guiid == RFTools.GUI_STORAGE_FILTER) {
            return new StorageFilterContainer(entityPlayer);
        } else if (guiid == RFTools.GUI_MODIFIER_MODULE) {
            return new ModifierContainer(entityPlayer);
        }
//        if (guiid == RFTools.GUI_LIST_BLOCKS || guiid == RFTools.GUI_DEVELOPERS_DELIGHT ||
//                guiid == RFTools.GUI_MANUAL_DIMENSION || guiid == RFTools.GUI_CHAMBER_DETAILS || guiid == RFTools.GUI_SHAPECARD) {
//            return null;
//        }

        BlockPos pos = new BlockPos(x, y, z);
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof GenericBlock) {
            GenericBlock<?, ?> genericBlock = (GenericBlock<?, ?>) block;
            TileEntity te = world.getTileEntity(pos);
            return genericBlock.createServerContainer(entityPlayer, te);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int guiid, EntityPlayer entityPlayer, World world, int x, int y, int z) {
        if (guiid == RFTools.GUI_MANUAL_MAIN) {
            return new GuiRFToolsManual(GuiRFToolsManual.MANUAL_MAIN);
        } else if (guiid == RFTools.GUI_MANUAL_SHAPE) {
            return new GuiRFToolsManual(GuiRFToolsManual.MANUAL_SHAPE);
        } else if (guiid == RFTools.GUI_TELEPORTPROBE) {
            return new GuiTeleportProbe();
        } else if (guiid == RFTools.GUI_ADVANCEDPORTER) {
            return new GuiAdvancedPorter();
        } else if (guiid == RFTools.GUI_LIST_BLOCKS) {
            return new GuiNetworkMonitor();
        } else if (guiid == RFTools.GUI_DEVELOPERS_DELIGHT) {
            return new GuiDevelopersDelight();
        } else if (guiid == RFTools.GUI_REMOTE_STORAGE_ITEM) {
            return new GuiModularStorage(new RemoteStorageItemContainer(entityPlayer));
        } else if (guiid == RFTools.GUI_REMOTE_STORAGESCANNER_ITEM) {
            ItemStack tablet = entityPlayer.getHeldItemMainhand();
            int monitordim = RFToolsTools.getDimensionFromModule(tablet);
            BlockPos pos = RFToolsTools.getPositionFromModule(tablet);
            StorageScannerTileEntity te = new StorageScannerTileEntity(entityPlayer, monitordim) {
                @Override
                public BlockPos getCraftingGridContainerPos() {
                    // We are a handheld so we return a null pos for the craftinggrid
                    return null;
                }

                @Override
                public boolean isOpenWideView() {
                    TileEntity realTe = RFTools.proxy.getClientWorld().getTileEntity(pos);
                    if (realTe instanceof StorageScannerTileEntity) {
                        return ((StorageScannerTileEntity) realTe).isOpenWideView();
                    }
                    return true;
                }

                @Override
                public BlockPos getStorageScannerPos() {
                    return pos;
                }
            };
            // The position of the actual storage scanner is set on the dummy te
            te.setPos(pos);
            return new GuiStorageScanner(te, new StorageScannerContainer(entityPlayer, te));
        } else if (guiid == RFTools.GUI_MODULAR_STORAGE_ITEM) {
            return new GuiModularStorage(new ModularStorageItemContainer(entityPlayer));
        } else if (guiid == RFTools.GUI_STORAGE_FILTER) {
            return new GuiStorageFilter(new StorageFilterContainer(entityPlayer));
        } else if (guiid == RFTools.GUI_MODIFIER_MODULE) {
            return new GuiModifier(new ModifierContainer(entityPlayer));
        } else if (guiid == RFTools.GUI_SHAPECARD) {
            return new GuiShapeCard(false);
        } else if (guiid == RFTools.GUI_SHAPECARD_COMPOSER) {
            return new GuiShapeCard(true);
        } else if (guiid == RFTools.GUI_CHAMBER_DETAILS) {
            return new GuiChamberDetails();
        }
//        if (guiid == RFTools.GUI_LIST_BLOCKS) {
//            return new GuiNetworkMonitor();
//        } else if (guiid == RFTools.GUI_DEVELOPERS_DELIGHT) {
//            return new GuiDevelopersDelight();
//        } else if (guiid == RFTools.GUI_MANUAL_DIMENSION) {
//            return new GuiRFToolsManual(GuiRFToolsManual.MANUAL_DIMENSION);
//        }

        BlockPos pos = new BlockPos(x, y, z);
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof GenericBlock) {
            GenericBlock<?, ?> genericBlock = (GenericBlock<?, ?>) block;
            TileEntity te = world.getTileEntity(pos);
            return genericBlock.createClientGui(entityPlayer, te);
        }
        return null;
    }
}
