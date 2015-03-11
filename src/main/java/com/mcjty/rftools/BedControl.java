package com.mcjty.rftools;

import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDirectional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class BedControl {

    public static int getBedMeta(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);

        if (!BlockBed.isBlockHeadOfBed(meta)) {
            int j1 = BlockDirectional.getDirection(meta);
            x += BlockBed.field_149981_a[j1][0];
            z += BlockBed.field_149981_a[j1][1];

            if (!(world.getBlock(x, y, z) instanceof BlockBed)) {
                return -1;
            }

            meta = world.getBlockMetadata(x, y, z);
        }
        return meta;
    }

    public static boolean trySleep(World world, EntityPlayer player, int x, int y, int z, int meta) {
        if (BlockBed.func_149976_c(meta)) {
            EntityPlayer entityplayer1 = null;

            for (Object playerEntity : world.playerEntities) {
                EntityPlayer entityplayer2 = (EntityPlayer) playerEntity;

                if (entityplayer2.isPlayerSleeping()) {
                    ChunkCoordinates chunkcoordinates = entityplayer2.playerLocation;

                    if (chunkcoordinates.posX == x && chunkcoordinates.posY == y && chunkcoordinates.posZ == z) {
                        entityplayer1 = entityplayer2;
                    }
                }
            }

            if (entityplayer1 != null) {
                player.addChatComponentMessage(new ChatComponentTranslation("tile.bed.occupied"));
                return true;
            }

            BlockBed.func_149979_a(world, x, y, z, false);
        }

        EntityPlayer.EnumStatus enumstatus = player.sleepInBedAt(x, y, z);

        if (enumstatus == EntityPlayer.EnumStatus.OK) {
            BlockBed.func_149979_a(world, x, y, z, true);
            return true;
        } else {
            if (enumstatus == EntityPlayer.EnumStatus.NOT_POSSIBLE_NOW) {
                player.addChatComponentMessage(new ChatComponentTranslation("tile.bed.noSleep"));
            } else if (enumstatus == EntityPlayer.EnumStatus.NOT_SAFE) {
                player.addChatComponentMessage(new ChatComponentTranslation("tile.bed.notSafe"));
            }

            return true;
        }

    }
}
