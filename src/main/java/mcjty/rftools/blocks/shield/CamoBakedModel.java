package mcjty.rftools.blocks.shield;

import mcjty.rftools.RFTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class CamoBakedModel implements IBakedModel {

    public static final ModelResourceLocation modelFacade = new ModelResourceLocation(RFTools.MODID + ":" + CamoShieldBlock.CAMO);

    private VertexFormat format;
    private static TextureAtlasSprite spriteCable;

    public CamoBakedModel(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        this.format = format;
    }

    private static void initTextures() {
        if (spriteCable == null) {
            spriteCable = Minecraft.getInstance().getTextureMapBlocks().getAtlasSprite(RFTools.MODID + ":blocks/facade");
        }
    }



    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, long rand) {
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
        CamoBlockId facadeId = extendedBlockState.getValue(CamoShieldBlock.CAMOID);
        if (facadeId == null) {
            return Collections.emptyList();
        }

        BlockState facadeState = facadeId.getBlockState();
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if (layer != null && !facadeState.getBlock().canRenderInLayer(facadeState, layer)) { // always render in the null layer or the block-breaking textures don't show up
            return Collections.emptyList();
        }
        IBakedModel model = getModel(facadeState);
        try {
            return model.getQuads(state, side, rand);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private IBakedModel getModel(@Nonnull BlockState facadeState) {
        initTextures();
        IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(facadeState);
        return model;
    }


    @Override
    public boolean isAmbientOcclusion() {
        return true;
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
        return spriteCable;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

}
