package mcjty.rftools.blocks.powercell;

import com.google.common.primitives.Ints;
import mcjty.rftools.RFTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.common.property.IExtendedBlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PowerCellISBM implements ISmartBlockModel {

    public static final ModelResourceLocation modelResourceLocation = new ModelResourceLocation(RFTools.MODID + ":powercell");

    @Override
    public IBakedModel handleBlockState(IBlockState state) {
        // Called with the blockstate from our block. Here we get the values of the six properties and pass that to
        // our baked model implementation.

        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
        Boolean north = extendedBlockState.getValue(PowerCellBlock.NORTH);
        Boolean south = extendedBlockState.getValue(PowerCellBlock.SOUTH);
        Boolean west = extendedBlockState.getValue(PowerCellBlock.WEST);
        Boolean east = extendedBlockState.getValue(PowerCellBlock.EAST);
        Boolean up = extendedBlockState.getValue(PowerCellBlock.UP);
        Boolean down = extendedBlockState.getValue(PowerCellBlock.DOWN);
        return new BakedModel(north, south, west, east, up, down);
    }

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing side) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<BakedQuad> getGeneralQuads() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return null;
    }

    public class BakedModel implements IBakedModel {
        private TextureAtlasSprite sprite;

        private final boolean north;
        private final boolean south;
        private final boolean west;
        private final boolean east;
        private final boolean up;
        private final boolean down;

        public BakedModel(boolean north, boolean south, boolean west, boolean east, boolean up, boolean down) {
            sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(RFTools.MODID + ":blocks/powercell");
            this.north = north;
            this.south = south;
            this.west = west;
            this.east = east;
            this.up = up;
            this.down = down;
        }

        private int[] vertexToInts(double x, double y, double z, float u, float v) {
            return new int[] {
                    Float.floatToRawIntBits((float) x),
                    Float.floatToRawIntBits((float) y),
                    Float.floatToRawIntBits((float) z),
                    -1,
                    Float.floatToRawIntBits(sprite.getInterpolatedU(u)),
                    Float.floatToRawIntBits(sprite.getInterpolatedV(v)),
                    0
            };
        }

        private BakedQuad createQuad(Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4) {
            Vec3 normal = v1.subtract(v2).crossProduct(v3.subtract(v2));
            EnumFacing side = LightUtil.toSide((float) normal.xCoord, (float) normal.yCoord, (float) normal.zCoord);

            return new BakedQuad(Ints.concat(
                    vertexToInts(v1.xCoord, v1.yCoord, v1.zCoord, 0, 0),
                    vertexToInts(v2.xCoord, v2.yCoord, v2.zCoord, 0, 16),
                    vertexToInts(v3.xCoord, v3.yCoord, v3.zCoord, 16, 16),
                    vertexToInts(v4.xCoord, v4.yCoord, v4.zCoord, 16, 0)
            ), -1, side);
        }

        @Override
        public List<BakedQuad> getFaceQuads(EnumFacing side) {
            return Collections.emptyList();
        }

        @Override
        public List<BakedQuad> getGeneralQuads() {
            List<BakedQuad> quads = new ArrayList<>();
            double o = 0;

            // For each side we either cap it off if there is no similar block adjacent on that side
            // or else we extend so that we touch the adjacent block:

            if (up) {
                quads.add(createQuad(new Vec3(1-o, 1-o, o), new Vec3(1-o, 1, o), new Vec3(1-o, 1, 1-o), new Vec3(1-o, 1-o, 1-o)));
                quads.add(createQuad(new Vec3(o, 1-o, 1-o), new Vec3(o, 1, 1-o), new Vec3(o, 1, o), new Vec3(o, 1-o, o)));
                quads.add(createQuad(new Vec3(o, 1, o), new Vec3(1-o, 1, o), new Vec3(1-o, 1-o, o), new Vec3(o, 1-o, o)));
                quads.add(createQuad(new Vec3(o, 1-o, 1-o), new Vec3(1-o, 1-o, 1-o), new Vec3(1-o, 1, 1-o), new Vec3(o, 1, 1-o)));
            } else {
                quads.add(createQuad(new Vec3(o, 1-o, 1-o), new Vec3(1-o, 1-o, 1-o), new Vec3(1-o, 1-o, o), new Vec3(o, 1-o, o)));
            }

            if (down) {
                quads.add(createQuad(new Vec3(1-o, 0, o), new Vec3(1-o, o, o), new Vec3(1-o, o, 1-o), new Vec3(1-o, 0, 1-o)));
                quads.add(createQuad(new Vec3(o, 0, 1-o), new Vec3(o, o, 1-o), new Vec3(o, o, o), new Vec3(o, 0, o)));
                quads.add(createQuad(new Vec3(o, o, o), new Vec3(1-o, o, o), new Vec3(1-o, 0, o), new Vec3(o, 0, o)));
                quads.add(createQuad(new Vec3(o, 0, 1-o), new Vec3(1-o, 0, 1-o), new Vec3(1-o, o, 1-o), new Vec3(o, o, 1-o)));
            } else {
                quads.add(createQuad(new Vec3(o, o, o), new Vec3(1-o, o, o), new Vec3(1-o, o, 1-o), new Vec3(o, o, 1-o)));
            }

            if (east) {
                quads.add(createQuad(new Vec3(1-o, 1-o, 1-o), new Vec3(1, 1-o, 1-o), new Vec3(1, 1-o, o), new Vec3(1-o, 1-o, o)));
                quads.add(createQuad(new Vec3(1-o, o, o), new Vec3(1, o, o), new Vec3(1, o, 1-o), new Vec3(1-o, o, 1-o)));
                quads.add(createQuad(new Vec3(1-o, 1-o, o), new Vec3(1, 1-o, o), new Vec3(1, o, o), new Vec3(1-o, o, o)));
                quads.add(createQuad(new Vec3(1-o, o, 1-o), new Vec3(1, o, 1-o), new Vec3(1, 1-o, 1-o), new Vec3(1-o, 1-o, 1-o)));
            } else {
                quads.add(createQuad(new Vec3(1-o, o, o), new Vec3(1-o, 1-o, o), new Vec3(1-o, 1-o, 1-o), new Vec3(1-o, o, 1-o)));
            }

            if (west) {
                quads.add(createQuad(new Vec3(0, 1-o, 1-o), new Vec3(o, 1-o, 1-o), new Vec3(o, 1-o, o), new Vec3(0, 1-o, o)));
                quads.add(createQuad(new Vec3(0, o, o), new Vec3(o, o, o), new Vec3(o, o, 1-o), new Vec3(0, o, 1-o)));
                quads.add(createQuad(new Vec3(0, 1-o, o), new Vec3(o, 1-o, o), new Vec3(o, o, o), new Vec3(0, o, o)));
                quads.add(createQuad(new Vec3(0, o, 1-o), new Vec3(o, o, 1-o), new Vec3(o, 1-o, 1-o), new Vec3(0, 1-o, 1-o)));
            } else {
                quads.add(createQuad(new Vec3(o, o, 1-o), new Vec3(o, 1-o, 1-o), new Vec3(o, 1-o, o), new Vec3(o, o, o)));
            }

            if (north) {
                quads.add(createQuad(new Vec3(o, 1-o, o), new Vec3(1-o, 1-o, o), new Vec3(1-o, 1-o, 0), new Vec3(o, 1-o, 0)));
                quads.add(createQuad(new Vec3(o, o, 0), new Vec3(1-o, o, 0), new Vec3(1-o, o, o), new Vec3(o, o, o)));
                quads.add(createQuad(new Vec3(1-o, o, 0), new Vec3(1-o, 1-o, 0), new Vec3(1-o, 1-o, o), new Vec3(1-o, o, o)));
                quads.add(createQuad(new Vec3(o, o, o), new Vec3(o, 1-o, o), new Vec3(o, 1-o, 0), new Vec3(o, o, 0)));
            } else {
                quads.add(createQuad(new Vec3(o, 1-o, o), new Vec3(1-o, 1-o, o), new Vec3(1-o, o, o), new Vec3(o, o, o)));
            }
            if (south) {
                quads.add(createQuad(new Vec3(o, 1-o, 1), new Vec3(1-o, 1-o, 1), new Vec3(1-o, 1-o, 1-o), new Vec3(o, 1-o, 1-o)));
                quads.add(createQuad(new Vec3(o, o, 1-o), new Vec3(1-o, o, 1-o), new Vec3(1-o, o, 1), new Vec3(o, o, 1)));
                quads.add(createQuad(new Vec3(1-o, o, 1-o), new Vec3(1-o, 1-o, 1-o), new Vec3(1-o, 1-o, 1), new Vec3(1-o, o, 1)));
                quads.add(createQuad(new Vec3(o, o, 1), new Vec3(o, 1-o, 1), new Vec3(o, 1-o, 1-o), new Vec3(o, o, 1-o)));
            } else {
                quads.add(createQuad(new Vec3(o, o, 1-o), new Vec3(1-o, o, 1-o), new Vec3(1-o, 1-o, 1-o), new Vec3(o, 1-o, 1-o)));
            }

            return quads;
        }

        @Override
        public boolean isAmbientOcclusion() {
            return true;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean isBuiltInRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return sprite;
        }

        @Override
        public ItemCameraTransforms getItemCameraTransforms() {
            return ItemCameraTransforms.DEFAULT;
        }
    }
}
