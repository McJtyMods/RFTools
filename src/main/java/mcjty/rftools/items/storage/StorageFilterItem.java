package mcjty.rftools.items.storage;

import mcjty.lib.container.InventoryHelper;
import mcjty.lib.varia.ItemStackList;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.storage.ModularStorageTileEntity;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static mcjty.rftools.items.storage.StorageFilterContainer.FILTER_SLOTS;

public class StorageFilterItem extends GenericRFToolsItem {

    public StorageFilterItem() {
        super("filter_module");
        setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
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
            list.add(TextFormatting.WHITE + "This filter module is for the Modular Storage block,");
            list.add(TextFormatting.WHITE + "the Builder or the Area Scanner.");
            list.add(TextFormatting.WHITE + "This module can make sure the block only accepts");
            list.add(TextFormatting.WHITE + "certain types of items");
            list.add(TextFormatting.YELLOW + "Sneak-right click on an inventory to");
            list.add(TextFormatting.YELLOW + "configure the filter based on contents");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (playerIn.isSneaking()) {
            if (!worldIn.isRemote) {
                TileEntity te = worldIn.getTileEntity(pos);
                if (InventoryHelper.isInventory(te)) {
                    ItemStackList stacks = ItemStackList.create();
                    Set<ResourceLocation> registeredItems = new HashSet<>();
                    InventoryHelper.getItems(te, s -> true).forEach(s -> addItem(te, stacks, registeredItems, s));
                    if (!stack.hasTagCompound()) {
                        stack.setTagCompound(new NBTTagCompound());
                    }
                    StorageFilterInventory.convertItemsToNBT(stack.getTagCompound(), stacks);
                    ITextComponent component = new TextComponentString(TextFormatting.GREEN + "Stored inventory contents in filter");
                    if (playerIn instanceof EntityPlayer) {
                        ((EntityPlayer) playerIn).sendStatusMessage(component, false);
                    } else {
                        playerIn.sendMessage(component);
                    }
                } else {
                    IBlockState state = worldIn.getBlockState(pos);
                    ItemStack blockStack = state.getBlock().getItem(worldIn, pos, state);
                    if (!blockStack.isEmpty()) {
                        if (!stack.hasTagCompound()) {
                            stack.setTagCompound(new NBTTagCompound());
                        }
                        Set<ResourceLocation> registeredItems = new HashSet<>();
                        ItemStackList stacks = ItemStackList.create(FILTER_SLOTS);
                        NBTTagList bufferTagList = stack.getTagCompound().getTagList("Items", Constants.NBT.TAG_COMPOUND);
                        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
                            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
                            stacks.set(i, new ItemStack(nbtTagCompound));
                        }
                        for (int i = 0 ; i < FILTER_SLOTS ; i++) {
                            if (stacks.get(i).isEmpty()) {
                                stacks.set(i, blockStack);
                                ITextComponent component = new TextComponentString(TextFormatting.GREEN + "Added " + blockStack.getDisplayName() + " to the filter!");
                                if (playerIn instanceof EntityPlayer) {
                                    ((EntityPlayer) playerIn).sendStatusMessage(component, false);
                                } else {
                                    playerIn.sendMessage(component);
                                }
                                StorageFilterInventory.convertItemsToNBT(stack.getTagCompound(), stacks);
                                break;
                            }
                        }
                    }
                }
            }
            return EnumActionResult.SUCCESS;
        }
        return super.onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    private void addItem(TileEntity te, List<ItemStack> stacks, Set<ResourceLocation> registeredItems, ItemStack s) {
        if (registeredItems.contains(s.getItem().getRegistryName())) {
            return;
        }
        if (te instanceof ModularStorageTileEntity) {
            if (s.getItem() instanceof StorageModuleItem || s.getItem() instanceof StorageFilterItem || s.getItem() instanceof StorageTypeItem) {
                return;
            }
        }
        if (stacks.size() < FILTER_SLOTS) {
            ItemStack copy = s.copy();
            if (1 <= 0) {
                copy.setCount(0);
            } else {
                copy.setCount(1);
            }
            stacks.add(copy);
            registeredItems.add(s.getItem().getRegistryName());
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_STORAGE_FILTER, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public static StorageFilterCache getCache(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return new StorageFilterCache(stack);
    }
}
