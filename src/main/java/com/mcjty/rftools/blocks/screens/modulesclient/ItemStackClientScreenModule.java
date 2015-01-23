package com.mcjty.rftools.blocks.screens.modulesclient;

import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.rftools.blocks.screens.ModuleGuiChanged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemStackClientScreenModule implements ClientScreenModule {
    @Override
    public TransformMode getTransformMode() {
        return TransformMode.ITEM;
    }

    @Override
    public int getHeight() {
        return 30;
    }

    @Override
    public void render(FontRenderer fontRenderer, int currenty) {
        RenderItem itemRender = new RenderItem();
        short short1 = 240;
        short short2 = 240;
        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, short1 / 1.0F, short2 / 1.0F);
        ItemStack itm = new ItemStack(Blocks.cobblestone, 32);
        itemRender.renderItemIntoGUI(fontRenderer, Minecraft.getMinecraft().getTextureManager(), itm, 20, currenty);
        itemRender.renderItemOverlayIntoGUI(fontRenderer, Minecraft.getMinecraft().getTextureManager(), itm, 20, currenty);

        itm = new ItemStack(Blocks.dirt, 20);
        itemRender.renderItemIntoGUI(fontRenderer, Minecraft.getMinecraft().getTextureManager(), itm, 50, currenty);
        itemRender.renderItemOverlayIntoGUI(fontRenderer, Minecraft.getMinecraft().getTextureManager(), itm, 50, currenty);

        itm = new ItemStack(Blocks.glowstone, 64);
        itemRender.renderItemIntoGUI(fontRenderer, Minecraft.getMinecraft().getTextureManager(), itm, 80, currenty);
        itemRender.renderItemOverlayIntoGUI(fontRenderer, Minecraft.getMinecraft().getTextureManager(), itm, 80, currenty);
    }

    @Override
    public Panel createGui(Minecraft mc, Gui gui, NBTTagCompound currentData, ModuleGuiChanged moduleGuiChanged) {
        Panel panel = new Panel(mc, gui).setLayout(new VerticalLayout());
        return panel;
    }
}
