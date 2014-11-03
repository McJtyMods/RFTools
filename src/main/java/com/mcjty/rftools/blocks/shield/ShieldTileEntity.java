package com.mcjty.rftools.blocks.shield;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.entity.SyncedValueList;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShieldTileEntity extends GenericEnergyHandlerTileEntity implements IInventory {

    public static final String CMD_SHIELDVISMODE = "shieldVisMode";
    public static final String CMD_APPLYCAMO = "applyCamo";

    public static int MAXENERGY = 100000;
    public static int RECEIVEPERTICK = 1000;

    // Maximum size of a shield in blocks.
    public static int maxShieldSize = 100;

    public static final int META_INVISIBLE = 1;
    public static final int META_SOLID = 2;

    // If true the shield is currently made.
    private boolean shieldComposed = false;

    private ShieldRenderingMode shieldRenderingMode = ShieldRenderingMode.MODE_SHIELD;

    private SyncedValueList<Coordinate> shieldBlocks = new SyncedValueList<Coordinate>() {
        @Override
        public Coordinate readElementFromNBT(NBTTagCompound tagCompound) {
            return Coordinate.readFromNBT(tagCompound, "c");
        }

        @Override
        public NBTTagCompound writeElementToNBT(Coordinate element) {
            return Coordinate.writeToNBT(element);
        }
    };

    private ItemStack stacks[] = new ItemStack[ShieldContainerFactory.BUFFER_SIZE];

    public ShieldTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
        registerSyncedObject(shieldBlocks);
    }

    public ShieldRenderingMode getShieldRenderingMode() {
        return shieldRenderingMode;
    }

    private void applyCamo() {
        int[] camoId = calculateCamoId();
        Block block = calculateShieldBlock();
        int meta = calculateShieldMeta();
        for (Coordinate c : shieldBlocks) {
            TileEntity te = worldObj.getTileEntity(c.getX(), c.getY(), c.getZ());
            if (te instanceof SolidShieldTileEntity) {
                worldObj.setBlock(c.getX(), c.getY(), c.getZ(), block, meta, 2);
                ((SolidShieldTileEntity)te).setCamoBlock(camoId[0], camoId[1]);
            }
        }
    }

    public void setShieldRenderingMode(ShieldRenderingMode shieldRenderingMode) {
        this.shieldRenderingMode = shieldRenderingMode;

        // Update the metadata for all the affected shield blocks.
        if (shieldComposed) {
            int[] camoId = calculateCamoId();
            int meta = calculateShieldMeta();
            Block block = calculateShieldBlock();
            for (Coordinate c : shieldBlocks) {
                worldObj.setBlock(c.getX(), c.getY(), c.getZ(), block, meta, 2);
                TileEntity te = worldObj.getTileEntity(c.getX(), c.getY(), c.getZ());
                if (te instanceof SolidShieldTileEntity) {
                    ((SolidShieldTileEntity)te).setCamoBlock(camoId[0], camoId[1]);
                }
            }
        }

        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    private int[] calculateCamoId() {
        ItemStack stack = stacks[0];
        int camoId = -1;
        int meta = 0;
        if (stack != null && stack.getItem() != null) {
            Block block = Block.getBlockFromItem(stack.getItem());
            camoId = block.getIdFromBlock(block);
            meta = 0;       // @@@ @Todo not right! Mycelium instead of grass!
        }
        return new int[] { camoId, meta };
    }

    private Block calculateShieldBlock() {
        if (ShieldRenderingMode.MODE_INVISIBLE.equals(shieldRenderingMode)) {
            return ModBlocks.invisibleShieldBlock;
        }
        if (ShieldRenderingMode.MODE_SHIELD.equals(shieldRenderingMode)) {
            return ModBlocks.visibleShieldBlock;
        }
        return ModBlocks.solidShieldBlock;
    }

    private int calculateShieldMeta() {
        if (ShieldRenderingMode.MODE_INVISIBLE.equals(shieldRenderingMode)) {
            return META_INVISIBLE;
        }
        if (ShieldRenderingMode.MODE_SHIELD.equals(shieldRenderingMode)) {
            return 0;
        }
        return META_SOLID;
    }

    public boolean isShieldComposed() {
        return shieldComposed;
    }

    public List<Coordinate> getShieldBlocks() {
        return shieldBlocks;
    }

    public void composeDecomposeShield() {
        if (shieldComposed) {
            // Shield is already composed. Break it into glass again.
            decomposeShield();
        } else {
            // Shield is not composed. Find all nearby glass blocks and form a shield.
            composeShield();
        }
    }

    public void composeShield() {
        int[] camoId = calculateCamoId();
        int meta = calculateShieldMeta();
        Block block = calculateShieldBlock();

        Set<Coordinate> coordinateSet = new HashSet<Coordinate>();
        findGlassBlocks(coordinateSet, xCoord, yCoord, zCoord, block, meta, camoId);
        shieldBlocks.clear();
        for (Coordinate c : coordinateSet) {
            shieldBlocks.add(c);
        }
        shieldComposed = true;
        markDirty();
        notifyBlockUpdate();
    }

    public void decomposeShield() {
        for (Coordinate c : shieldBlocks) {
            Block block = worldObj.getBlock(c.getX(), c.getY(), c.getZ());
            if (block instanceof AbstractShieldBlock) {
                worldObj.setBlock(c.getX(), c.getY(), c.getZ(), Blocks.glass, 0, 2);
            }
        }
        shieldComposed = false;
        shieldBlocks.clear();
        markDirty();
        notifyBlockUpdate();
    }

    private void findGlassBlocks(Set<Coordinate> coordinateSet, int x, int y, int z, Block block, int meta, int[] camoId) {
        if (coordinateSet.size() >= maxShieldSize) {
            return;
        }
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            int xx = x + dir.offsetX;
            int yy = y + dir.offsetY;
            int zz = z + dir.offsetZ;
            if (yy >= 0 && yy < worldObj.getHeight()) {
                Coordinate c = new Coordinate(xx, yy, zz);
                if (!coordinateSet.contains(c)) {
                    if (Blocks.glass.equals(worldObj.getBlock(xx, yy, zz))) {
                        coordinateSet.add(c);
                        worldObj.setBlock(c.getX(), c.getY(), c.getZ(), block, meta, 2);
                        TileEntity te = worldObj.getTileEntity(c.getX(), c.getY(), c.getZ());
                        if (te instanceof SolidShieldTileEntity) {
                            ((SolidShieldTileEntity)te).setCamoBlock(camoId[0], camoId[1]);
                        }
                        findGlassBlocks(coordinateSet, xx, yy, zz, block, meta, camoId);
                    }
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        shieldComposed = tagCompound.getBoolean("composed");
        shieldBlocks.readFromNBT(tagCompound, "coordinates");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
        int m = tagCompound.getInteger("visMode");
        shieldRenderingMode = ShieldRenderingMode.values()[m];
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            stacks[i+ShieldContainerFactory.SLOT_BUFFER] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("composed", shieldComposed);
        shieldBlocks.writeToNBT(tagCompound, "coordinates");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
        tagCompound.setInteger("visMode", shieldRenderingMode.ordinal());
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = ShieldContainerFactory.SLOT_BUFFER ; i < stacks.length ; i++) {
            ItemStack stack = stacks[i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_SHIELDVISMODE.equals(command)) {
            String m = args.get("mode").getString();
            setShieldRenderingMode(ShieldRenderingMode.getMode(m));
            return true;
        } else if (CMD_APPLYCAMO.equals(command)) {
            applyCamo();
            return true;
        }
        return false;
    }
    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return stacks[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (stacks[index] != null) {
            if (stacks[index].stackSize <= amount) {
                ItemStack old = stacks[index];
                stacks[index] = null;
                markDirty();
                return old;
            }
            ItemStack its = stacks[index].splitStack(amount);
            if (stacks[index].stackSize == 0) {
                stacks[index] = null;
            }
            markDirty();
            return its;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        stacks[index] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "Shield Inventory";
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
}
