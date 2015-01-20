package com.mcjty.rftools.dimension.world;

import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.items.dimlets.BlockMeta;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MapGenRuinedCities {
    private final GenericChunkProvider provider;

    public MapGenRuinedCities(GenericChunkProvider provider) {
        this.provider = provider;
    }

    private static class Span {
        private final int low;          // Lowest air block of this span.
        private final int high;         // Highest air block of this span.

        public static final Span EMPTY = new Span(0, -1);

        public Span(int low, int high) {
            this.low = low;
            this.high = high;
        }

        public Span(Span other) {
            this(other.low, other.high);
        }

        public boolean isEmpty() {
            return low > high;
        }

        public boolean isLarger(int ylow, int yhigh) {
            return ((yhigh - ylow) > (high - low));
        }

        public int getHeight() {
            return high - low + 1;
        }

        public Span intersect(Span other) {
            if (isEmpty() || other.isEmpty()) {
                return EMPTY;
            }
            if (low > other.high) {
                return EMPTY;
            }
            if (high < other.low) {
                return EMPTY;
            }
            return new Span(Math.max(low, other.low), Math.min(high, other.high));
        }

        public static Span averageSpan(Span... spans) {
            int size = spans.length;
            int low = 0;
            int high = 0;
            for (Span span : spans) {
                low += span.low;
                high += span.high;
            }
            return new Span(low / size, high / size);
        }
    }

    public Span findSuitableY(int x, int z, Block[] ablock) {
        int index = (x * 16 + z) * 256;

        Span bestSpan = null;

        int lowestNonair = 256;
        for (int y = 255 ; y >= 0 ; y--) {
            Block block = ablock[index+y];
            if (!(block == null || block.getMaterial() == Material.air || block == Blocks.bedrock)) {
                if (bestSpan == null || bestSpan.isLarger(y + 1, lowestNonair - 1)) {
                    bestSpan = new Span(y+1, lowestNonair-1);
                }
                lowestNonair = y;
            }
        }
        if (bestSpan == null) {
            bestSpan = Span.EMPTY;
        }
        return bestSpan;
    }

    private Span findSuitableSpan(Block[] ablock) {
        Span s1 = findSuitableY(0, 0, ablock);
        if (s1.isEmpty()) {
            return null;
        }

        Span s = new Span(s1);

        Span s2 = findSuitableY(15, 0, ablock);
        s = s.intersect(s2);
        if (s.isEmpty()) {
            return null;
        }

        Span s3 = findSuitableY(0, 15, ablock);
        s = s.intersect(s3);
        if (s.isEmpty()) {
            return null;
        }

        Span s4 = findSuitableY(15, 15, ablock);
        s = s.intersect(s4);
        if (s.isEmpty()) {
            return null;
        }

        if (s.getHeight() < 40) {
            return null;
        }

        return Span.averageSpan(s1, s2, s3, s4);
    }

    public void generate(World world, int chunkX, int chunkZ, Block[] ablock, byte[] ameta) {
        Random random = new Random((world.getSeed() + chunkX) * 117 + chunkZ * 36631 + 177);
        random.nextFloat();

        if (random.nextFloat() > 0.1f) {
            return;
        }

        Span span = findSuitableSpan(ablock);
        if (span == null) {
            return;
        }

        createBuilding(ablock, ameta, span);
    }


    private void createBlockMap() {
        if (blockMap != null) {
            return;
        }
        blockMap = new HashMap<Character, BlockMeta>();
        blockMap.put(' ', null);
        blockMap.put('B', new BlockMeta(ModBlocks.dimensionalBlock, 0));
        blockMap.put('b', new BlockMeta(ModBlocks.dimensionalBlankBlock, 0));
        blockMap.put('p', new BlockMeta(ModBlocks.dimensionalCrossBlock, 2));
        blockMap.put('P', new BlockMeta(ModBlocks.dimensionalCrossBlock, 0));
        blockMap.put('X', new BlockMeta(ModBlocks.dimensionalPattern1Block, 0));
        blockMap.put('x', new BlockMeta(ModBlocks.dimensionalPattern2Block, 0));
        blockMap.put('g', new BlockMeta(Blocks.stained_glass, 11));
    }

    private void createBuilding(Block[] ablock, byte[] ameta, Span span) {
        createBlockMap();
        int idx = 0;
        for (int y = span.low-1 ; y <= span.high ; y++) {
            if (idx >= LEVEL.length) {
                return;
            }
            String[] level = LEVEL[idx++];
            for (int x = 0 ; x < 16 ; x++) {
                for (int z = 0 ; z < 16 ; z++) {
                    int index = (x * 16 + z) * 256 + y;
                    char c = level[z].charAt(x);
                    BlockMeta blockMeta = blockMap.get(c);
                    if (blockMeta != null) {
                        ablock[index] = blockMeta.getBlock();
                        ameta[index] = blockMeta.getMeta();
                    }
                }
            }
        }
    }

    private static Map<Character, BlockMeta> blockMap = null;

    private static String[] LevelBase = new String[] {
            "BBBBBBBBBBBBBBB ",
            "BBBBBBBBBBBBBBB ",
            "BBBBBBBBBBBBBBB ",
            "BBBBBBBBBBBBBBB ",
            "BBBBBBBBBBBBBBB ",
            "BBBBBBBBBBBBBBB ",
            "BBBBBBBBBBBBBBB ",
            "BBBBBBBBBBBBBBB ",
            "BBBBBBBBBBBBBBB ",
            "BBBBBBBBBBBBBBB ",
            "BBBBBBBBBBBBBBB ",
            "BBBBBBBBBBBBBBB ",
            "BBBBBBBBBBBBBBB ",
            "BBBBBBBBBBBBBBB ",
            "BBBBBBBBBBBBBBB ",
            "                ",
    };

    private static String[] Level1_3 = new String[] {
            "Pbbbbbb bbbbbbP ",
            "b     bPb     b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b   bb X bb   b ",
            "Pbbbx     xbbbP ",
    };

    private static String[] Level4 = new String[] {
            "Pbbbbbb bbbbbbP ",
            "b     bPb     b ",
            "X             X ",
            "X             X ",
            "X             X ",
            "X             X ",
            "X             X ",
            "X             X ",
            "X             X ",
            "X             X ",
            "X             X ",
            "X             X ",
            "X             X ",
            "X             X ",
            "b   bbbbbbb   b ",
            "PbbbbbbbbbbbbbP ",
    };

    private static String[] Level5_6 = new String[] {
            "Pbbbbbb bbbbbbP ",
            "b     bPb     b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "PbbbbbbbbbbbbbP ",
    };

    private static String[] Level7 = new String[] {
            "Pbbbbbb bbbbbbP ",
            "b     bPb     b ",
            "X             X ",
            "X             X ",
            "X             X ",
            "X             X ",
            "X             X ",
            "X             X ",
            "X             X ",
            "X             X ",
            "X             X ",
            "X             X ",
            "b             b ",
            "b             b ",
            "b             b ",
            "PbbbbbbbbbbbbbP ",
    };

    private static String[] Level8_9 = new String[] {
            "Pbbbbbb bbbbbbP ",
            "b     bPb     b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "b             b ",
            "PbbbbbbbbbbbbbP ",
    };

    private static String[] Level10 = new String[] {
            "Pbbbbbb bbbbbbP ",
            "bBBBBBbPbBBBBBb ",
            "XBBBBBBBBBBBBBX ",
            "XBBBBBBBBBBBBBX ",
            "XBBBBBBBBBBBBBX ",
            "XBBBBBBBBBBBBBX ",
            "XBBBBBBBBBBBBBX ",
            "XBBBBBBBBBBBBBX ",
            "XBBBBBBBBBBBBBX ",
            "bBBBBBBBBBBBBBb ",
            "bBBBBBBBBBBBBBb ",
            "bBBBBBBBBBBBBBb ",
            "bBBBBBBBBBBBBBb ",
            "bBBBBBBBBBBBBBb ",
            "bBBBBBBBBBBBBBb ",
            "PbbbbbbbbbbbbbP ",
    };

    private static String[] Level11 = new String[] {
            "Pbbbbbb bbbbbbP ",
            "pbbbbbbPbbbbbbp ",
            "pb           bp ",
            "pb           bp ",
            "pb           bp ",
            "pb           bp ",
            "pg           gp ",
            "pg           gp ",
            "pg           gp ",
            "pg           gp ",
            "pg           gp ",
            "pg           gp ",
            "pg           gp ",
            "pg           gp ",
            "pgggggggggggggp ",
            "P             P ",
    };

    private static String[] Level12 = new String[] {
            "Pbbbbbb bbbbbbP ",
            "bbbbbbbPbbbbbbb ",
            "bb           bb ",
            "bb           bb ",
            "bb           bb ",
            "bb           bb ",
            "bg           gb ",
            "bg           gb ",
            "bg           gb ",
            "bg           gb ",
            "bg           gb ",
            "bg           gb ",
            "bg           gb ",
            "bg           gb ",
            " ggggggggggggg  ",
            "                ",
    };

    private static String[] Level13 = new String[] {
            "Pbbbbbb bbbbbbP ",
            "bbbbbbbPbbbbbbb ",
            "Xb           bX ",
            "Xb           bX ",
            "Xb           bX ",
            "bb           bb ",
            "bg           gb ",
            "bg           gb ",
            "bg           gb ",
            "bg           gb ",
            "bg           gb ",
            "bg           gb ",
            " g           g  ",
            " g           g  ",
            " ggggggggggggg  ",
            "                ",
    };

    private static String[] Level14 = new String[] {
            "Pbbbbbb bbbbbbP ",
            "bbbbbbbPbbbbbbb ",
            "bb           bb ",
            "bb           bb ",
            "bb           bb ",
            "bb           bb ",
            "bg           gb ",
            "bg           gb ",
            "bg           gb ",
            "bg           gb ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " ggggggggggggg  ",
            "                ",
    };

    private static String[] Level15 = new String[] {
            "Pbbbbbb bbbbbbP ",
            "pbbbbbbPbbbbbbp ",
            "pb           bp ",
            "pb           bp ",
            "pb           bp ",
            "pb           bp ",
            "pg           gp ",
            "pg           gp ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " ggggggggggggg  ",
            "                ",
    };

    private static String[] Level16 = new String[] {
            "                ",
            " bbbbbbPbbbbbb  ",
            " b           b  ",
            " b           b  ",
            " b           b  ",
            " b           b  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " ggggggggggggg  ",
            "                ",
    };

    private static String[] Level17_and_19 = new String[] {
            "bbbbbbb bbbbbbb ",
            "bbbbbbbPbbbbbbb ",
            "bbBBBBBBBBBBBbb ",
            "bbBBBBBBBBBBBbb ",
            "bbBBBBBBBBBBBbb ",
            "bbBBBBBBBBBBBbb ",
            "bbBBBBBBBBBBBbb ",
            "bbBBBBBBBBBBBbb ",
            "bbBBBBBBBBBBBbb ",
            "bbBBBBBBBBBBBbb ",
            "bbBBBBBBBBBBBbb ",
            "bbBBBBBBBBBBBbb ",
            "bbBBBBBBBBBBBbb ",
            "bbBBBBBBBBBBBbb ",
            "bgggggggggggggb ",
            "bbbbbbbbbbbbbbb ",
    };

    private static String[] Level18  = new String[] {
            "                ",
            " ppppppPpppppp  ",
            " pBBBBBBBBBBBp  ",
            " pBBBBBBBBBBBp  ",
            " pBBBBBBBBBBBp  ",
            " pBBBBBBBBBBBp  ",
            " pBBBBBBBBBBBp  ",
            " pBBBBBBBBBBBp  ",
            " pBBBBBBBBBBBp  ",
            " pBBBBBBBBBBBp  ",
            " pBBBBBBBBBBBp  ",
            " pBBBBBBBBBBBp  ",
            " pBBBBBBBBBBBp  ",
            " pBBBBBBBBBBBp  ",
            " ppppppppppppp  ",
            "                ",
    };

    private static String[] Level20_25  = new String[] {
            "                ",
            " gggggbPbggggg  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " g           g  ",
            " ggggggggggggg  ",
            "                ",
    };

    private static String[] LevelH1  = new String[] {
            "                ",
            " gggggbPbggggg  ",
            " ggggggggggggg  ",
            " ggggggggggggg  ",
            " ggggggggggggg  ",
            " ggggggggggggg  ",
            " ggggggggggggg  ",
            " ggggggggggggg  ",
            " ggggggggggggg  ",
            " ggggggggggggg  ",
            " ggggggggggggg  ",
            " ggggggggggggg  ",
            " ggggggggggggg  ",
            " ggggggggggggg  ",
            " ggggggggggggg  ",
            "                ",
    };

    private static String[] LevelH2  = new String[] {
            "                ",
            "      bPb       ",
            "      bPb       ",
            "      bPb       ",
            "      bbb       ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
    };

    private static String[][] LEVEL = {
            LevelBase,
            Level1_3,
            Level1_3,
            Level1_3,
            Level4,
            Level5_6,
            Level5_6,
            Level7,
            Level8_9,
            Level8_9,
            Level10,
            Level11,
            Level12,
            Level13,
            Level14,
            Level15,
            Level16,
            Level17_and_19,
            Level18,
            Level17_and_19,
            Level20_25,
            Level20_25,
            Level20_25,
            Level20_25,
            Level20_25,
            Level20_25,
            Level17_and_19,
            Level18,
            Level17_and_19,
            Level20_25,
            Level20_25,
            Level20_25,
            Level20_25,
            Level20_25,
            Level20_25,
            Level17_and_19,
            Level18,
            Level17_and_19,
            Level20_25,
            Level20_25,
            Level20_25,
            Level20_25,
            Level20_25,
            Level20_25,
            Level20_25,
            Level20_25,
            Level20_25,
            Level20_25,
            Level20_25,
            Level20_25,
            LevelH1,
            LevelH2,
    };
}
