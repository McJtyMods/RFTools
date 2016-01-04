package mcjty.rftools.blocks.screens;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelScreen extends ModelBase {

    private ModelRenderer renderer = new ModelRenderer(this, 0, 0);

    public ModelScreen(int size) {
        if (size == ScreenTileEntity.SIZE_HUGE) {
            this.renderer.addBox(-8.0F, -8.0F, -1.0F, 48, 48, 2, 0.0F);
        } else if (size == ScreenTileEntity.SIZE_LARGE) {
            this.renderer.addBox(-8.0F, -8.0F, -1.0F, 32, 32, 2, 0.0F);
        } else {
            this.renderer.addBox(-8.0F, -8.0F, -1.0F, 16, 16, 2, 0.0F);
        }
        this.renderer.setTextureSize(16, 16);
    }

    public void render() {
        this.renderer.render(0.0625F);
    }

}
