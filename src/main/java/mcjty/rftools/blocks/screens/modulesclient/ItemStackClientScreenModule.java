package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.gui.widgets.Panel;
import mcjty.rftools.blocks.screens.ModuleGuiChanged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ItemStackClientScreenModule implements ClientScreenModule {
    private int slot1 = -1;
    private int slot2 = -1;
    private int slot3 = -1;
    private int slot4 = -1;

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.ITEM;
    }

    @Override
    public int getHeight() {
        return 22;
    }

    @Override
    public void render(FontRenderer fontRenderer, int currenty, Object[] screenData, float factor) {
        if (screenData == null) {
            return;
        }

        RenderHelper.enableGUIStandardItemLighting();
//        RenderHelper.enableStandardItemLighting();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        GL11.glDepthMask(true);

        boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        if (!lighting) {
            GL11.glEnable(GL11.GL_LIGHTING);
        }
        boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        if (!depthTest) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }

        GL11.glPushMatrix();
        float f3 = 0.0075F;
        GL11.glTranslatef(-0.5F, 0.5F, 0.06F);
        GL11.glScalef(f3 * factor, -f3 * factor, 0.0001f);

//        short short1 = 240;
//        short short2 = 240;
//        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, short1 / 1.0F, short2 / 1.0F);
        int x = 10;
        x = renderSlot(fontRenderer, currenty, screenData, slot1, 0, x);
        x = renderSlot(fontRenderer, currenty, screenData, slot2, 1, x);
        x = renderSlot(fontRenderer, currenty, screenData, slot3, 2, x);
        renderSlot(fontRenderer, currenty, screenData, slot4, 3, x);

        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, 0.5F, 0.08F);
        GL11.glScalef(f3 * factor, -f3 * factor, 0.0001f);

        x = 10;
        x = renderSlotOverlay(fontRenderer, currenty, screenData, slot1, 0, x);
        x = renderSlotOverlay(fontRenderer, currenty, screenData, slot2, 1, x);
        x = renderSlotOverlay(fontRenderer, currenty, screenData, slot3, 2, x);
        renderSlotOverlay(fontRenderer, currenty, screenData, slot4, 3, x);
        GL11.glPopMatrix();

        if (!lighting) {
            GL11.glDisable(GL11.GL_LIGHTING);
        }
        if (!depthTest) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }

        GL11.glDepthMask(false);
        RenderHelper.enableStandardItemLighting();
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    private int renderSlot(FontRenderer fontRenderer, int currenty, Object[] screenData, int slot, int index, int x) {
        if (slot != -1) {
            ItemStack itm = null;
            try {
                itm = (ItemStack) screenData[index];
            } catch (Exception e) {
                // Ignore this.
            }
            if (itm != null) {
                RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
                itemRender.renderItemAndEffectIntoGUI(itm, x, currenty);
            }
            x += 30;
        }
        return x;
    }

    private int renderSlotOverlay(FontRenderer fontRenderer, int currenty, Object[] screenData, int slot, int index, int x) {
        if (slot != -1) {
            ItemStack itm = null;
            try {
                itm = (ItemStack) screenData[index];
            } catch (Exception e) {
                // Ignore this.
            }
            if (itm != null) {
//                itemRender.renderItemOverlayIntoGUI(fontRenderer, Minecraft.getMinecraft().getTextureManager(), itm, x, currenty);
                renderItemOverlayIntoGUI(fontRenderer, itm, x, currenty);
            }
            x += 30;
        }
        return x;
    }

    private static void renderItemOverlayIntoGUI(FontRenderer fontRenderer, ItemStack itemStack, int x, int y) {
        if (itemStack != null) {
            int size = itemStack.stackSize;
            if (size > 1) {
                String s1;
                if (size < 10000) {
                    s1 = String.valueOf(size);
                } else if (size < 1000000) {
                    s1 = String.valueOf(size / 1000) + "k";
                } else if (size < 1000000000) {
                    s1 = String.valueOf(size / 1000000) + "m";
                } else {
                    s1 = String.valueOf(size / 1000000000) + "g";
                }
                GL11.glDisable(GL11.GL_LIGHTING);
//                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_BLEND);
                fontRenderer.drawString(s1, x + 19 - 2 - fontRenderer.getStringWidth(s1), y + 6 + 3, 16777215);
                GL11.glEnable(GL11.GL_LIGHTING);
//                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }

            if (itemStack.getItem().showDurabilityBar(itemStack)) {
                double health = itemStack.getItem().getDurabilityForDisplay(itemStack);
                int j1 = (int) Math.round(13.0D - health * 13.0D);
                int k = (int) Math.round(255.0D - health * 255.0D);
                GL11.glDisable(GL11.GL_LIGHTING);
//                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glDisable(GL11.GL_BLEND);
                Tessellator tessellator = Tessellator.getInstance();
                int l = 255 - k << 16 | k << 8;
                int i1 = (255 - k) / 4 << 16 | 16128;
                renderQuad(tessellator, x + 2, y + 13, 13, 2, 0, 0.0D);
                renderQuad(tessellator, x + 2, y + 13, 12, 1, i1, 0.02D);
                renderQuad(tessellator, x + 2, y + 13, j1, 1, l, 0.04D);
                //GL11.glEnable(GL11.GL_BLEND); // Forge: Disable Bled because it screws with a lot of things down the line.
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_LIGHTING);
//                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    private static void renderQuad(Tessellator tessellator, int x, int y, int width, int height, int color, double offset) {
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
//        tessellator.setColorOpaque_I(color);
        renderer.pos(x, y, offset);
        renderer.pos(x, (y + height), offset);
        renderer.pos((x + width), (y + height), offset);
        renderer.pos((x + width), y, offset);
        tessellator.draw();
    }


    @Override
    public Panel createGui(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged) {
        return new ScreenModuleGuiBuilder(mc, gui, currentData, moduleGuiChanged).
                label("Slot 1:").integer("slot1", "Slot index to show").nl().
                label("Slot 2:").integer("slot2", "Slot index to show").nl().
                label("Slot 3:").integer("slot3", "Slot index to show").nl().
                label("Slot 4:").integer("slot4", "Slot index to show").nl().
                build();
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            if (tagCompound.hasKey("slot1")) {
                slot1 = tagCompound.getInteger("slot1");
            }
            if (tagCompound.hasKey("slot2")) {
                slot2 = tagCompound.getInteger("slot2");
            }
            if (tagCompound.hasKey("slot3")) {
                slot3 = tagCompound.getInteger("slot3");
            }
            if (tagCompound.hasKey("slot4")) {
                slot4 = tagCompound.getInteger("slot4");
            }
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
