package mcjty.rftools.blocks.logic.invchecker;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.container.LogicTileEntity;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.CapabilityTools;
import mcjty.lib.varia.Logging;
import mcjty.typed.TypedMap;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import static mcjty.rftools.blocks.logic.invchecker.GuiInvChecker.META_MATCH;
import static mcjty.rftools.blocks.logic.invchecker.GuiInvChecker.OREDICT_USE;

public class InvCheckerTileEntity extends LogicTileEntity implements ITickable, DefaultSidedInventory {

    public static final String CMD_SETAMOUNT = "inv.setCounter";
    public static final String CMD_SETSLOT = "inv.setSlot";
    public static final String CMD_SETOREDICT = "inv.setOreDict";
    public static final String CMD_SETMETA = "inv.setUseMeta";


    private int amount = 1;
    private int slot = 0;
    private boolean oreDict = false;
    private boolean useMeta = false;
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
        markDirtyClient();
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
        markDirtyClient();
    }

    public boolean isOreDict() {
        return oreDict;
    }

    public void setOreDict(boolean oreDict) {
        this.oreDict = oreDict;
        markDirtyClient();
    }

    public boolean isUseMeta() {
        return useMeta;
    }

    public void setUseMeta(boolean useMeta) {
        this.useMeta = useMeta;
        markDirtyClient();
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        // Clear the oredict cache
        set1 = null;
        getInventoryHelper().setInventorySlotContents(getInventoryStackLimit(), index, stack);
    }

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

        setRedstoneState(checkOutput() ? 15 : 0);
    }

    public boolean checkOutput() {
        boolean newout = false;

        EnumFacing inputSide = getFacing(getWorld().getBlockState(getPos())).getInputSide();
        BlockPos inputPos = getPos().offset(inputSide);
        TileEntity te = getWorld().getTileEntity(inputPos);
        if (InventoryHelper.isInventory(te)) {
            ItemStack stack = ItemStack.EMPTY;
            if (CapabilityTools.hasItemCapabilitySafe(te)) {
                IItemHandler capability = CapabilityTools.getItemCapabilitySafe(te);
                if (capability == null) {
                    Block errorBlock = getWorld().getBlockState(inputPos).getBlock();
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
            if (!stack.isEmpty()) {
                int nr = isItemMatching(stack);
                newout = nr >= amount;
            }
        }
        return newout;
    }

    private int isItemMatching(ItemStack stack) {
        int nr = 0;
        ItemStack matcher = inventoryHelper.getStackInSlot(0);
        if (!matcher.isEmpty()) {
            if (oreDict) {
                if (isEqualForOredict(matcher, stack)) {
                    if ((!useMeta) || matcher.getMetadata() == stack.getMetadata()) {
                        nr = stack.getCount();
                    }
                }
            } else {
                if (useMeta) {
                    if (matcher.isItemEqual(stack)) {
                        nr = stack.getCount();
                    }
                } else {
                    if (matcher.getItem() == stack.getItem()) {
                        nr = stack.getCount();
                    }
                }
            }
        } else {
            nr = stack.getCount();
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
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powerOutput = tagCompound.getBoolean("rs") ? 15 : 0;
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
        tagCompound.setBoolean("rs", powerOutput > 0);
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
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_SETMETA.equals(command)) {
            setUseMeta(META_MATCH.equals(params.get(ChoiceLabel.PARAM_CHOICE)));
            return true;
        } else if (CMD_SETOREDICT.equals(command)) {
            setOreDict(OREDICT_USE.equals(params.get(ChoiceLabel.PARAM_CHOICE)));
            return true;
        } else if (CMD_SETSLOT.equals(command)) {
            int slot;
            try {
                slot = Integer.parseInt(params.get(TextField.PARAM_TEXT));
            } catch (NumberFormatException e) {
                slot = 0;
            }
            setSlot(slot);
            return true;
        } else if (CMD_SETAMOUNT.equals(command)) {
            int amount;
            try {
                amount = Integer.parseInt(params.get(TextField.PARAM_TEXT));
            } catch (NumberFormatException e) {
                amount = 1;
            }
            setAmount(amount);
            return true;
        }
        return false;
    }

}