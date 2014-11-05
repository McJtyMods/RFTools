package com.mcjty.rftools.blocks.shield;

import net.minecraft.block.material.Material;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class InvisibleShieldBlock extends AbstractShieldBlock {

    public InvisibleShieldBlock(Material material) {
        super(material);
        setBlockName("invisibleShieldBlock");
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 startVec, Vec3 endVec) {
        return null;
    }


    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1;              // Invisible
    }
}
