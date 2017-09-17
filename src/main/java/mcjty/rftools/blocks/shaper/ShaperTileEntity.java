package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.items.builder.*;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.util.Constants;

public class ShaperTileEntity extends GenericTileEntity implements DefaultSidedInventory, ITickable {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ShaperContainer.factory, ShaperContainer.SLOT_COUNT*2 + 1);
    private ShapeModifier modifiers[] = new ShapeModifier[ShaperContainer.SLOT_COUNT];

    public ShaperTileEntity() {
        for (int i = 0; i < modifiers.length ; i++) {
            modifiers[i] = new ShapeModifier(ShapeOperation.UNION, false, ShapeRotation.NONE);
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
                            ShapeModifier modifier = modifiers[i - 1];
                            copy.setString("mod_op", modifier.getOperation().getCode());
                            copy.setBoolean("mod_flipy", modifier.isFlipY());
                            copy.setString("mod_rot", modifier.getRotation().getCode());
                            ItemStack materialGhost = getStackInSlot(i + ShaperContainer.SLOT_COUNT);
                            if (ItemStackTools.isValid(materialGhost)) {
                                Block block = Block.getBlockFromItem(materialGhost.getItem());
                                if (block != null) {
                                    copy.setString("ghost_block", block.getRegistryName().toString());
                                    copy.setInteger("ghost_meta", materialGhost.getMetadata());
                                }
                            }
                            list.appendTag(copy);
                        }
                    }
                }
                if (!output.hasTagCompound()) {
                    output.setTagCompound(new NBTTagCompound());
                }
                output.getTagCompound().setTag("children", list);
                if (!ShapeCardItem.getShape(output).isCustom()) {
                    ShapeCardItem.setShape(output, Shape.SHAPE_CUSTOM, true);
                }
            }
        }
    }

    public ShapeModifier[] getModifiers() {
        return modifiers;
    }

    public void setModifiers(ShapeModifier[] modifiers) {
        this.modifiers = modifiers;
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
            String op = tag.getString("mod_op");
            boolean flipY = tag.getBoolean("mod_flipy");
            String rot = tag.getString("mod_rot");
            ShapeOperation operation = ShapeOperation.getByName(op);
            if (operation == null) {
                operation = ShapeOperation.UNION;
            }
            ShapeRotation rotation = ShapeRotation.getByName(rot);
            if (rotation == null) {
                rotation = ShapeRotation.NONE;
            }
            modifiers[i] = new ShapeModifier(operation, flipY, rotation);
        }
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        NBTTagList list = new NBTTagList();
        for (int i = 0 ; i < ShaperContainer.SLOT_COUNT ; i++) {
            NBTTagCompound tc = new NBTTagCompound();
            ShapeModifier mod = modifiers[i];
            tc.setString("mod_op", mod.getOperation().getCode());
            tc.setBoolean("mod_flipy", mod.isFlipY());
            tc.setString("mod_rot", mod.getRotation().getCode());
            list.appendTag(tc);
        }
        tagCompound.setTag("ops", list);
    }
}
