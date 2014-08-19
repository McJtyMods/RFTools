package com.mcjty.gui;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class BlockRender extends AbstractWidget<BlockRender> {
    private Object renderItem = null;
    private RenderItem itemRender;

    public Object getRenderItem() {
        return renderItem;
    }

    public BlockRender setRenderItem(Object renderItem) {
        this.renderItem = renderItem;
        return this;
    }

    public BlockRender(Minecraft mc, Gui gui) {
        super(mc, gui);
        itemRender = new RenderItem();
    }

    @Override
    public void draw(int x, int y) {
        super.draw(x, y);
        if (renderItem != null) {
            renderObject(x + bounds.x, y + bounds.y, renderItem, false);
        }
    }

    public boolean renderObject(int x, int y, Object itm, boolean highlight) {
        return renderObject(x, y, itm, highlight, 150);
    }

    public boolean renderObject(int x, int y, Object itm, boolean highlight, float lvl) {
        itemRender.zLevel = lvl;

        if (itm==null) {
            return renderItemStack(null, x, y, "", highlight);
        }
        if (itm instanceof Item) {
            return renderItemStack(new ItemStack((Item) itm, 1), x, y, "", highlight);
        }
        if (itm instanceof Block) {
            return renderItemStack(new ItemStack((Block) itm, 1), x, y, "", highlight);
        }
        if (itm instanceof ItemStack) {
            return renderItemStackWithCount((ItemStack) itm, x, y, highlight);
        }
        return renderItemStack(null,x,y,"",highlight);
    }

    public boolean renderItemStackWithCount(ItemStack itm, int xo, int yo, boolean highlight) {
        if (itm.stackSize==1) {
            return renderItemStack(itm, xo, yo, "", highlight);
        } else {
            return renderItemStack(itm, xo, yo, "" + itm.stackSize, highlight);
        }
    }

    public boolean renderItemStack(ItemStack itm, int x, int y, String txt, boolean highlight){
        GL11.glColor3f(1F, 1F, 1F);

        boolean isLightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
        boolean isRescaleNormalEnabled = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);

        if (highlight){
            GL11.glDisable(GL11.GL_LIGHTING);
//            drawGradientRect(x, y, x+16, y+16, 0x80ffffff, 0xffffffff);
        }
        if (itm==null) {
            return false;
        }
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0.0F, 32.0F);
        GL11.glColor3f(1F, 1F, 1F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_LIGHTING);
        itemRender.renderItemAndEffectIntoGUI(mc.fontRenderer, this.mc.getTextureManager(), itm, x, y);
        itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, this.mc.getTextureManager(), itm, x, y, txt);
        GL11.glPopMatrix();

        if (isLightingEnabled) {
            GL11.glEnable(GL11.GL_LIGHTING);
        } else {
            GL11.glDisable(GL11.GL_LIGHTING);
        }
        if (isRescaleNormalEnabled) {
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        } else {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }

        return true;
    }

}
