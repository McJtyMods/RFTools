package mcjty.rftools.items.storage;

import com.sun.istack.internal.NotNull;
import mcjty.lib.container.InventoryHelper;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.storage.ModularStorageTileEntity;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StorageFilterItem extends GenericRFToolsItem {

    public StorageFilterItem() {
        super("filter_module");
        setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
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
            list.add(TextFormatting.YELLOW + "Sneak-right click on an inventory to");
            list.add(TextFormatting.YELLOW + "configure the filter based on contents");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (playerIn.isSneaking()) {
            if (!worldIn.isRemote) {
                TileEntity te = worldIn.getTileEntity(pos);
                if (InventoryHelper.isInventory(te)) {
                    List<ItemStack> stacks = new ArrayList<>();
                    Set<ResourceLocation> registeredItems = new HashSet<>();
                    InventoryHelper.getItems(te, s -> true).forEach(s -> addItem(te, stacks, registeredItems, s));
                    if (!stack.hasTagCompound()) {
                        stack.setTagCompound(new NBTTagCompound());
                    }
                    StorageFilterInventory.convertItemsToNBT(stack.getTagCompound(),stacks.toArray(new ItemStack[stacks.size()]) );
                    playerIn.addChatComponentMessage(new TextComponentString(TextFormatting.GREEN + "Stored inventory contents in filter"));
                } else {
                    playerIn.addChatComponentMessage(new TextComponentString(TextFormatting.RED + "This is not an inventory"));
                }
            }
            return EnumActionResult.SUCCESS;
        }
        return super.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    private void addItem(TileEntity te, List<ItemStack> stacks, Set<ResourceLocation> registeredItems, @NotNull ItemStack s) {
        if (registeredItems.contains(s.getItem().getRegistryName())) {
            return;
        }
        if (te instanceof ModularStorageTileEntity) {
            if (s.getItem() instanceof StorageModuleItem || s.getItem() instanceof StorageFilterItem || s.getItem() instanceof StorageTypeItem) {
                return;
            }
        }
        if (stacks.size() < StorageFilterContainer.FILTER_SLOTS) {
            ItemStack copy = s.copy();
            copy.stackSize = 1;
            stacks.add(copy);
            registeredItems.add(s.getItem().getRegistryName());
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
