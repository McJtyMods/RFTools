package mcjty.rftools.items.storage;

import mcjty.rftools.RFTools;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class StorageFilterItem extends Item {

    public StorageFilterItem() {
        setMaxStackSize(1);
        setUnlocalizedName("filter_module");
        setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerItem(this, "filter_module");
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(RFTools.MODID + ":" + getUnlocalizedName().substring(5), "inventory"));
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
            list.add(EnumChatFormatting.BLUE + modeLine);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This filter module is for the Modular Storage block.");
            list.add(EnumChatFormatting.WHITE + "This module can make sure the storage block only accepts");
            list.add(EnumChatFormatting.WHITE + "certain types of items");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_STORAGE_FILTER, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
            return stack;
        }
        return stack;
    }

    public static StorageFilterCache getCache(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        return new StorageFilterCache(stack);
    }
}
