package com.mcjty.rftools.blocks;

import com.mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.swing.*;

public class RFMonitorBlock extends Block {

//    private static final ResourceLocation iconFront = new ResourceLocation(RFTools.MODID, "textures/blocks/machineFront.png");
//    private static final ResourceLocation iconSide = new ResourceLocation(RFTools.MODID, "textures/blocks/machineSide.png");

    private IIcon iconFront;
    private IIcon iconSide;

    public RFMonitorBlock(Material material) {
        super(material);
        setBlockName("rfMonitorBlock");
    }


    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sidex, float sidey, float sidez) {
        return super.onBlockActivated(world, x, y, z, player, side, sidex, sidey, sidez);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconFront = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFront");
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + "machineSide");
    }



    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == 2) {
            return iconFront;
        } else {
            return iconSide;
        }
    }

    @Override
    public IIcon getIcon(IBlockAccess p_149673_1_, int p_149673_2_, int p_149673_3_, int p_149673_4_, int p_149673_5_) {
        return super.getIcon(p_149673_1_, p_149673_2_, p_149673_3_, p_149673_4_, p_149673_5_);
    }
}
