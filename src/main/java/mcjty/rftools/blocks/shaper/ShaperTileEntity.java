package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.items.builder.Shape;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.items.builder.ShapeOperation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.util.Constants;

public class ShaperTileEntity extends GenericTileEntity implements DefaultSidedInventory, ITickable {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ShaperContainer.factory, ShaperContainer.SLOT_COUNT + 1);
    private ShapeOperation operations[] = new ShapeOperation[ShaperContainer.SLOT_COUNT];

    public ShaperTileEntity() {
        for (int i = 0 ; i < operations.length ; i++) {
            operations[i] = ShapeOperation.UNION;
        }
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            ItemStack output = getStackInSlot(ShaperContainer.SLOT_OUT);
            if (ItemStackTools.isValid(output)) {
                NBTTagList list = new NBTTagList();
                for (int i = ShaperContainer.SLOT_TABS; i < ShaperContainer.SLOT_TABS + ShaperContainer.SLOT_COUNT; i++) {
                    ItemStack item = getStackInSlot(i);
                    if (ItemStackTools.isValid(item)) {
                        if (item.hasTagCompound()) {
                            NBTTagCompound copy = item.getTagCompound().copy();
                            copy.setString("op", operations[i-1].getCode());
                            list.appendTag(copy);
                        }
                    }
                }
                if (!output.hasTagCompound()) {
                    output.setTagCompound(new NBTTagCompound());
                }
                output.getTagCompound().setTag("children", list);
                if (!ShapeCardItem.getShape(output).isCustom()) {
                    ShapeCardItem.setShape(output, Shape.SHAPE_CUSTOM);
                }
            }
        }
    }

    public ShapeOperation[] getOperations() {
        return operations;
    }

    public void setOperations(ShapeOperation[] operations) {
        this.operations = operations;
        markDirtyClient();
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isUsable(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return stack.getItem() == BuilderSetup.shapeCardItem;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        NBTTagList list = tagCompound.getTagList("ops", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < list.tagCount() ; i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            String opcode = tag.getString("op");
            operations[i] = ShapeOperation.getByName(opcode);
        }
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        NBTTagList list = new NBTTagList();
        for (int i = 0 ; i < ShaperContainer.SLOT_COUNT ; i++) {
            NBTTagCompound tc = new NBTTagCompound();
            tc.setString("op", operations[i].getCode());
            list.appendTag(tc);
        }
        tagCompound.setTag("ops", list);
    }
}
