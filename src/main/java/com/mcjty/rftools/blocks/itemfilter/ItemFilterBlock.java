package com.mcjty.rftools.blocks.itemfilter;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ItemFilterBlock extends GenericContainerBlock {

    private IIcon icons[] = new IIcon[6];

    public ItemFilterBlock() {
        super(Material.iron, ItemFilterTileEntity.class);
        setBlockName("itemFilterBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icons[ForgeDirection.DOWN.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineItemFilterD");
        icons[ForgeDirection.UP.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineItemFilterU");
        icons[ForgeDirection.NORTH.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineItemFilterN");
        icons[ForgeDirection.SOUTH.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineItemFilterS");
        icons[ForgeDirection.WEST.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineItemFilterW");
        icons[ForgeDirection.EAST.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineItemFilterE");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            list.add(EnumChatFormatting.GREEN + "Contents: " + bufferTagList.tagCount() + " stacks");
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "With this block you can direct items from any side");
            list.add(EnumChatFormatting.WHITE + "to any other side. This allows you to make item");
            list.add(EnumChatFormatting.WHITE + "filters for quarries, tree farms, mob farms, ...");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }

    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_ITEMFILTER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        ItemFilterTileEntity itemFilterTileEntity = (ItemFilterTileEntity) tileEntity;
        ItemFilterContainer itemFilterContainer = new ItemFilterContainer(entityPlayer, itemFilterTileEntity);
        return new GuiItemFilter(itemFilterTileEntity, itemFilterContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new ItemFilterContainer(entityPlayer, (ItemFilterTileEntity) tileEntity);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        ItemFilterTileEntity itemFilterTileEntity = (ItemFilterTileEntity)world.getTileEntity(x, y, z);

        if (itemFilterTileEntity != null) {
            // To avoid the ghost items being dropped in the world (which would give easy item duplication)
            // we first clear out the crafting grid here.
            for (int i = ItemFilterContainer.SLOT_GHOST ; i < (ItemFilterContainer.SLOT_GHOST + ItemFilterContainer.GHOST_SIZE); i++) {
                itemFilterTileEntity.setInventorySlotContents(i, null);
            }

            BlockTools.emptyInventoryInWorld(world, x, y, z, block, itemFilterTileEntity);
        }

        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    protected void breakWithWrench(World world, int x, int y, int z) {
        // To avoid the inventory being dropped all over the place when wrenching we clear it first.
        ItemFilterTileEntity itemFilterTileEntity = (ItemFilterTileEntity)world.getTileEntity(x, y, z);

        if (itemFilterTileEntity != null) {
            for (int i = 0 ; i < itemFilterTileEntity.getSizeInventory() ; i++) {
                itemFilterTileEntity.setInventorySlotContents(i, null);
            }
        }
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        return icons[side];
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return icons[side];
    }
}
