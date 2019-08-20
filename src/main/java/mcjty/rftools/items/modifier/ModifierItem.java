package mcjty.rftools.items.modifier;

import mcjty.lib.varia.ItemStackList;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModifierItem extends GenericRFToolsItem {

    public ModifierItem() {
        super("modifier_module");
        setMaxStackSize(1);
    }

    private static ListNBT getOpList(ItemStack item) {
        if (!item.isEmpty() && item.getItem() == ModItems.modifierItem) {
            if (!item.hasTagCompound()) {
                item.setTagCompound(new CompoundNBT());
            }
            CompoundNBT tag = item.getTag();
            if (tag.hasKey("ops")) {
                return tag.getTagList("ops", Constants.NBT.TAG_COMPOUND);
            } else {
                ListNBT taglist = new ListNBT();
                tag.setTag("ops", taglist);
                return taglist;
            }
        }
        return null;
    }

    public static void performCommand(PlayerEntity player, ItemStack stack, ModifierCommand cmd, int index, ModifierFilterType type, ModifierFilterOperation op) {
        stack = stack.copy();
        switch (cmd) {
            case ADD:
                addOp(stack, type, op);
                break;
            case DEL:
                delOp(stack, index);
                break;
            case UP:
                upOp(stack, index);
                break;
            case DOWN:
                downOp(stack, index);
                break;
        }
        player.setHeldItem(Hand.MAIN_HAND, stack);
        player.openContainer.detectAndSendChanges();
    }

    private static ListNBT getTagList(List<ModifierEntry> modifiers) {
        ListNBT taglist = new ListNBT();
        for (ModifierEntry modifier : modifiers) {
            CompoundNBT tag = new CompoundNBT();

            if (!modifier.getIn().isEmpty()) {
                CompoundNBT tc = new CompoundNBT();
                modifier.getIn().writeToNBT(tc);
                tag.setTag("in", tc);
            }
            if (!modifier.getOut().isEmpty()) {
                CompoundNBT tc = new CompoundNBT();
                modifier.getOut().writeToNBT(tc);
                tag.setTag("out", tc);
            }

            tag.setString("type", modifier.getType().getCode());
            tag.setString("op", modifier.getOp().getCode());

            taglist.appendTag(tag);

        }

        return taglist;
    }

    private static void updateModifiers(ItemStack stack, List<ModifierEntry> modifiers) {
        ListNBT tagList = getTagList(modifiers);
        stack.getTag().setTag("ops", tagList);
    }

    public static ItemStackList getItemStacks(@Nullable CompoundNBT tagCompound) {
        ItemStackList stacks = ItemStackList.create(ModifierContainer.COUNT_SLOTS);
        if (tagCompound != null) {
            ListNBT bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < bufferTagList.tagCount(); i++) {
                CompoundNBT CompoundNBT = bufferTagList.getCompoundTagAt(i);
                stacks.set(i, new ItemStack(CompoundNBT));
            }
        }
        return stacks;
    }

    private static void upOp(ItemStack stack, int index) {
        List<ModifierEntry> modifiers = getModifiers(stack);
        ModifierEntry entry = modifiers.get(index);
        modifiers.remove(index);
        modifiers.add(index-1, entry);
        updateModifiers(stack, modifiers);
    }

    private static void downOp(ItemStack stack, int index) {
        List<ModifierEntry> modifiers = getModifiers(stack);
        ModifierEntry entry = modifiers.get(index);
        modifiers.remove(index);
        modifiers.add(index+1, entry);
        updateModifiers(stack, modifiers);
    }

    private static void addOp(ItemStack stack, ModifierFilterType type, ModifierFilterOperation op) {
        List<ModifierEntry> modifiers = getModifiers(stack);
        CompoundNBT tagCompound = stack.getTag();
        ItemStackList stacks = getItemStacks(tagCompound);
        ItemStack stackIn = stacks.get(ModifierContainer.SLOT_FILTER);
        ItemStack stackOut = stacks.get(ModifierContainer.SLOT_REPLACEMENT);
        modifiers.add(new ModifierEntry(stackIn, stackOut, type, op));
        stacks.set(ModifierContainer.SLOT_FILTER, ItemStack.EMPTY);
        stacks.set(ModifierContainer.SLOT_REPLACEMENT, ItemStack.EMPTY);
        ModifierInventory.convertItemsToNBT(tagCompound, stacks);
        updateModifiers(stack, modifiers);
    }

    private static void delOp(ItemStack stack, int index) {
        List<ModifierEntry> modifiers = getModifiers(stack);
        ModifierEntry entry = modifiers.get(index);
        ItemStack in = entry.getIn();
        ItemStack out = entry.getOut();
        CompoundNBT tagCompound = stack.getTag();
        ItemStackList stacks = getItemStacks(tagCompound);
        if (!in.isEmpty() && !stacks.get(ModifierContainer.SLOT_FILTER).isEmpty()) {
            // Something is in the way
            return;
        }
        if (!out.isEmpty() && !stacks.get(ModifierContainer.SLOT_REPLACEMENT).isEmpty()) {
            // Something is in the way
            return;
        }
        if (!in.isEmpty()) {
            stacks.set(ModifierContainer.SLOT_FILTER, in.copy());
        }
        if (!out.isEmpty()) {
            stacks.set(ModifierContainer.SLOT_REPLACEMENT, out.copy());
        }
        ModifierInventory.convertItemsToNBT(tagCompound, stacks);
        modifiers.remove(index);
        updateModifiers(stack, modifiers);
    }

    public static List<ModifierEntry> getModifiers(ItemStack item) {
        List<ModifierEntry> modifiers = new ArrayList<>();
        ListNBT taglist = getOpList(item);
        if (taglist == null) {
            return Collections.emptyList();
        }
        for (int i = 0 ; i < taglist.tagCount() ; i++) {
            CompoundNBT compound = taglist.getCompoundTagAt(i);
            ItemStack stackIn = new ItemStack(compound.getCompoundTag("in"));
            ItemStack stackOut = new ItemStack(compound.getCompoundTag("out"));
            ModifierFilterType type = ModifierFilterType.getByCode(compound.getString("type"));
            ModifierFilterOperation op = ModifierFilterOperation.getByCode(compound.getString("op"));
            modifiers.add(new ModifierEntry(stackIn, stackOut, type, op));
        }
        return modifiers;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This module can be used by the area scanner to");
            list.add(TextFormatting.WHITE + "modify the scanned output");
        } else {
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            player.openGui(RFTools.instance, GuiProxy.GUI_MODIFIER_MODULE, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }
}
