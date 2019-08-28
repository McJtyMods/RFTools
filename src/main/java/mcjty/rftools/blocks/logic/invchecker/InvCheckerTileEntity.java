package mcjty.rftools.blocks.logic.invchecker;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.tileentity.LogicTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.CapabilityTools;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import static mcjty.rftools.blocks.logic.LogicBlockSetup.TYPE_INVCHECKER;
import static mcjty.rftools.blocks.logic.invchecker.GuiInvChecker.META_MATCH;
import static mcjty.rftools.blocks.logic.invchecker.GuiInvChecker.OREDICT_USE;

public class InvCheckerTileEntity extends LogicTileEntity implements ITickableTileEntity {

    public static final String CMD_SETAMOUNT = "inv.setCounter";
    public static final String CMD_SETSLOT = "inv.setSlot";
    public static final String CMD_SETOREDICT = "inv.setOreDict";
    public static final String CMD_SETMETA = "inv.setUseMeta";

    public static final String CONTAINER_INVENTORY = "container";
    public static final int SLOT_ITEMMATCH = 0;
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_GHOST), ContainerFactory.CONTAINER_CONTAINER, SLOT_ITEMMATCH, 154, 24, 1, 18, 1, 18);
            layoutPlayerInventorySlots(10, 70);
        }
    };

    private int amount = 1;
    private int slot = 0;
    private boolean oreDict = false;
    private boolean useMeta = false;
    private IntOpenHashSet set1 = null;

    private int checkCounter = 0;

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, 1);


    public InvCheckerTileEntity() {
        super(TYPE_INVCHECKER);
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

    // @todo 1.14
//    @Override
//    public void setInventorySlotContents(int index, ItemStack stack) {
//        // Clear the oredict cache
//        set1 = null;
//        getInventoryHelper().setInventorySlotContents(getInventoryStackLimit(), index, stack);
//    }

    @Override
    public void tick() {
        if (world.isRemote) {
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

        Direction inputSide = getFacing(world.getBlockState(getPos())).getInputSide();
        BlockPos inputPos = getPos().offset(inputSide);
        TileEntity te = world.getTileEntity(inputPos);
        if (InventoryHelper.isInventory(te)) {
            ItemStack stack = CapabilityTools.getItemCapabilitySafe(te).map(h -> {
                if (slot >= 0 && slot < h.getSlots()) {
                    return h.getStackInSlot(slot);
                }
                return ItemStack.EMPTY;
            }).orElse(ItemStack.EMPTY);
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
                    // @todo 1.14
//                    if ((!useMeta) || matcher.getMetadata() == stack.getMetadata()) {
//                        nr = stack.getCount();
//                    }
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
//        if (set1 == null) {
//            int[] oreIDs1 = OreDictionary.getOreIDs(s1);
//            set1 = new TIntHashSet(oreIDs1);
//        }
//        if (set1.isEmpty()) {
//            // The first item is not an ore. In this case we do normal equality of item
//            return s1.getItem() == s2.getItem();
//        }
//
//        int[] oreIDs2 = OreDictionary.getOreIDs(s2);
//        if (oreIDs2.length == 0) {
//            // The first is an ore but this isn't. So we cannot match.
//            return false;
//        }
//        TIntSet set2 = new TIntHashSet(oreIDs2);
//        set2.retainAll(set1);
//        return !set2.isEmpty();
        // @todo 1.14
        return false;
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        powerOutput = tagCompound.getBoolean("rs") ? 15 : 0;
        readRestorableFromNBT(tagCompound);
    }

    // @todo 1.14 loot tables
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        readBufferFromNBT(tagCompound, inventoryHelper);
        amount = tagCompound.getInt("amount");
        slot = tagCompound.getInt("slot");
        oreDict = tagCompound.getBoolean("oredict");
        useMeta = tagCompound.getBoolean("useMeta");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.putBoolean("rs", powerOutput > 0);
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }

    // @todo 1.14 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.putInt("amount", amount);
        tagCompound.putInt("slot", slot);
        tagCompound.putBoolean("oredict", oreDict);
        tagCompound.putBoolean("useMeta", useMeta);
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
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

//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        boolean rc = checkOutput();
//        probeInfo.text(TextFormatting.GREEN + "Output: " + TextFormatting.WHITE + (rc ? "on" : "off"));
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.addWailaBody(itemStack, currenttip, accessor, config);
//    }


}