package mcjty.rftools.items.storage;

import mcjty.rftools.RFTools;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class StorageFilterItem extends GenericRFToolsItem {

    public StorageFilterItem() {
        super("filter_module");
        setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            String blackListMode = tagCompound.getString("blacklistMode");
            String modeLine = "Mode " + ("Black".equals(blackListMode) ? "blacklist" : "whitelist");
            if (tagCompound.getBoolean("oredictMode")) {
                modeLine += ", Oredict";
            }
            if (tagCompound.getBoolean("damageMode")) {
                modeLine += ", Damage";
            }
            if (tagCompound.getBoolean("nbtMode")) {
                modeLine += ", NBT";
            }
            if (tagCompound.getBoolean("modMode")) {
                modeLine += ", Mod";
            }
            list.add(TextFormatting.BLUE + modeLine);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This filter module is for the Modular Storage block.");
            list.add(TextFormatting.WHITE + "This module can make sure the storage block only accepts");
            list.add(TextFormatting.WHITE + "certain types of items");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_STORAGE_FILTER, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public static StorageFilterCache getCache(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        return new StorageFilterCache(stack);
    }
}
