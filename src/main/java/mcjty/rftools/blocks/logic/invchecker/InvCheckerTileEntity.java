package mcjty.rftools.blocks.logic.invchecker;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.logic.generic.LogicTileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Map;

public class InvCheckerTileEntity extends LogicTileEntity implements ITickable, DefaultSidedInventory {

    public static final String CMD_SETAMOUNT = "setCounter";
    public static final String CMD_SETSLOT = "setSlot";
    public static final String CMD_SETOREDICT = "setOreDict";
    public static final String CMD_SETMETA = "setUseMeta";


    private int amount = 1;
    private int slot = 0;
    private boolean oreDict = false;
    private boolean useMeta = false;
    private boolean redstoneOut = false;
    private TIntSet set1 = null;

    private int checkCounter = 0;

    private InventoryHelper inventoryHelper = new InventoryHelper(this, InvCheckerContainer.factory, 1);

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }


    public InvCheckerTileEntity() {
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        markDirty();
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
        markDirty();
    }

    public boolean isOreDict() {
        return oreDict;
    }

    public void setOreDict(boolean oreDict) {
        this.oreDict = oreDict;
        markDirty();
    }

    public boolean isUseMeta() {
        return useMeta;
    }

    public void setUseMeta(boolean useMeta) {
        this.useMeta = useMeta;
        markDirty();
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        // Clear the oredict cache
        set1 = null;
        getInventoryHelper().setInventorySlotContents(getInventoryStackLimit(), index, stack);
    }

    @Override
    public void update() {
        if (worldObj.isRemote) {
            return;
        }

        checkCounter--;
        if (checkCounter > 0) {
            return;
        }
        checkCounter = 10;

        boolean newout = false;

        EnumFacing inputSide = getFacing(worldObj.getBlockState(getPos())).getInputSide();
        BlockPos inputPos = getPos().offset(inputSide);
        TileEntity te = worldObj.getTileEntity(inputPos);
        if (InventoryHelper.isInventory(te)) {
            ItemStack stack = null;
            if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                IItemHandler capability = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if (capability == null) {
                    Block errorBlock = worldObj.getBlockState(inputPos).getBlock();
                    Logging.logError("Block: " + errorBlock.getLocalizedName() + " at " + BlockPosTools.toString(inputPos) + " returns null for getCapability(). Report to mod author");
                } else if (slot >= 0 && slot < capability.getSlots()) {
                    stack = capability.getStackInSlot(slot);
                }
            } else if (te instanceof IInventory) {
                IInventory inventory = (IInventory) te;
                if (slot >= 0 && slot < inventory.getSizeInventory()) {
                    stack = inventory.getStackInSlot(slot);
                }
            }
            if (stack != null) {
                int nr = isItemMatching(stack);
                newout = nr >= amount;
            }
        }

        if (newout != redstoneOut) {
            redstoneOut = newout;
            setRedstoneState(redstoneOut);
        }
    }

    private int isItemMatching(ItemStack stack) {
        int nr = 0;
        ItemStack matcher = inventoryHelper.getStackInSlot(0);
        if (matcher != null) {
            if (oreDict) {
                if (isEqualForOredict(matcher, stack)) {
                    if ((!useMeta) || matcher.getItemDamage() == stack.getItemDamage()) {
                        nr = stack.stackSize;
                    }
                }
            } else {
                if (useMeta) {
                    if (matcher.isItemEqual(stack)) {
                        nr = stack.stackSize;
                    }
                } else {
                    if (matcher.getItem() == stack.getItem()) {
                        nr = stack.stackSize;
                    }
                }
            }
        } else {
            nr = stack.stackSize;
        }
        return nr;
    }

    private boolean isEqualForOredict(ItemStack s1, ItemStack s2) {
        if (set1 == null) {
            int[] oreIDs1 = OreDictionary.getOreIDs(s1);
            set1 = new TIntHashSet(oreIDs1);
        }
        if (set1.isEmpty()) {
            // The first item is not an ore. In this case we do normal equality of item
            return s1.getItem() == s2.getItem();
        }

        int[] oreIDs2 = OreDictionary.getOreIDs(s2);
        if (oreIDs2.length == 0) {
            // The first is an ore but this isn't. So we cannot match.
            return false;
        }
        TIntSet set2 = new TIntHashSet(oreIDs2);
        set2.retainAll(set1);
        return !set2.isEmpty();
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        redstoneOut = tagCompound.getBoolean("rs");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        amount = tagCompound.getInteger("amount");
        slot = tagCompound.getInteger("slot");
        oreDict = tagCompound.getBoolean("oredict");
        useMeta = tagCompound.getBoolean("useMeta");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", redstoneOut);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("amount", amount);
        tagCompound.setInteger("slot", slot);
        tagCompound.setBoolean("oredict", oreDict);
        tagCompound.setBoolean("useMeta", useMeta);
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
        } else if (CMD_SETSLOT.equals(command)) {
            setSlot(args.get("slot").getInteger());
            return true;
        } else if (CMD_SETMETA.equals(command)) {
            setUseMeta(args.get("b").getBoolean());
            return true;
        } else if (CMD_SETOREDICT.equals(command)) {
            setOreDict(args.get("b").getBoolean());
            return true;
        }
        return false;
    }
}
