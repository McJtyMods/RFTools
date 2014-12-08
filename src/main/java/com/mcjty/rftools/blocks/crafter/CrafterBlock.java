package com.mcjty.rftools.blocks.crafter;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.blocks.Infusable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
    }


    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        CrafterBlockTileEntity3 crafterBlockTileEntity = (CrafterBlockTileEntity3)world.getTileEntity(x, y, z);

        if (crafterBlockTileEntity != null) {
            // To avoid the ghost items being dropped in the world (which would give easy item duplication)
            // we first clear out the crafting grid here.
            for (int i = CrafterContainer.SLOT_CRAFTINPUT ; i <= CrafterContainer.SLOT_CRAFTOUTPUT ; i++) {
                crafterBlockTileEntity.setInventorySlotContents(i, null);
            }

            BlockTools.emptyInventoryInWorld(world, x, y, z, block, crafterBlockTileEntity);
        }

        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    protected void breakWithWrench(World world, int x, int y, int z) {
        // To avoid the inventory being dropped all over the place when wrenching we clear it first.
        CrafterBlockTileEntity3 crafterBlockTileEntity = (CrafterBlockTileEntity3)world.getTileEntity(x, y, z);

        if (crafterBlockTileEntity != null) {
            for (int i = 0 ; i < crafterBlockTileEntity.getSizeInventory() ; i++) {
                crafterBlockTileEntity.setInventorySlotContents(i, null);
            }
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
        CrafterBlockTileEntity3 crafterBlockTileEntity = (CrafterBlockTileEntity3) tileEntity;
        CrafterContainer crafterContainer = new CrafterContainer(entityPlayer, crafterBlockTileEntity);
        return new GuiCrafter(crafterBlockTileEntity, crafterContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new CrafterContainer(entityPlayer, (CrafterBlockTileEntity3) tileEntity);
    }
}
