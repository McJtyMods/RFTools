package mcjty.rftools.blocks.spawner;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.container.InventoryHelper;
import mcjty.entity.GenericEnergyReceiverTileEntity;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.BlockTools;
import mcjty.rftools.network.Argument;
import mcjty.rftools.network.PacketHandler;
import mcjty.rftools.network.PacketServerCommand;
import mcjty.varia.Coordinate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;

public class MatterBeamerTileEntity extends GenericEnergyReceiverTileEntity implements ISidedInventory {

    public static final int TICKTIME = 20;
    public static String CMD_SETDESTINATION = "setDest";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, MatterBeamerContainer.factory, 1);

    // The location of the destination spawner..
    private Coordinate destination = null;

    private int ticker = TICKTIME;

    public MatterBeamerTileEntity() {
        super(SpawnerConfiguration.BEAMER_MAXENERGY, SpawnerConfiguration.BEAMER_RECEIVEPERTICK);
    }

    @Override
    protected void checkStateServer() {
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        if (!BlockTools.getRedstoneSignal(meta)) {
            disableBlockGlow();
            return;
        }

        ticker--;
        if (ticker > 0) {
            return;
        }
        ticker = TICKTIME;

        TileEntity te = null;
        if (destination != null) {
            te = worldObj.getTileEntity(destination.getX(), destination.getY(), destination.getZ());
            if (!(te instanceof SpawnerTileEntity)) {
                setDestination(null);
                return;
            }
        } else {
            return;
        }

        ItemStack itemStack = inventoryHelper.getStacks()[0];
        if (itemStack == null || itemStack.stackSize == 0) {
            disableBlockGlow();
            return;
        }

        SpawnerTileEntity spawnerTileEntity = (SpawnerTileEntity) te;

        int maxblocks = (int) (SpawnerConfiguration.beamBlocksPerSend * (1.01 + getInfusedFactor() * 2.0));
        int numblocks = Math.min(maxblocks, itemStack.stackSize);

        int rf = (int) (SpawnerConfiguration.beamRfPerObject * numblocks * (4.0f - getInfusedFactor()) / 4.0f);
        if (getEnergyStored(ForgeDirection.DOWN) < rf) {
            return;
        }
        consumeEnergy(rf);

        spawnerTileEntity.addMatter(itemStack, numblocks);
        inventoryHelper.decrStackSize(0, numblocks);
        enableBlockGlow();
    }

    private void disableBlockGlow() {
        // Bit 0 is active, bit 3 is redstone.
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        if ((meta & 1) != 0) {
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (meta & ~1), 3);
        }
    }

    private void enableBlockGlow() {
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        if ((meta & 1) == 0) {
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, meta | 1, 3);
        }
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(xCoord - 4, yCoord - 4, zCoord - 4, xCoord + 5, yCoord + 5, zCoord + 5);
    }


    // Called from client side when a wrench is used.
    public void useWrench(EntityPlayer player) {
        Coordinate thisCoord = new Coordinate(xCoord, yCoord, zCoord);
        Coordinate coord = RFTools.instance.clientInfo.getSelectedTE();
        TileEntity tileEntity = null;
        if (coord != null) {
            tileEntity = worldObj.getTileEntity(coord.getX(), coord.getY(), coord.getZ());
        }

        if (!(tileEntity instanceof MatterBeamerTileEntity)) {
            // None selected. Just select this one.
            RFTools.instance.clientInfo.setSelectedTE(thisCoord);
            SpawnerTileEntity destinationTE = getDestinationTE();
            if (destinationTE == null) {
                RFTools.instance.clientInfo.setDestinationTE(null);
            } else {
                RFTools.instance.clientInfo.setDestinationTE(new Coordinate(destinationTE.xCoord, destinationTE.yCoord, destinationTE.zCoord));
            }
            RFTools.message(player, "Select a spawner as destination");
        } else if (coord.equals(thisCoord)) {
            // Unselect this one.
            RFTools.instance.clientInfo.setSelectedTE(null);
            RFTools.instance.clientInfo.setDestinationTE(null);
            setDestination(null);
            RFTools.message(player, "Destination cleared!");
        }
    }

    public void setDestination(Coordinate destination) {
        this.destination = destination;
        disableBlockGlow();
        markDirty();

        if (worldObj.isRemote) {
            // We're on the client. Send change to server.
            PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(xCoord, yCoord, zCoord,
                    MatterBeamerTileEntity.CMD_SETDESTINATION,
                    new Argument("dest", destination)));
        } else {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public Coordinate getDestination() {
        return destination;
    }

    /**
     * Get the current destination. This function checks first if that destination is
     * still valid and if not it is reset to null (i.e. the destination was removed).
     * @return the destination TE or null if there is no valid one
     */
    private SpawnerTileEntity getDestinationTE() {
        if (destination == null) {
            return null;
        }
        TileEntity te = worldObj.getTileEntity(destination.getX(), destination.getY(), destination.getZ());
        if (te instanceof SpawnerTileEntity) {
            return (SpawnerTileEntity) te;
        } else {
            destination = null;
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return null;
        }
    }


    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        destination = Coordinate.readFromNBT(tagCompound, "dest");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.getStacks()[i] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        Coordinate.writeToNBT(tagCompound, "dest", destination);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = 0 ; i < inventoryHelper.getStacks().length ; i++) {
            ItemStack stack = inventoryHelper.getStacks()[i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return MatterBeamerContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return MatterBeamerContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return MatterBeamerContainer.factory.isOutputSlot(index);
    }

    @Override
    public int getSizeInventory() {
        return inventoryHelper.getStacks().length;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventoryHelper.getStacks()[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        return inventoryHelper.decrStackSize(index, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
    }


    @Override
    public String getInventoryName() {
        return "Beamer Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETDESTINATION.equals(command)) {
            setDestination(args.get("dest").getCoordinate());
            return true;
        }
        return false;
    }

}
