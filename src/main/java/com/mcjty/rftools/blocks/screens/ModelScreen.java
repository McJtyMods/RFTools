package com.mcjty.rftools.blocks.screens;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

@SideOnly(Side.CLIENT)
public class ModelScreen extends ModelBase {

    public ModelRenderer renderer = new ModelRenderer(this, 0, 0);

//    this.signBoard.addBox(-12.0F, -14.0F, -1.0F, 24, 12, 2, 0.0F);
    public ModelScreen() {
        this.renderer.addBox(-14.0F, -14.0F, -1.0F, 28, 28, 2, 0.0F);
//        this.renderer.addBox(-14.0F, -14.0F, -1.0F, 28, 28, 2, 0.0F);
    }

    public void render() {
        this.renderer.render(0.0625F);
    }

}
