package com.mcjty.rftools.blocks.teleporter;

import net.minecraft.entity.Entity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class RfToolsTeleporter extends Teleporter {
    private final WorldServer worldServerInstance;

    private double x;
    private double y;
    private double z;


    public RfToolsTeleporter(WorldServer world, double x, double y, double z) {
        super(world);
        this.worldServerInstance = world;
        this.x = x;
        this.y = y;
        this.z = z;

    }

    @Override
    public void placeInPortal(Entity pEntity, double p2, double p3, double p4, float p5) {
        this.worldServerInstance.getBlock((int) this.x, (int) this.y, (int) this.z);   //dummy load to maybe gen chunk

        pEntity.setPosition(this.x, this.y, this.z);
//        pEntity.setVelocity();
    }

}
