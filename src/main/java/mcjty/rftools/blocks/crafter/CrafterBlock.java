package mcjty.rftools.blocks.crafter;

import crazypants.enderio.api.redstone.IRedstoneConnectable;
import mcjty.lib.api.Infusable;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

@Optional.InterfaceList({
        @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class CrafterBlock extends GenericRFToolsBlock<CrafterBaseTE, CrafterContainer, GuiCrafter> implements Infusable, IRedstoneConnectable {

    public CrafterBlock(String blockName, Class<? extends CrafterBaseTE> tileEntityClass) {
        super(Material.iron, tileEntityClass, CrafterContainer.class, GuiCrafter.class, blockName, true);
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

            list.add(EnumChatFormatting.GREEN + "Contents: " + rc + " stacks");

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

            list.add(EnumChatFormatting.GREEN + "Recipes: " + rc + " recipes");
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
            list.add(EnumChatFormatting.WHITE + "This machine can handle up to " + amount + " recipes");
            list.add(EnumChatFormatting.WHITE + "at once and allows recipes to use the crafting results");
            list.add(EnumChatFormatting.WHITE + "of previous steps.");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        checkRedstoneWithTE(world, pos);
    }

    @Override
    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, EnumFacing from) {
        return true;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_CRAFTER;
    }
}
