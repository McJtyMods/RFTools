package mcjty.rftools.blocks.screens.modules;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.data.IModuleData;
import mcjty.rftools.api.screens.IScreenDataHelper;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemStackScreenModule implements IScreenModule<ItemStackScreenModule.ModuleDataStacks> {
    private int slot1 = -1;
    private int slot2 = -1;
    private int slot3 = -1;
    private int slot4 = -1;
    protected int dim = 0;
    protected BlockPos coordinate = BlockPosTools.INVALID;


    public static class ModuleDataStacks implements IModuleData {

        public static final String ID = RFTools.MODID + ":itemStacks";

        private final ItemStack[] stacks = new ItemStack[4];

        @Override
        public String getId() {
            return ID;
        }

        public ModuleDataStacks(ItemStack stack1, ItemStack stack2, ItemStack stack3, ItemStack stack4) {
            this.stacks[0] = stack1;
            this.stacks[1] = stack2;
            this.stacks[2] = stack3;
            this.stacks[3] = stack4;
        }

        public ModuleDataStacks(ByteBuf buf) {
            for (int i = 0 ; i < 4 ; i++) {
                if (buf.readBoolean()) {
                    stacks[i] = NetworkTools.readItemStack(buf);
                } else {
                    stacks[i] = null;
                }
            }
        }

        public ItemStack getStack(int idx) {
            return stacks[idx];
        }

        @Override
        public void writeToBuf(ByteBuf buf) {
            writeStack(buf, stacks[0]);
            writeStack(buf, stacks[1]);
            writeStack(buf, stacks[2]);
            writeStack(buf, stacks[3]);
        }

        private void writeStack(ByteBuf buf, ItemStack stack) {
            if (stack != null) {
                buf.writeBoolean(true);
                NetworkTools.writeItemStack(buf, stack);
            } else {
                buf.writeBoolean(false);
            }
        }
    }

    @Override
    public ModuleDataStacks getData(IScreenDataHelper helper, World worldObj, long millis) {
        World world = DimensionManager.getWorld(dim);
        if (world == null) {
            return null;
        }

        if (!world.getChunkProvider().chunkExists(coordinate.getX() >> 4, coordinate.getZ() >> 4)) {
            return null;
        }

        TileEntity te = world.getTileEntity(coordinate);
        if (te == null) {
            return null;
        }

        ItemStack stack1;
        ItemStack stack2;
        ItemStack stack3;
        ItemStack stack4;
        if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            stack1 = getItemStack(itemHandler, slot1);
            stack2 = getItemStack(itemHandler, slot2);
            stack3 = getItemStack(itemHandler, slot3);
            stack4 = getItemStack(itemHandler, slot4);
            return new ModuleDataStacks(stack1, stack2, stack3, stack4);
        } else if (te instanceof IInventory) {
            IInventory inventory = (IInventory) te;
            stack1 = getItemStack(inventory, slot1);
            stack2 = getItemStack(inventory, slot2);
            stack3 = getItemStack(inventory, slot3);
            stack4 = getItemStack(inventory, slot4);
            return new ModuleDataStacks(stack1, stack2, stack3, stack4);
        } else {
            return null;
        }
    }

    private ItemStack getItemStack(IInventory inventory, int slot) {
        if (slot == -1) {
            return null;
        }
        if (slot < inventory.getSizeInventory()) {
//            if (RFTools.instance.mfr && MFRCompatibility.isExtendedStorage(inventory)) {
//                return MFRCompatibility.getContents(inventory);
//            } else if (RFTools.instance.jabba && MFRCompatibility.isExtendedStorage(inventory)) {
//                return MFRCompatibility.getContents(inventory);
//            }
            return inventory.getStackInSlot(slot);
        } else {
            return null;
        }
    }

    private ItemStack getItemStack(IItemHandler itemHandler, int slot) {
        if (slot == -1) {
            return null;
        }
        if (slot < itemHandler.getSlots()) {
//            if (RFTools.instance.mfr && MFRCompatibility.isExtendedStorage(inventory)) {
//                return MFRCompatibility.getContents(inventory);
//            } else if (RFTools.instance.jabba && MFRCompatibility.isExtendedStorage(inventory)) {
//                return MFRCompatibility.getContents(inventory);
//            }
            return itemHandler.getStackInSlot(slot);
        } else {
            return null;
        }
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            setupCoordinateFromNBT(tagCompound, dim, x, y, z);
            if (tagCompound.hasKey("slot1")) {
                slot1 = tagCompound.getInteger("slot1");
            }
            if (tagCompound.hasKey("slot2")) {
                slot2 = tagCompound.getInteger("slot2");
            }
            if (tagCompound.hasKey("slot3")) {
                slot3 = tagCompound.getInteger("slot3");
            }
            if (tagCompound.hasKey("slot4")) {
                slot4 = tagCompound.getInteger("slot4");
            }
        }
    }

    protected void setupCoordinateFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        coordinate = BlockPosTools.INVALID;
        if (tagCompound.hasKey("monitorx")) {
            this.dim = tagCompound.getInteger("dim");
            if (dim == this.dim) {
                BlockPos c = new BlockPos(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
                int dx = Math.abs(c.getX() - x);
                int dy = Math.abs(c.getY() - y);
                int dz = Math.abs(c.getZ() - z);
                if (dx <= 64 && dy <= 64 && dz <= 64) {
                    coordinate = c;
                }
            }
        }
    }

    @Override
    public int getRfPerTick() {
        return ScreenConfiguration.ITEMSTACK_RFPERTICK;
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }
}
