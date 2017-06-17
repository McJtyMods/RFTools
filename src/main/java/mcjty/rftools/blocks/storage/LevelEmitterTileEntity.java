package mcjty.rftools.blocks.storage;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.network.Argument;
import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.blocks.logic.generic.LogicTileEntity;
import mcjty.rftools.blocks.screens.ScreenSetup;
import mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.Map;

public class LevelEmitterTileEntity extends LogicTileEntity implements DefaultSidedInventory, ITickable {

    public static final String CMD_SETAMOUNT = "setCounter";
    public static final String CMD_SETOREDICT = "setOreDict";
    public static final String CMD_SETSTARRED = "setStarred";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, LevelEmitterContainer.factory, 2);

    private int amount = 1;
    private boolean oreDict = false;
    private boolean starred = false;

    private int checkCounter = 0;

    @Override
    public void update() {
        if (getWorld().isRemote) {
            return;
        }

        checkCounter--;
        if (checkCounter > 0) {
            return;
        }
        checkCounter = 10;

        int count = getCurrentCount();
        setRedstoneState(count >= amount);
    }

    public int getCurrentCount() {
        ItemStack module = inventoryHelper.getStackInSlot(LevelEmitterContainer.SLOT_MODULE);
        int count = -1;
        if (ItemStackTools.isValid(module)) {
            ItemStack matcher = inventoryHelper.getStackInSlot(LevelEmitterContainer.SLOT_ITEMMATCH);
            if (ItemStackTools.isEmpty(matcher)) {
                return count;
            }
            int dimension = RFToolsTools.getDimensionFromModule(module);
            BlockPos scannerPos = RFToolsTools.getPositionFromModule(module);
            WorldServer world = DimensionManager.getWorld(dimension);

            if (RFToolsTools.chunkLoaded(world, scannerPos)) {
                TileEntity te = world.getTileEntity(scannerPos);
                if (te instanceof StorageScannerTileEntity) {
                    StorageScannerTileEntity scannerTE = (StorageScannerTileEntity) te;
                    count = scannerTE.countItems(matcher, starred, oreDict);
                }
            }
        }
        return count;
    }


    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        boolean module = ItemStackTools.isValid(inventoryHelper.getStackInSlot(LevelEmitterContainer.SLOT_MODULE));

        super.onDataPacket(net, packet);

        if (getWorld().isRemote) {
            // If needed send a render update.
            boolean newmodule = ItemStackTools.isValid(inventoryHelper.getStackInSlot(LevelEmitterContainer.SLOT_MODULE));
            if (newmodule != module) {
                getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
            }
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        // Clear the oredict cache
        inventoryHelper.setInventorySlotContents(this.getInventoryStackLimit(), index, stack);
        if (!getWorld().isRemote) {
            // Make sure we update client-side
            markDirtyClient();
        } else {
            getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powered = tagCompound.getBoolean("rs");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        amount = tagCompound.getInteger("amount");
        oreDict = tagCompound.getBoolean("oredict");
        starred = tagCompound.getBoolean("starred");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", powered);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("amount", amount);
        tagCompound.setBoolean("oredict", oreDict);
        tagCompound.setBoolean("starred", starred);
    }


    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == LevelEmitterContainer.SLOT_MODULE && stack.getItem() != ScreenSetup.storageControlModuleItem) {
            return false;
        }
        if (index == LevelEmitterContainer.SLOT_ITEMMATCH) {
            return false;
        }
        return true;
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        markDirty();
    }

    public boolean isOreDict() {
        return oreDict;
    }

    public void setOreDict(boolean oreDict) {
        this.oreDict = oreDict;
        markDirty();
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
        markDirty();
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETAMOUNT.equals(command)) {
            setAmount(args.get("amount").getInteger());
            return true;
        } else if (CMD_SETSTARRED.equals(command)) {
            setStarred(args.get("b").getBoolean());
            return true;
        } else if (CMD_SETOREDICT.equals(command)) {
            setOreDict(args.get("b").getBoolean());
            return true;
        }
        return false;
    }
}
