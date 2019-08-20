package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.shapes.Shape;
import mcjty.rftools.shapes.ShapeModifier;
import mcjty.rftools.shapes.ShapeOperation;
import mcjty.rftools.shapes.ShapeRotation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.util.Constants;

public class ComposerTileEntity extends GenericTileEntity implements DefaultSidedInventory, ITickable {

    public static final int SLOT_COUNT = 9;
    public static final int SLOT_OUT = 0;
    public static final int SLOT_TABS = 1;
    public static final int SLOT_GHOSTS = SLOT_TABS + SLOT_COUNT;
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlot(new SlotDefinition(SlotType.SLOT_SPECIFICITEM,
                    new ItemStack(BuilderSetup.shapeCardItem)), ContainerFactory.CONTAINER_CONTAINER, SLOT_OUT, 18, 200);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM,
                            new ItemStack(BuilderSetup.shapeCardItem)),
                    ContainerFactory.CONTAINER_CONTAINER, SLOT_TABS, 18, 7, 1, 18, SLOT_COUNT, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_GHOST),
                    ContainerFactory.CONTAINER_CONTAINER, SLOT_GHOSTS, 36, 7, 1, 18, SLOT_COUNT, 18);
            layoutPlayerInventorySlots(85, 142);
        }
    };
    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, SLOT_COUNT*2 + 1);
    private ShapeModifier modifiers[] = new ShapeModifier[SLOT_COUNT];

    public ComposerTileEntity() {
        for (int i = 0; i < modifiers.length ; i++) {
            modifiers[i] = new ShapeModifier(ShapeOperation.UNION, false, ShapeRotation.NONE);
        }
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            ItemStack output = getStackInSlot(SLOT_OUT);
            if (!output.isEmpty()) {
                NBTTagList list = new NBTTagList();
                for (int i = SLOT_TABS; i < SLOT_TABS + SLOT_COUNT; i++) {
                    ItemStack item = getStackInSlot(i);
                    if (!item.isEmpty()) {
                        if (item.hasTagCompound()) {
                            CompoundNBT copy = item.getTag().copy();
                            ShapeModifier modifier = modifiers[i - 1];
                            ShapeCardItem.setModifier(copy, modifier);
                            ItemStack materialGhost = getStackInSlot(i + SLOT_COUNT);
                            ShapeCardItem.setGhostMaterial(copy, materialGhost);
                            list.appendTag(copy);
                        }
                    }
                }
                ShapeCardItem.setChildren(output, list);
                if (!ShapeCardItem.getShape(output).isComposition()) {
                    ShapeCardItem.setShape(output, Shape.SHAPE_COMPOSITION, true);
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
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return canPlayerAccess(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return stack.getItem() == BuilderSetup.shapeCardItem;
    }

    @Override
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        NBTTagList list = tagCompound.getTagList("ops", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < list.tagCount() ; i++) {
            CompoundNBT tag = list.getCompoundTagAt(i);
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
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < SLOT_COUNT ; i++) {
            CompoundNBT tc = new CompoundNBT();
            ShapeModifier mod = modifiers[i];
            tc.setString("mod_op", mod.getOperation().getCode());
            tc.setBoolean("mod_flipy", mod.isFlipY());
            tc.setString("mod_rot", mod.getRotation().getCode());
            list.appendTag(tc);
        }
        tagCompound.setTag("ops", list);
    }
}
