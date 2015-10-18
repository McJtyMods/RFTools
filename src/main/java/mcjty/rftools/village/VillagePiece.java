package mcjty.rftools.village;

import mcjty.lib.varia.Coordinate;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.blocks.dimlets.DimletSetup;
import mcjty.rftools.blocks.logic.LogicBlockSetup;
import mcjty.rftools.blocks.logic.SequencerMode;
import mcjty.rftools.blocks.logic.SequencerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import java.util.List;
import java.util.Random;

public class VillagePiece extends StructureVillagePieces.Village {

    public VillagePiece() {
    }

    public VillagePiece(StructureVillagePieces.Start start, int weight, Random random, StructureBoundingBox box, int coordBaseMode) {
        super(start, weight);
        this.coordBaseMode = coordBaseMode;
        this.boundingBox = box;
    }

    public static VillagePiece buildPiece(StructureVillagePieces.Start start, List list, Random random, int p_74898_3_, int p_74898_4_, int p_74898_5_, int p_74898_6_, int p_74898_7_) {
        StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p_74898_3_, p_74898_4_, p_74898_5_, 0, 0, 0, 9, 9, 6, p_74898_6_);
        return canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(list, structureboundingbox) == null ? new VillagePiece(start, p_74898_7_, random, structureboundingbox, p_74898_6_) : null;
    }


    /**
     * arguments: (World worldObj, StructureBoundingBox structBB, int minX, int minY, int minZ, int maxX, int maxY, int
     * maxZ, Block placeBlock, int placeBlockMetadata, Block replaceBlock, int replaceBlockMetadata, boolean
     * alwaysreplace)
     */
    protected void fillWithMetadataBlocks(World world, StructureBoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Block block, int meta) {
        for (int i2 = minY; i2 <= maxY; ++i2) {
            for (int j2 = minX; j2 <= maxX; ++j2) {
                for (int k2 = minZ; k2 <= maxZ; ++k2) {
                    this.placeBlockAtCurrentPosition(world, block, meta, j2, i2, k2, box);
                }
            }
        }
    }

    /**
     * arguments: (World worldObj, StructureBoundingBox structBB, int minX, int minY, int minZ, int maxX, int
     * maxY, int maxZ, int placeBlock, int replaceBlock, boolean alwaysreplace)
     */
    private void fillWithBlocks(World world, StructureBoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Block placeBlock, int meta) {
        fillWithMetadataBlocks(world, box, minX, minY, minZ, maxX, maxY, maxZ, placeBlock, meta);
    }

    /**
     * Current Position depends on currently set Coordinates mode, is computed here
     */
    private Coordinate getCoordinate(int x, int y, int z) {
        int i1 = this.getXWithOffset(x, z);
        int j1 = this.getYWithOffset(y);
        int k1 = this.getZWithOffset(x, z);
        return new Coordinate(i1, j1, k1);
    }

    private void setupSequencer(World world, int x, int y, int z, Random random) {
        Coordinate c = getCoordinate(x, y, z);
        TileEntity tileEntity = world.getTileEntity(c.getX(), c.getY(), c.getZ());
        if (tileEntity instanceof SequencerTileEntity) {
            SequencerTileEntity sequencerTileEntity = (SequencerTileEntity) tileEntity;

            sequencerTileEntity.setDelay(5);
            for (int i = 0; i < 64; i++) {
                sequencerTileEntity.setCycleBit(i, random.nextBoolean());
            }
            sequencerTileEntity.setMode(SequencerMode.MODE_LOOP1);
        }
    }

    private int rotateMetaSeq(int meta) {
        switch (coordBaseMode) {
            case 3:
                return meta;
            case 0:
                switch (meta) {
                    case 3: return 1;
                    case 2: return 4;
                }
                break;
            case 2:
                switch (meta) {
                    case 3: return 0;
                    case 2: return 5;
                }
                break;
            case 1:
                switch (meta) {
                    case 3: return 2;
                    case 2: return 3;
                }
                break;
        }
        return meta;
    }

    private int rotateMeta(int meta) {
        switch (coordBaseMode) {
            case 3:
                return meta;
            case 0:
            case 2:
                switch (meta) {
                    case 3: return 0;
                    case 4: return 2;
                    case 2: return 4;
                }
                break;
        }
        return meta;
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

        this.fillWithBlocks(world, bbox, 1, 1, 1, 7, 5, 4, Blocks.air, 0);
        Block stone = Blocks.cobblestone;
        int meta = 0;
//        Block stone = Blocks.stained_hardened_clay;
//        int meta = 3;

        this.fillWithBlocks(world, bbox, 0, 0, 0, 0, 0, 5, stone, meta);
        this.fillWithBlocks(world, bbox, 1, 0, 5, 8, 0, 5, stone, meta);
        this.fillWithBlocks(world, bbox, 8, 0, 0, 8, 0, 4, stone, meta);
        this.fillWithBlocks(world, bbox, 1, 0, 0, 7, 0, 0, stone, meta);

        this.fillWithBlocks(world, bbox, 1, 0, 1, 7, 0, 4, DimletSetup.dimensionalBlankBlock, 0);
        this.fillWithBlocks(world, bbox, 0, 5, 0, 8, 5, 5, stone, meta);
        this.fillWithBlocks(world, bbox, 0, 6, 1, 8, 6, 4, stone, meta);
        this.fillWithBlocks(world, bbox, 0, 7, 2, 8, 7, 3, stone, meta);
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

        this.fillWithBlocks(world, bbox, 0, 1, 0, 0, 1, 5, stone, meta);
        this.fillWithBlocks(world, bbox, 1, 1, 5, 8, 1, 5, stone, meta);
        this.fillWithBlocks(world, bbox, 8, 1, 0, 8, 1, 4, stone, meta);
        this.fillWithBlocks(world, bbox, 2, 1, 0, 7, 1, 0, stone, meta);
        this.fillWithBlocks(world, bbox, 0, 2, 0, 0, 4, 0, stone, meta);
        this.fillWithBlocks(world, bbox, 0, 2, 5, 0, 4, 5, stone, meta);
        this.fillWithBlocks(world, bbox, 8, 2, 5, 8, 4, 5, stone, meta);
        this.fillWithBlocks(world, bbox, 8, 2, 0, 8, 4, 0, stone, meta);
        this.fillWithBlocks(world, bbox, 0, 2, 1, 0, 4, 4, Blocks.planks, 0);
        this.fillWithBlocks(world, bbox, 1, 2, 5, 7, 4, 5, Blocks.planks, 0);
        this.fillWithBlocks(world, bbox, 8, 2, 1, 8, 4, 4, Blocks.planks, 0);
        this.fillWithBlocks(world, bbox, 1, 2, 0, 7, 4, 0, Blocks.planks, 0);
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
        this.fillWithBlocks(world, bbox, 1, 4, 1, 7, 4, 1, Blocks.planks, 0);
        this.fillWithBlocks(world, bbox, 1, 4, 4, 7, 4, 4, Blocks.planks, 0);

        this.placeBlockAtCurrentPosition(world, LogicBlockSetup.sequencerBlock, rotateMetaSeq(3), 7, 1, 4, bbox);      // 3->3, 2->0
        this.placeBlockAtCurrentPosition(world, DimletSetup.dimensionalCross2Block, 0, 7, 2, 4, bbox);
        this.placeBlockAtCurrentPosition(world, DimletSetup.dimensionalCross2Block, 0, 7, 3, 4, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.redstone_lamp, 0, 7, 1, 3, bbox);
        this.placeBlockAtCurrentPosition(world, Blocks.redstone_lamp, 0, 7, 1, 2, bbox);
        this.placeBlockAtCurrentPosition(world, LogicBlockSetup.sequencerBlock, rotateMetaSeq(2), 7, 1, 1, bbox);      // 3->2, 2->5
        this.placeBlockAtCurrentPosition(world, DimletSetup.dimensionalCross2Block, 0, 7, 2, 1, bbox);
        this.placeBlockAtCurrentPosition(world, DimletSetup.dimensionalCross2Block, 0, 7, 3, 1, bbox);

        this.placeBlockAtCurrentPosition(world, DimletSetup.dimensionalCross2Block, rotateMeta(4), 7, 4, 2, bbox);  // 3->4, 2->2
        this.placeBlockAtCurrentPosition(world, DimletSetup.dimensionalCross2Block, rotateMeta(4), 7, 4, 3, bbox);  // 3->4, 2->2

        this.placeBlockAtCurrentPosition(world, DimletSetup.dimensionalCross2Block, rotateMeta(2), 6, 1, 4, bbox);  // 3->2, 2->4
        this.placeBlockAtCurrentPosition(world, DimletSetup.dimensionalCross2Block, rotateMeta(2), 6, 1, 1, bbox);  // 3->2, 2->4

        setupSequencer(world, 7, 1, 4, random);
        setupSequencer(world, 7, 1, 1, random);

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
        return GeneralConfiguration.villagerId;
    }
}
