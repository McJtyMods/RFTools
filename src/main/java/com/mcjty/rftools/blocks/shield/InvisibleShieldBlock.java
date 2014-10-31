package com.mcjty.rftools.blocks.shield;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

public class InvisibleShieldBlock extends Block {

    public InvisibleShieldBlock(Material material) {
        super(material);
        setBlockName("invisibleShieldBlock");
        setBlockUnbreakable();
    }

    @Override
    public int getRenderType() {
        return -1;              // Invisible
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 startVec, Vec3 endVec) {
        return null;
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List list, Entity entity) {
//        if (entity instanceof EntityPlayer) {
//            // No collision for players
//            return;
//        }
        super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
    }
}
