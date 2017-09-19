package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.items.modifier.ModifierEntry;
import mcjty.rftools.items.modifier.ModifierFilterOperation;
import mcjty.rftools.items.modifier.ModifierItem;
import mcjty.rftools.items.storage.StorageFilterCache;
import mcjty.rftools.items.storage.StorageFilterItem;
import mcjty.rftools.shapes.Shape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScannerTileEntity extends GenericTileEntity implements DefaultSidedInventory {

    public static final String CMD_SCAN = "scan";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ScannerContainer.factory, 4);
    private StorageFilterCache filterCache = null;

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index == ScannerContainer.SLOT_FILTER) {
            filterCache = null;
        }
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index == ScannerContainer.SLOT_FILTER) {
            filterCache = null;
        }
        return getInventoryHelper().decrStackSize(index, count);
    }

    @Override
    public boolean isUsable(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    private void getFilterCache() {
        if (filterCache == null) {
            filterCache = StorageFilterItem.getCache(inventoryHelper.getStackInSlot(ScannerContainer.SLOT_FILTER));
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return stack.getItem() == BuilderSetup.shapeCardItem;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
    }

    private void scan(int offsetX, int offsetY, int offsetZ) {
        ItemStack cardIn = inventoryHelper.getStackInSlot(ScannerContainer.SLOT_IN);
        if (ItemStackTools.isValid(cardIn)) {
            ShapeCardItem.setOffset(cardIn, offsetX, offsetY, offsetZ);

            ItemStack cardOut = inventoryHelper.getStackInSlot(ScannerContainer.SLOT_OUT);
            if (ItemStackTools.isEmpty(cardOut)) {
                return;
            }
            if (!ShapeCardItem.getShape(cardOut).isScheme()) {
                ShapeCardItem.setShape(cardOut, Shape.SHAPE_SCHEME, true);
            }
            NBTTagCompound tagOut = cardOut.getTagCompound();
            NBTTagCompound tagIn = cardIn.getTagCompound();     // @todo use this
            BlockPos dim = ShapeCardItem.getDimension(cardIn);
            int dimX = dim.getX();
            int dimY = dim.getY();
            int dimZ = dim.getZ();
            ShapeCardItem.setDimension(cardOut, dimX, dimY, dimZ);
            scanArea(tagOut, getPos().add(offsetX, offsetY, offsetZ), dimX, dimY, dimZ);
        }
    }

    private IBlockState mapState(List<ModifierEntry> modifiers, Map<IBlockState, IBlockState> modifierMapping, BlockPos pos, IBlockState inState) {
        if (modifiers.isEmpty()) {
            return inState;
        }
        if (!modifierMapping.containsKey(inState)) {
            IBlockState outState = inState;
            boolean stop = false;
            for (ModifierEntry modifier : modifiers) {
                if (stop) {
                    break;
                }
                switch (modifier.getType()) {
                    case FILTER_SLOT: {
                        // @todo support item filter here
                        ItemStack inputItem = inState.getBlock().getItem(getWorld(), pos, inState);
                        // Empty input stack in modifier also matches
                        if (ItemStackTools.isEmpty(modifier.getIn()) || ItemStack.areItemsEqual(inputItem, modifier.getIn())) {
                            outState = getOutput(inState, modifier);
                            stop = true;
                        }
                        break;
                    }
                    case FILTER_ORE: {
                        ItemStack inputItem = inState.getBlock().getItem(getWorld(), pos, inState);
                        int[] oreIDs = OreDictionary.getOreIDs(inputItem);
                        for (int id : oreIDs) {
                            if (OreDictionary.getOreName(id).startsWith("ore")) {
                                outState = getOutput(inState, modifier);
                                stop = true;
                                break;
                            }
                        }
                        break;
                    }
                    case FILTER_LIQUID:
                        if (inState.getBlock() instanceof BlockLiquid) {
                            outState = getOutput(inState, modifier);
                            stop = true;
                        }
                        break;
                    case FILTER_TILEENTITY:
                        if (getWorld().getTileEntity(pos) != null) {
                            outState = getOutput(inState, modifier);
                            stop = true;
                        }
                        break;
                }
            }
            modifierMapping.put(inState, outState);
        }
        return modifierMapping.get(inState);
    }

    private IBlockState getOutput(IBlockState input, ModifierEntry modifier) {
        if (modifier.getOp() == ModifierFilterOperation.OPERATION_VOID) {
            return Blocks.AIR.getDefaultState();
        }
        ItemStack outputItem = modifier.getOut();
        if (ItemStackTools.isEmpty(outputItem)) {
            return input;
        } else {
            Block block = ForgeRegistries.BLOCKS.getValue(outputItem.getItem().getRegistryName());
            if (block == null) {
                return Blocks.AIR.getDefaultState();
            } else {
                return block.getStateFromMeta(outputItem.getMetadata());
            }
        }
    }

    private int allocPalette(List<IBlockState> materialPalette, Map<IBlockState, Integer> paletteIndex, IBlockState state) {
        if (paletteIndex.containsKey(state)) {
            return paletteIndex.get(state);
        }
        int idx = materialPalette.size();
        if (idx > 254) {
            // @todo overflow. How to handle gracefully?
            System.out.println("Scanner palette overflow!");
            return idx;
        }
        materialPalette.add(state);
        paletteIndex.put(state, idx);
        return idx;
    }

    private void scanArea(NBTTagCompound tagOut, BlockPos center, int dimX, int dimY, int dimZ) {
        ItemStack modifier = getStackInSlot(ScannerContainer.SLOT_MODIFIER);
        List<ModifierEntry> modifiers = ModifierItem.getModifiers(modifier);
        Map<IBlockState, IBlockState> modifierMapping = new HashMap<>();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BlockPos tl = new BlockPos(center.getX() - dimX/2, center.getY() - dimY/2, center.getZ() - dimZ/2);
        Integer prev = null;

        List<IBlockState> materialPalette = new ArrayList<>();
        Map<IBlockState, Integer> paletteIndex = new HashMap<>();

        int cnt = 0;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int y = tl.getY() ; y < tl.getY() + dimY ; y++) {
            for (int x = tl.getX() ; x < tl.getX() + dimX ; x++) {
                for (int z = tl.getZ() ; z < tl.getZ() + dimZ ; z++) {
                    mpos.setPos(x, y, z);
                    int c;
                    if (getWorld().isAirBlock(mpos)) {
                        c = 0;
                    } else {
                        IBlockState state = getWorld().getBlockState(mpos);
                        getFilterCache();
                        if (filterCache != null) {
                            ItemStack item = state.getBlock().getItem(getWorld(), mpos, state);
                            if (!filterCache.match(item)) {
                                state = null;
                            }
                        }
                        if (state != Blocks.AIR.getDefaultState()) {
                            state = mapState(modifiers, modifierMapping, mpos, state);
                        }
                        if (state != Blocks.AIR.getDefaultState()) {
                            c = allocPalette(materialPalette, paletteIndex, state);
                        } else {
                            c = 0;
                        }
                    }
                    if (prev == null) {
                        prev = c;
                        cnt = 1;
                    } else if (prev == c && cnt < 255) {
                        cnt++;
                    } else {
                        stream.write(cnt);
                        stream.write(prev);
                        prev = c;
                        cnt = 1;
                    }
                }
            }
        }
        if (prev != null) {
            stream.write(cnt);
            stream.write(prev);
        }
        ShapeCardItem.setData(tagOut, stream.toByteArray());
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SCAN.equals(command)) {
            scan(args.get("offsetX").getInteger(), args.get("offsetY").getInteger(), args.get("offsetZ").getInteger());
            return true;

        }
        return false;
    }

}
