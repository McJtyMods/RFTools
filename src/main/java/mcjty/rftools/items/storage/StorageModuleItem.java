package mcjty.rftools.items.storage;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.Constants;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class StorageModuleItem extends Item {
    private final IIcon[] icons = new IIcon[3];

    public static final int MAXSIZE[] = new int[] { 100, 200, 300 };

    public StorageModuleItem() {
        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        for (int i = 0 ; i < 3 ; i++) {
            icons[i] = iconRegister.registerIcon(RFTools.MODID + ":storage/storageModule" + i);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        int max = MAXSIZE[itemStack.getItemDamage()];
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            int cnt = 0;
            for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
                NBTTagCompound tagAt = bufferTagList.getCompoundTagAt(i);
                if (ItemStack.loadItemStackFromNBT(tagAt) != null) {
                    cnt++;
                }
            }
            list.add(EnumChatFormatting.GREEN + "Contents: " + cnt + "/" + max + " stacks");
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This storage module is for the Modular Storage block.");
            list.add(EnumChatFormatting.WHITE + "This module supports " + max + " stacks");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public IIcon getIconFromDamage(int damage) {
        return icons[damage];
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        return super.getUnlocalizedName(itemStack) + itemStack.getItemDamage();
    }

    @Override
    public void getSubItems(Item item, CreativeTabs creativeTabs, List list) {
        for (int i = 0 ; i < 3 ; i++) {
            list.add(new ItemStack(ModularStorageSetup.storageModuleItem, 1, i));
        }
    }
}
