package mcjty.rftools.blocks.screens.modules;

import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.api.screens.IScreenDataHelper;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.api.screens.data.IModuleDataBoolean;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class DumpScreenModule implements IScreenModule {

    public static int COLS = 7;
    public static int ROWS = 4;

    private ItemStack[] stacks = new ItemStack[COLS*ROWS];
    protected int dim = 0;
    protected BlockPos coordinate = BlockPosTools.INVALID;
    private boolean oredict = false;

    @Override
    public IModuleDataBoolean getData(IScreenDataHelper helper, World worldObj, long millis) {
        return null;
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            setupCoordinateFromNBT(tagCompound, dim, pos);
            for (int i = 0; i < stacks.length; i++) {
                if (tagCompound.hasKey("stack" + i)) {
                    stacks[i] = new ItemStack(tagCompound.getCompoundTag("stack" + i));
                }
            }
        }
    }

    protected void setupCoordinateFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos) {
        coordinate = BlockPosTools.INVALID;
        oredict = tagCompound.getBoolean("oredict");
        if (tagCompound.hasKey("monitorx")) {
            if (tagCompound.hasKey("monitordim")) {
                this.dim = tagCompound.getInteger("monitordim");
            } else {
                // Compatibility reasons
                this.dim = tagCompound.getInteger("dim");
            }
            if (dim == this.dim) {
                BlockPos c = new BlockPos(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
                int dx = Math.abs(c.getX() - pos.getX());
                int dy = Math.abs(c.getY() - pos.getY());
                int dz = Math.abs(c.getZ() - pos.getZ());
                if (dx <= 64 && dy <= 64 && dz <= 64) {
                    coordinate = c;
                }
            }
        }
    }

    private boolean isShown(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (ItemStack s : stacks) {
            if (StorageScannerTileEntity.isItemEqual(stack, s, oredict)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked, EntityPlayer player) {
        if ((!clicked) || player == null) {
            return;
        }
        if (BlockPosTools.INVALID.equals(coordinate)) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Module is not linked to storage scanner!"), false);
            return;
        }

        StorageScannerTileEntity scannerTileEntity = StorageControlScreenModule.getStorageScanner(dim, coordinate);
        if (scannerTileEntity == null) {
            return;
        }
        int xoffset = 5;
        if (x >= xoffset) {
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                if (isShown(player.inventory.getStackInSlot(i))) {
                    ItemStack stack = scannerTileEntity.injectStackFromScreen(player.inventory.getStackInSlot(i), player);
                    player.inventory.setInventorySlotContents(i, stack);
                }
            }
            player.openContainer.detectAndSendChanges();
            return;
        }
    }

    @Override
    public int getRfPerTick() {
        return ScreenConfiguration.DUMP_RFPERTICK;
    }
}
