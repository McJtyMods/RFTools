package mcjty.rftools.items.storage;

import mcjty.lib.crafting.INBTPreservingIngredient;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.storage.*;
import mcjty.rftools.setup.GuiProxy;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class StorageModuleItem extends GenericRFToolsItem implements INBTPreservingIngredient {
    public static final int STORAGE_TIER1 = 0;
    public static final int STORAGE_TIER2 = 1;
    public static final int STORAGE_TIER3 = 2;
    public static final int STORAGE_REMOTE = 6;
    public static final int MAXSIZE[] = new int[] { 100, 200, 300, 0, 0, 0, -1 };

    public StorageModuleItem() {
        super("storage_module");
        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, STORAGE_TIER1, new ModelResourceLocation(RFTools.MODID + ":storage_module0", "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, STORAGE_TIER2, new ModelResourceLocation(RFTools.MODID + ":storage_module1", "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, STORAGE_TIER3, new ModelResourceLocation(RFTools.MODID + ":storage_module2", "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, STORAGE_REMOTE, new ModelResourceLocation(RFTools.MODID + ":storage_module_remote", "inventory"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            Logging.message(player, TextFormatting.YELLOW + "Place this module in a storage module tablet to access contents");
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    // Called from the Remote or Modular store TE's to update the stack size for this item while it is inside that TE.
    public static void updateStackSize(ItemStack stack, int numStacks) {
        if (stack.isEmpty()) {
            return;
        }
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
        }
        tagCompound.setInteger("count", numStacks);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        int max = MAXSIZE[itemStack.getItemDamage()];
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            addModuleInformation(list, max, tagCompound);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This storage module is for the Modular Storage block.");
            if (max == -1) {
                list.add(TextFormatting.WHITE + "This module supports a remote inventory.");
                list.add(TextFormatting.WHITE + "Link to another storage module in the remote storage block.");
            } else {
                list.add(TextFormatting.WHITE + "This module supports " + max + " stacks");
            }
        } else {
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }

    public static void addModuleInformation(List<String> list, int max, NBTTagCompound tagCompound) {
        if (max == -1) {
            // This is a remote storage module.
            if (tagCompound.hasKey("id")) {
                int id = tagCompound.getInteger("id");
                list.add(TextFormatting.GREEN + "Remote id: " + id);
            } else {
                list.add(TextFormatting.YELLOW + "Unlinked");
            }
        } else {
            int cnt = tagCompound.getInteger("count");
            if (tagCompound.hasKey("id")) {
                int id = tagCompound.getInteger("id");
                list.add(TextFormatting.GREEN + "Contents id: " + id);
            }
            list.add(TextFormatting.GREEN + "Contents: " + cnt + "/" + max + " stacks");
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        return super.getUnlocalizedName(itemStack) + itemStack.getItemDamage();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            for (int i = 0; i < 7; i++) {
                if (MAXSIZE[i] != 0) {
                    items.add(new ItemStack(ModularStorageSetup.storageModuleItem, 1, i));
                }
            }
        }
    }
}
