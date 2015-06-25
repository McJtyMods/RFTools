package mcjty.rftools.village;

import mcjty.rftools.GeneralConfiguration;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import java.util.List;
import java.util.Random;

public class VillagePiece extends StructureVillagePieces.Village {

    public VillagePiece() {
    }

    public VillagePiece(StructureVillagePieces.Start p_i2094_1_, int p_i2094_2_, Random p_i2094_3_, StructureBoundingBox p_i2094_4_, int p_i2094_5_) {
        super(p_i2094_1_, p_i2094_2_);
        this.coordBaseMode = p_i2094_5_;
        this.boundingBox = p_i2094_4_;
    }

    public static VillagePiece buildPiece(StructureVillagePieces.Start start, List list, Random random, int p_74898_3_, int p_74898_4_, int p_74898_5_, int p_74898_6_, int p_74898_7_) {
        StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_74898_3_, p_74898_4_, p_74898_5_, 0, 0, 0, 9, 9, 6, p_74898_6_);
        return canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(list, structureboundingbox) == null ? new VillagePiece(start, p_74898_7_, random, structureboundingbox, p_74898_6_) : null;
    }

    /**
     * second Part of Structure generating, this for example places Spiderwebs, Mob Spawners, it closes
     * Mineshafts at the end, it adds Fences...
     */
    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox bbox) {
        if (this.field_143015_k < 0) {
            this.field_143015_k = this.getAverageGroundLevel(world, bbox);

            if (this.field_143015_k < 0) {
                return true;
            }

            this.boundingBox.offset(0, this.field_143015_k - this.boundingBox.maxY + 9 - 1, 0);
        }

        this.fillWithBlocks(world, bbox, 1, 1, 1, 7, 5, 4, Blocks.air, Blocks.air, false);
        Block stone = Blocks.hardened_clay;
//        Block stone = Blocks.cobblestone;
        this.fillWithBlocks(world, bbox, 0, 0, 0, 8, 0, 5, stone, stone, false);
        this.fillWithBlocks(world, bbox, 0, 5, 0, 8, 5, 5, stone, stone, false);
        this.fillWithBlocks(world, bbox, 0, 6, 1, 8, 6, 4, stone, stone, false);
        this.fillWithBlocks(world, bbox, 0, 7, 2, 8, 7, 3, stone, stone, false);
        int i = this.getMetadataWithOffset(Blocks.oak_stairs, 3);
        int j = this.getMetadataWithOffset(Blocks.oak_stairs, 2);
        int k;
        int l;

        for (k = -1; k <= 2; ++k) {
            for (l = 0; l <= 8; ++l) {
                this.placeBlockAtCurrentPosition(world, Blocks.oak_stairs, i, l, 6 + k, k, bbox);
                this.placeBlockAtCurrentPosition(world, Blocks.oak_stairs, j, l, 6 + k, 5 - k, bbox);
            }
        }

        this.fillWithBlocks(world, bbox, 0, 1, 0, 0, 1, 5, stone, stone, false);
        this.fillWithBlocks(world, bbox, 1, 1, 5, 8, 1, 5, stone, stone, false);
        this.fillWithBlocks(world, bbox, 8, 1, 0, 8, 1, 4, stone, stone, false);
        this.fillWithBlocks(world, bbox, 2, 1, 0, 7, 1, 0, stone, stone, false);
        this.fillWithBlocks(world, bbox, 0, 2, 0, 0, 4, 0, stone, stone, false);
        this.fillWithBlocks(world, bbox, 0, 2, 5, 0, 4, 5, stone, stone, false);
        this.fillWithBlocks(world, bbox, 8, 2, 5, 8, 4, 5, stone, stone, false);
        this.fillWithBlocks(world, bbox, 8, 2, 0, 8, 4, 0, stone, stone, false);
        this.fillWithBlocks(world, bbox, 0, 2, 1, 0, 4, 4, Blocks.planks, Blocks.planks, false);
        this.fillWithBlocks(world, bbox, 1, 2, 5, 7, 4, 5, Blocks.planks, Blocks.planks, false);
        this.fillWithBlocks(world, bbox, 8, 2, 1, 8, 4, 4, Blocks.planks, Blocks.planks, false);
        this.fillWithBlocks(world, bbox, 1, 2, 0, 7, 4, 0, Blocks.planks, Blocks.planks, false);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 4, 2, 0, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 5, 2, 0, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 6, 2, 0, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 4, 3, 0, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 5, 3, 0, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 6, 3, 0, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 0, 2, 2, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 0, 2, 3, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 0, 3, 2, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 0, 3, 3, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 8, 2, 2, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 8, 2, 3, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 8, 3, 2, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 8, 3, 3, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 2, 2, 5, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 3, 2, 5, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 5, 2, 5, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 6, 2, 5, bbox);
        this.fillWithBlocks(world, bbox, 1, 4, 1, 7, 4, 1, Blocks.planks, Blocks.planks, false);
        this.fillWithBlocks(world, bbox, 1, 4, 4, 7, 4, 4, Blocks.planks, Blocks.planks, false);
        this.fillWithBlocks(world, bbox, 1, 3, 4, 7, 3, 4, Blocks.bookshelf, Blocks.bookshelf, false);
        this.placeBlockAtCurrentPosition(world, Blocks.planks, 0, 7, 1, 4, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.oak_stairs, this.getMetadataWithOffset(Blocks.oak_stairs, 0), 7, 1, 3, bbox);
        k = this.getMetadataWithOffset(Blocks.oak_stairs, 3);
        this.placeBlockAtCurrentPosition(world, Blocks.oak_stairs, k, 6, 1, 4, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.oak_stairs, k, 5, 1, 4, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.oak_stairs, k, 4, 1, 4, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.oak_stairs, k, 3, 1, 4, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.fence, 0, 6, 1, 3, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.wooden_pressure_plate, 0, 6, 2, 3, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.fence, 0, 4, 1, 3, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.wooden_pressure_plate, 0, 4, 2, 3, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.crafting_table, 0, 7, 1, 1, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.air, 0, 1, 1, 0, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.air, 0, 1, 2, 0, bbox);
        this.placeDoorAtCurrentPosition(world, bbox, random, 1, 1, 0, this.getMetadataWithOffset(Blocks.wooden_door, 1));

        if (this.getBlockAtCurrentPosition(world, 1, 0, -1, bbox).getMaterial() == Material.air && this.getBlockAtCurrentPosition(world, 1, -1, -1, bbox).getMaterial() != Material.air) {
            this.placeBlockAtCurrentPosition(world, Blocks.stone_stairs, this.getMetadataWithOffset(Blocks.stone_stairs, 3), 1, 0, -1, bbox);
        }

        for (l = 0; l < 6; ++l) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.clearCurrentPositionBlocksUpwards(world, i1, 9, l, bbox);
                this.func_151554_b(world, stone, 0, i1, -1, l, bbox);
            }
        }

        this.spawnVillagers(world, bbox, 2, 1, 2, 1);
        return true;
    }

    /**
     * Returns the villager type to spawn in this component, based on the number of villagers already spawned.
     */
    @Override
    protected int getVillagerType(int count) {
        return GeneralConfiguration.realVillagerId;
    }
}
