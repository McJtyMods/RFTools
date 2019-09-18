package mcjty.rftools.blocks.screens.modules;

import io.netty.buffer.ByteBuf;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.ItemStackList;
import mcjty.lib.varia.SoundTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IScreenDataHelper;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.api.screens.IScreenModuleUpdater;
import mcjty.rftools.api.screens.ITooltipInfo;
import mcjty.rftools.api.screens.data.IModuleData;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.Collections;
import java.util.List;

public class StorageControlScreenModule implements IScreenModule<StorageControlScreenModule.ModuleDataStacks>, ITooltipInfo,
        IScreenModuleUpdater {
    private ItemStackList stacks = ItemStackList.create(9);

    protected int dim = 0;
    protected BlockPos coordinate = BlockPosTools.INVALID;
    private boolean starred = false;
    private boolean oredict = false;
    private int dirty = -1;

    public static class ModuleDataStacks implements IModuleData {

        public static final String ID = RFTools.MODID + ":storage";

        private int[] amounts = null;

        @Override
        public String getId() {
            return ID;
        }

        public ModuleDataStacks(int... amountsIn) {
            amounts = amountsIn;
        }

        public ModuleDataStacks(ByteBuf buf) {
            int s = buf.readInt();
            amounts = new int[s];
            for (int i = 0; i < s; i++) {
                amounts[i] = buf.readInt();
            }
        }

        public int getAmount(int idx) {
            return amounts[idx];
        }

        @Override
        public void writeToBuf(ByteBuf buf) {
            buf.writeInt(amounts.length);
            for (int i : amounts) {
                buf.writeInt(i);
            }

        }
    }

    @Override
    public ModuleDataStacks getData(IScreenDataHelper helper, World worldObj, long millis) {
        // @todo 1.14
//        StorageScannerTileEntity scannerTileEntity = getStorageScanner(dim, coordinate);
//        if (scannerTileEntity == null) {
//            return null;
//        }
//        int[] amounts = new int[stacks.size()];
//        for (int i = 0; i < stacks.size(); i++) {
//            amounts[i] = scannerTileEntity.countItems(stacks.get(i), starred, oredict);
//        }
//        return new ModuleDataStacks(amounts);
        return null;
    }

    /* todo 1.14
    public static StorageScannerTileEntity getStorageScanner(int dim, BlockPos coordinate) {
        World world = DimensionManager.getWorld(dim);
        if (world == null) {
            return null;
        }

        if (!WorldTools.chunkLoaded(world, coordinate)) {
            return null;
        }

        TileEntity te = world.getTileEntity(coordinate);
        if (te == null) {
            return null;
        }

        if (!(te instanceof StorageScannerTileEntity)) {
            return null;
        }

        return (StorageScannerTileEntity) te;
    }
    */

    @Override
    public void setupFromNBT(CompoundNBT tagCompound, DimensionType dim, BlockPos pos) {
        if (tagCompound != null) {
            setupCoordinateFromNBT(tagCompound, dim, pos);
            for (int i = 0; i < stacks.size(); i++) {
                if (tagCompound.contains("stack" + i)) {
                    stacks.set(i, ItemStack.read(tagCompound.getCompound("stack" + i)));
                }
            }
        }
        // @todo 1.14
//        StorageScannerTileEntity te = getStorageScanner(dim, coordinate);
//        if (te != null) {
//            te.clearCachedCounts();
//        }
    }

    private int getHighlightedStack(int hitx, int hity) {
        int i = 0;
        for (int yy = 0; yy < 3; yy++) {
            int y = 7 + yy * 35;
            for (int xx = 0; xx < 3; xx++) {
                int x = xx * 40;

                boolean hilighted = hitx >= x + 8 && hitx <= x + 38 && hity >= y - 7 && hity <= y + 22;
                if (hilighted) {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    @Override
    public List<String> getInfo(World world, int x, int y) {
        // @todo 1.14
//        StorageScannerTileEntity te = getStorageScanner(dim, coordinate);
//        if (te != null) {
//            int i = getHighlightedStack(x, y);
//            if (i != -1 && !stacks.get(i).isEmpty()) {
//                return Collections.singletonList(TextFormatting.GREEN + "Item: " + TextFormatting.WHITE + stacks.get(i).getDisplayName());
//            }
//        }
        return Collections.emptyList();
    }

    protected void setupCoordinateFromNBT(CompoundNBT tagCompound, DimensionType dim, BlockPos pos) {
        coordinate = BlockPosTools.INVALID;
        starred = tagCompound.getBoolean("starred");
        oredict = tagCompound.getBoolean("oredict");
        if (tagCompound.contains("monitorx")) {
            if (tagCompound.contains("monitordim")) {
                this.dim = tagCompound.getInt("monitordim");
            } else {
                // Compatibility reasons
                this.dim = tagCompound.getInt("dim");
            }
            BlockPos c = new BlockPos(tagCompound.getInt("monitorx"), tagCompound.getInt("monitory"), tagCompound.getInt("monitorz"));
            int dx = Math.abs(c.getX() - pos.getX());
            int dy = Math.abs(c.getY() - pos.getY());
            int dz = Math.abs(c.getZ() - pos.getZ());
            coordinate = c;
        }
    }

    @Override
    public int getRfPerTick() {
        return ScreenConfiguration.STORAGE_CONTROL_RFPERTICK.get();
    }


    private boolean isShown(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        // @todo 1.14
//        for (ItemStack s : stacks) {
//            if (StorageScannerTileEntity.isItemEqual(stack, s, oredict)) {
//                return true;
//            }
//        }
        return false;
    }


    @Override
    public CompoundNBT update(CompoundNBT tagCompound, World world, PlayerEntity player) {
        if (dirty >= 0) {
            CompoundNBT newCompound = tagCompound.copy();
            CompoundNBT tc = new CompoundNBT();
            stacks.get(dirty).write(tc);
            newCompound.put("stack" + dirty, tc);
            if (player != null) {
                SoundTools.playSound(player.getEntityWorld(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                        player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), 1.0f, 1.0f);
            }
            dirty = -1;
            return newCompound;
        }
        return null;
    }

    @Override
    public void mouseClick(World world, int hitx, int hity, boolean clicked, PlayerEntity player) {
        if ((!clicked) || player == null) {
            return;
        }
        if (BlockPosTools.INVALID.equals(coordinate)) {
            player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "Module is not linked to storage scanner!"), false);
            return;
        }
        // @todo 1.14
//        StorageScannerTileEntity scannerTileEntity = getStorageScanner(dim, coordinate);
//        if (scannerTileEntity == null) {
//            return;
//        }
//        if (hitx >= 0) {
//            boolean insertStackActive = hitx >= 0 && hitx < 60 && hity > 98;
//            if (insertStackActive) {
//                if (isShown(player.getHeldItem(Hand.MAIN_HAND))) {
//                    ItemStack stack = scannerTileEntity.injectStackFromScreen(player.getHeldItem(Hand.MAIN_HAND), player);
//                    player.setHeldItem(Hand.MAIN_HAND, stack);
//                }
//                player.openContainer.detectAndSendChanges();
//                return;
//            }
//
//            boolean insertAllActive = hitx >= 60 && hity > 98;
//            if (insertAllActive) {
//                for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
//                    if (isShown(player.inventory.getStackInSlot(i))) {
//                        ItemStack stack = scannerTileEntity.injectStackFromScreen(player.inventory.getStackInSlot(i), player);
//                        player.inventory.setInventorySlotContents(i, stack);
//                    }
//                }
//                player.openContainer.detectAndSendChanges();
//                return;
//            }
//
//            int i = getHighlightedStack(hitx, hity);
//            if (i != -1) {
//                if (stacks.get(i).isEmpty()) {
//                    ItemStack heldItem = player.getHeldItemMainhand();
//                    if (!heldItem.isEmpty()) {
//                        ItemStack stack = heldItem.copy();
//                        stack.setCount(1);
//                        stacks.set(i, stack);
//                        dirty = i;
//                    }
//                } else {
//                    scannerTileEntity.giveToPlayerFromScreen(stacks.get(i), player.isSneaking(), player, oredict);
//                }
//            }
//        }
    }
}
