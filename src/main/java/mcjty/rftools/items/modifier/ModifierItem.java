package mcjty.rftools.items.modifier;

import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.GenericRFToolsItem;
import mcjty.rftools.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModifierItem extends GenericRFToolsItem {

    public ModifierItem() {
        super("modifier_module");
        setMaxStackSize(1);
    }

    private static NBTTagList getOpList(ItemStack item) {
        if (ItemStackTools.isValid(item) && item.getItem() == ModItems.modifierItem) {
            if (!item.hasTagCompound()) {
                item.setTagCompound(new NBTTagCompound());
            }
            NBTTagCompound tag = item.getTagCompound();
            if (tag.hasKey("ops")) {
                return tag.getTagList("ops", Constants.NBT.TAG_COMPOUND);
            } else {
                NBTTagList taglist = new NBTTagList();
                tag.setTag("ops", taglist);
                return taglist;
            }
        }
        return null;
    }

    public static List<ModifierEntry> getModifiers(ItemStack item) {
        List<ModifierEntry> modifiers = new ArrayList<>();
        NBTTagList taglist = getOpList(item);
        if (taglist == null) {
            return Collections.emptyList();
        }
        for (int i = 0 ; i < taglist.tagCount() ; i++) {
            NBTTagCompound compound = taglist.getCompoundTagAt(i);
            ItemStack stackIn = ItemStackTools.loadFromNBT(compound.getCompoundTag("in"));
            ItemStack stackOut = ItemStackTools.loadFromNBT(compound.getCompoundTag("out"));
            ModifierFilterType type = ModifierFilterType.getByCode(compound.getString("type"));
            ModifierFilterOperation op = ModifierFilterOperation.getByCode(compound.getString("op"));
            modifiers.add(new ModifierEntry(stackIn, stackOut, type, op));
        }
        return modifiers;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This module can be used by the area scanner to");
            list.add(TextFormatting.WHITE + "modify the scanned output");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }


    @Override
    protected ActionResult<ItemStack> clOnItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_MODIFIER_MODULE, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
