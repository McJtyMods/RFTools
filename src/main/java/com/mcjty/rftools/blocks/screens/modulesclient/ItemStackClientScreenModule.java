package com.mcjty.rftools.blocks.screens.modulesclient;

import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.rftools.blocks.screens.ModuleGuiChanged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

public class ItemStackClientScreenModule implements ClientScreenModule {
    private RenderItem itemRender = new RenderItem();
    private RenderBlocks renderBlocks = new RenderBlocks();

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.ITEM;
    }

    @Override
    public int getHeight() {
        return 30;
    }

    @Override
    public void render(FontRenderer fontRenderer, int currenty, String[] screenData) {
        short short1 = 240;
        short short2 = 240;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.enableGUIStandardItemLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, short1 / 1.0F, short2 / 1.0F);
        ItemStack itm = new ItemStack(Blocks.cobblestone, 32);
        int x = 20;
        renderItemStack(fontRenderer, currenty, itm, x);

        itm = new ItemStack(Blocks.dirt, 20);
        x += 30;
        renderItemStack(fontRenderer, currenty, itm, x);

        itm = new ItemStack(Blocks.glowstone, 64);
        x += 30;
        renderItemStack(fontRenderer, currenty, itm, x);
    }

    private void renderItemStack(FontRenderer fontRenderer, int currenty, ItemStack itm, int x) {
        if (!ForgeHooksClient.renderInventoryItem(renderBlocks, Minecraft.getMinecraft().getTextureManager(), itm, true, 0.0F, x, currenty)) {
            itemRender.renderItemIntoGUI(fontRenderer, Minecraft.getMinecraft().getTextureManager(), itm, x, currenty);
        }
        itemRender.renderItemOverlayIntoGUI(fontRenderer, Minecraft.getMinecraft().getTextureManager(), itm, x, currenty);
    }

    @Override
    public Panel createGui(Minecraft mc, Gui gui, NBTTagCompound currentData, ModuleGuiChanged moduleGuiChanged) {
        Panel panel = new Panel(mc, gui).setLayout(new VerticalLayout());
        return panel;
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {

    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
