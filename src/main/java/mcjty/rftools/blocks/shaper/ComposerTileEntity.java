package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.shapes.Shape;
import mcjty.rftools.shapes.ShapeModifier;
import mcjty.rftools.shapes.ShapeOperation;
import mcjty.rftools.shapes.ShapeRotation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static mcjty.rftools.blocks.builder.BuilderSetup.TYPE_COMPOSER;

public class ComposerTileEntity extends GenericTileEntity implements ITickableTileEntity {

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
    private LazyOptional<NoDirectionItemHander> itemHandler = LazyOptional.of(this::createItemHandler);
    private ShapeModifier modifiers[] = new ShapeModifier[SLOT_COUNT];

    public ComposerTileEntity() {
        super(TYPE_COMPOSER);
        for (int i = 0; i < modifiers.length ; i++) {
            modifiers[i] = new ShapeModifier(ShapeOperation.UNION, false, ShapeRotation.NONE);
        }
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            itemHandler.ifPresent(h -> {
                ItemStack output = h.getStackInSlot(SLOT_OUT);
                if (!output.isEmpty()) {
                    ListNBT list = new ListNBT();
                    for (int i = SLOT_TABS; i < SLOT_TABS + SLOT_COUNT; i++) {
                        ItemStack item = h.getStackInSlot(i);
                        if (!item.isEmpty()) {
                            if (item.hasTag()) {
                                CompoundNBT copy = item.getTag().copy();
                                ShapeModifier modifier = modifiers[i - 1];
                                ShapeCardItem.setModifier(copy, modifier);
                                ItemStack materialGhost = h.getStackInSlot(i + SLOT_COUNT);
                                ShapeCardItem.setGhostMaterial(copy, materialGhost);
                                list.add(copy);
                            }
                        }
                    }
                    ShapeCardItem.setChildren(output, list);
                    if (!ShapeCardItem.getShape(output).isComposition()) {
                        ShapeCardItem.setShape(output, Shape.SHAPE_COMPOSITION, true);
                    }
                }
            });
        }
    }

    public ShapeModifier[] getModifiers() {
        return modifiers;
    }

    public void setModifiers(ShapeModifier[] modifiers) {
        this.modifiers = modifiers;
        markDirtyClient();
    }

    // @todo 1.14 loot tables
    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        ListNBT list = tagCompound.getList("ops", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < list.size() ; i++) {
            CompoundNBT tag = list.getCompound(i);
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

    // @todo 1.14 loot tables
    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        ListNBT list = new ListNBT();
        for (int i = 0; i < SLOT_COUNT ; i++) {
            CompoundNBT tc = new CompoundNBT();
            ShapeModifier mod = modifiers[i];
            tc.putString("mod_op", mod.getOperation().getCode());
            tc.putBoolean("mod_flipy", mod.isFlipY());
            tc.putString("mod_rot", mod.getRotation().getCode());
            list.add(tc);
        }
        tagCompound.put("ops", list);
        return tagCompound;
    }

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(ComposerTileEntity.this, CONTAINER_FACTORY, SLOT_COUNT*2 + 1) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() == BuilderSetup.shapeCardItem;
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
//        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
//            return screenHandler.cast();
//        }
        return super.getCapability(cap, facing);
    }
}
