package com.mcjty.rftools.blocks.dimletconstruction;

import com.mcjty.container.GenericBlock;
import com.mcjty.entity.GenericTileEntity;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BiomeAbsorberBlock extends GenericBlock {

    public BiomeAbsorberBlock() {
        super(Material.iron, BiomeAbsorberTileEntity.class);
        setBlockName("biomeAbsorberBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        NBTTagCompound tagCompound = accessor.getNBTData();
        if (tagCompound != null) {
            int biomeID = tagCompound.getInteger("biome");
            if (biomeID != -1) {
                BiomeGenBase biome = BiomeGenBase.getBiome(biomeID);
                if (biome != null) {
                    currenttip.add(EnumChatFormatting.GREEN + "Biome: " + biome.biomeName);
                    int absorbing = tagCompound.getInteger("absorbing");
                    int pct = ((DimletConfiguration.maxBiomeAbsorbtion - absorbing) * 100) / DimletConfiguration.maxBiomeAbsorbtion;
                    currenttip.add(EnumChatFormatting.GREEN + "Absorbed: " + pct + "%");
                }
            }
        }
        return currenttip;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int biomeID = tagCompound.getInteger("biome");
            if (biomeID != -1) {
                BiomeGenBase biome = BiomeGenBase.getBiome(biomeID);
                if (biome != null) {
                    list.add(EnumChatFormatting.GREEN + "Biome: " + biome.biomeName);
                    int absorbing = tagCompound.getInteger("absorbing");
                    int pct = ((DimletConfiguration.maxBiomeAbsorbtion - absorbing) * 100) / DimletConfiguration.maxBiomeAbsorbtion;
                    list.add(EnumChatFormatting.GREEN + "Absorbed: " + pct + "%");
                }
            }
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Place this block in an area and it will");
            list.add(EnumChatFormatting.WHITE + "gradually absorb the essence of the biome it is in.");
            list.add(EnumChatFormatting.WHITE + "You can use the end result in the Dimlet Workbench.");
        } else {
            list.add(EnumChatFormatting.WHITE + "Press Shift for more");
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        // We don't want what GenericContainerBlock does.
        restoreBlockFromNBT(world, x, y, z, itemStack);
        if (!world.isRemote) {
            BiomeAbsorberTileEntity biomeAbsorberTileEntity = (BiomeAbsorberTileEntity) world.getTileEntity(x, y, z);
            biomeAbsorberTileEntity.placeDown();
        }
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        return new ArrayList<ItemStack>();
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        ItemStack stack = new ItemStack(block);
        if (te instanceof GenericTileEntity) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            ((GenericTileEntity)te).writeRestorableToNBT(tagCompound);
            stack.setTagCompound(tagCompound);
        }

        super.breakBlock(world, x, y, z, block, meta);
        world.spawnEntityInWorld(new EntityItem(world, x, y, z, stack));
    }

    @Override
    public String getSideIconName() {
        return "biomeAbsorber";
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderBlockPass() {
        return 0;
    }

    @Override
    public int getGuiID() {
        return -1;
    }
}
