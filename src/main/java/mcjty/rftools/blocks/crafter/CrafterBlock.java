package mcjty.rftools.blocks.crafter;

import mcjty.lib.api.Infusable;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.container.InventoryHelper;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class CrafterBlock extends GenericRFToolsBlock<CrafterBaseTE, CrafterContainer> implements Infusable /*, IRedstoneConnectable*/ {

    public CrafterBlock(String blockName, Class<? extends CrafterBaseTE> tileEntityClass) {
        super(Material.IRON, tileEntityClass, CrafterContainer.class, blockName, true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiCrafter.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            NBTTagList recipeTagList = tagCompound.getTagList("Recipes", Constants.NBT.TAG_COMPOUND);

            int rc = 0;
            for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
                NBTTagCompound itemTag = bufferTagList.getCompoundTagAt(i);
                if (itemTag != null) {
                    ItemStack stack = ItemStack.loadItemStackFromNBT(itemTag);
                    if (stack != null) {
                        rc++;
                    }
                }
            }

            list.add(TextFormatting.GREEN + "Contents: " + rc + " stacks");

            rc = 0;
            for (int i = 0 ; i < recipeTagList.tagCount() ; i++) {
                NBTTagCompound tagRecipe = recipeTagList.getCompoundTagAt(i);
                NBTTagCompound resultCompound = tagRecipe.getCompoundTag("Result");
                if (resultCompound != null) {
                    ItemStack stack = ItemStack.loadItemStackFromNBT(resultCompound);
                    if (stack != null) {
                        rc++;
                    }
                }
            }

            list.add(TextFormatting.GREEN + "Recipes: " + rc + " recipes");
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            int amount;
            if (tileEntityClass.equals(CrafterBlockTileEntity1.class)) {
                amount = 2;
            } else if (tileEntityClass.equals(CrafterBlockTileEntity2.class)) {
                amount = 4;
            } else {
                amount = 8;
            }
            list.add(TextFormatting.WHITE + "This machine can handle up to " + amount + " recipes");
            list.add(TextFormatting.WHITE + "at once and allows recipes to use the crafting results");
            list.add(TextFormatting.WHITE + "of previous steps.");
            list.add(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (heldItem != null && heldItem.getItem() == ModularStorageSetup.storageFilterItem) {
            if (InventoryHelper.installModule(player, heldItem, hand, pos, CrafterContainer.SLOT_FILTER_MODULE, CrafterContainer.SLOT_FILTER_MODULE)) {
                return true;
            }
        }
        return super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
        // @todo?
        checkRedstoneWithTE((World) world, pos);
    }

//    @Override
//    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, EnumFacing from) {
//        return true;
//    }
//
    @Override
    public int getGuiID() {
        return RFTools.GUI_CRAFTER;
    }
}
