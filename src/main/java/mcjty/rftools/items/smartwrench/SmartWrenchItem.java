package mcjty.rftools.items.smartwrench;

import cofh.api.item.IToolHammer;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.RFTools;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

@Optional.InterfaceList({
        @Optional.Interface(iface = "cofh.api.item.IToolHammer", modid = "CoFHCore")})
public class SmartWrenchItem extends Item implements IToolHammer {

    public SmartWrenchItem() {
        setMaxStackSize(1);
    }

    @Override
    @Optional.Method(modid = "CoFHCore")
    public boolean isUsable(ItemStack item, EntityLivingBase user, int x, int y, int z) {
        SmartWrenchMode mode = getCurrentMode(item);
        System.out.println("mode = " + mode);
        return mode == SmartWrenchMode.MODE_WRENCH;
    }

    @Override
    @Optional.Method(modid = "CoFHCore")
    public void toolUsed(ItemStack item, EntityLivingBase user, int x, int y, int z) {
        System.out.println("SmartWrenchItem.toolUsed");
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            SmartWrenchMode mode = getCurrentMode(stack);
            if (mode == SmartWrenchMode.MODE_WRENCH) {
                mode = SmartWrenchMode.MODE_SELECT;
            } else {
                mode = SmartWrenchMode.MODE_WRENCH;
            }
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound == null) {
                tagCompound = new NBTTagCompound();
                stack.setTagCompound(tagCompound);
            }
            tagCompound.setString("mode", mode.getCode());
            RFTools.message(player, "Smart wrench is now in " + mode.getName() + " mode.");
        }
        return super.onItemRightClick(stack, world, player);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        System.out.println("onItemUse: world.isRemote = " + world.isRemote);
        return true;
    }

    @Override
    public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        SmartWrenchMode mode = getCurrentMode(itemStack);
        list.add(EnumChatFormatting.WHITE + "Right-click on air to change mode.");
        list.add(EnumChatFormatting.GREEN + "Mode: " + mode.getName());
        if (mode == SmartWrenchMode.MODE_WRENCH) {
            list.add(EnumChatFormatting.WHITE + "Use as a normal wrench:");
            list.add(EnumChatFormatting.WHITE + "    Sneak-right-click to pick up machines.");
            list.add(EnumChatFormatting.WHITE + "    Right-click to rotate machines.");
        } else if (mode == SmartWrenchMode.MODE_SELECT) {
            list.add(EnumChatFormatting.WHITE + "Use as a block selector:");
            list.add(EnumChatFormatting.WHITE + "    Sneak-right-click select master block.");
            list.add(EnumChatFormatting.WHITE + "    Right-click to associate blocks with master.");
        }
    }

    private SmartWrenchMode getCurrentMode(ItemStack itemStack) {
        SmartWrenchMode mode = SmartWrenchMode.MODE_WRENCH;
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            String modeString = tagCompound.getString("mode");
            if (modeString != null && !modeString.isEmpty()) {
                mode = SmartWrenchMode.getMode(modeString);
            }
        }
        return mode;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }
}