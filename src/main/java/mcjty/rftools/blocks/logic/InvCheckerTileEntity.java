package mcjty.rftools.blocks.logic;

import mcjty.lib.container.InventoryHelper;
import mcjty.lib.network.Argument;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Map;

public class InvCheckerTileEntity extends LogicTileEntity implements ITickable {

    public static final String CMD_SETAMOUNT = "setCounter";
    public static final String CMD_SETSLOT = "setSlot";

    private int amount = 1;
    private int slot = 0;
    private boolean redstoneOut = false;

    private int checkCounter = 0;


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

        EnumFacing inputSide = getFacing().getInputSide();
        TileEntity te = worldObj.getTileEntity(getPos().offset(inputSide));
        if (InventoryHelper.isInventory(te)) {
            int nr = 0;
            if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                IItemHandler capability = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                ItemStack stack = capability.getStackInSlot(slot);
                nr = stack == null ? 0 : stack.stackSize;
            } else if (te instanceof IInventory) {
                IInventory inventory = (IInventory) te;
                ItemStack stack = inventory.getStackInSlot(slot);
                nr = stack == null ? 0 : stack.stackSize;
            }
            newout = nr >= amount;
        }

        if (newout != redstoneOut) {
            markDirty();
            redstoneOut = newout;
            IBlockState state = worldObj.getBlockState(getPos());
            worldObj.setBlockState(getPos(), state.withProperty(LogicSlabBlock.OUTPUTPOWER, redstoneOut), 2);
            worldObj.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
            worldObj.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        redstoneOut = tagCompound.getBoolean("rs");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        amount = tagCompound.getInteger("amount");
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
        tagCompound.setInteger("amount", amount);
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
        }
        return false;
    }
}
