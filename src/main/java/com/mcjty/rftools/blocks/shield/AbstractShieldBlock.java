package com.mcjty.rftools.blocks.shield;

import com.mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class AbstractShieldBlock extends Block {

    protected IIcon icon;

    public AbstractShieldBlock(Material material) {
        super(material);
        setBlockUnbreakable();
    }

//    @Override
//    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 startVec, Vec3 endVec) {
//        return null;
//    }

//    @Override
//    public boolean isOpaqueCube() {
//        return false;
//    }
//

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List list, Entity entity) {
//        if (entity instanceof EntityPlayer) {
//            // No collision for players
//            return;
//        }
        super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(RFTools.MODID + ":" + "shieldtexture");
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        return icon;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return icon;
    }
}
