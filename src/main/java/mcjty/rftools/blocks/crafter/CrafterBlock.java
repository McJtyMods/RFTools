package mcjty.rftools.blocks.crafter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.api.Infusable;
import mcjty.container.GenericContainerBlock;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class CrafterBlock extends GenericContainerBlock implements Infusable {
    private String frontName;

    public CrafterBlock(String blockName, String frontName, Class<? extends TileEntity> tileEntityClass) {
        super(Material.iron, tileEntityClass);
        setBlockName(blockName);
        this.frontName = frontName;
        setCreativeTab(RFTools.tabRfTools);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            NBTTagList recipeTagList = tagCompound.getTagList("Recipes", Constants.NBT.TAG_COMPOUND);
            list.add(EnumChatFormatting.GREEN + "Contents: " + bufferTagList.tagCount() + " stacks");

            int rc = 0;
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
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sx, float sy, float sz) {
        return onBlockActivatedDefaultWrench(world, x, y, z, player);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        checkRedstone(world, x, y, z);
    }

    @Override
    public String getIdentifyingIconName() {
        return frontName;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_CRAFTER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        CrafterBaseTE crafterBlockTileEntity = (CrafterBaseTE) tileEntity;
        CrafterContainer crafterContainer = new CrafterContainer(entityPlayer, crafterBlockTileEntity);
        return new GuiCrafter(crafterBlockTileEntity, crafterContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new CrafterContainer(entityPlayer, (CrafterBaseTE) tileEntity);
    }
}
