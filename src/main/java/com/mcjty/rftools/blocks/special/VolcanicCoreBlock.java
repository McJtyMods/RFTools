package com.mcjty.rftools.blocks.special;

import com.mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class VolcanicCoreBlock extends Block implements ITileEntityProvider {

    private IIcon icon;

    public VolcanicCoreBlock() {
        super(Material.rock);
        setBlockName("volcanicCoreBlock");
        setCreativeTab(RFTools.tabRfTools);
        setHardness(50.0F);
        setResistance(2000.0F);
        setStepSound(soundTypePiston);
        setHarvestLevel("pickaxe", 3);
    }

    @Override
    public boolean isBurning(IBlockAccess world, int x, int y, int z) {
        return true;
    }

    @Override
    public int getLightValue() {
        return 15;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return null;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new VolcanicCoreTileEntity();
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float sx, float sy, float sz, int meta) {
        int rc = super.onBlockPlaced(world, x, y, z, side, sx, sy, sz, meta);
        if (!world.isRemote) {
            return rc;
        }
        // Client-side only.
        System.out.println("com.mcjty.rftools.blocks.special.VolcanicCoreBlock.onBlockPlaced");
        Minecraft.getMinecraft().getSoundHandler().playSound(new VolcanicRumbleSound(Minecraft.getMinecraft().thePlayer, world, x, y, z));
        return rc;
    }


    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(RFTools.MODID + ":volcanicCore");
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return icon;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return icon;
    }
}
