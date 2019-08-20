package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.varia.ItemStackList;
import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleRenderHelper;
import mcjty.rftools.api.screens.ModuleRenderInfo;
import mcjty.rftools.blocks.screens.modules.StorageControlScreenModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class StorageControlClientScreenModule implements IClientScreenModule<StorageControlScreenModule.ModuleDataStacks> {
    private ItemStackList stacks = ItemStackList.create(9);

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.ITEM;
    }

    @Override
    public int getHeight() {
        return 114;
    }

    @Override
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, StorageControlScreenModule.ModuleDataStacks screenData, ModuleRenderInfo renderInfo) {
        if (screenData == null) {
            return;
        }

        if (renderInfo.hitx >= 0) {
            GlStateManager.disableLighting();
            GlStateManager.pushMatrix();
            GlStateManager.translate(-0.5F, 0.5F, 0.07F);
            float f3 = 0.0105F;
            GlStateManager.scale(f3 * renderInfo.factor, -f3 * renderInfo.factor, f3);
            GL11.glNormal3f(0.0F, 0.0F, -1.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            int y = currenty;
            int i = 0;

            for (int yy = 0 ; yy < 3 ; yy++) {
                for (int xx = 0 ; xx < 3 ; xx++) {
                    if (!stacks.get(i).isEmpty()) {
                        int x = xx * 40;
                        boolean hilighted = renderInfo.hitx >= x+8 && renderInfo.hitx <= x + 38 && renderInfo.hity >= y-7 && renderInfo.hity <= y + 22;
                        if (hilighted) {
//                            mcjty.lib.client.RenderHelper.drawBeveledBox(5 + xx * 30, 10 + yy * 24 - 4, 29 + xx * 30, 10 + yy * 24 + 20, 0xffffffff, 0xffffffff, 0xff333333);
                            mcjty.lib.client.RenderHelper.drawFlatButtonBox((int) (5 + xx * 30.5f), 10 + yy * 24 - 4, (int) (29 + xx * 30.5f), 10 + yy * 24 + 20, 0xffffffff, 0xff333333, 0xffffffff);
                        }
                    }
                    i++;
                }
                y += 35;
            }
            GlStateManager.popMatrix();
        }

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.depthMask(true);

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();

        GlStateManager.pushMatrix();
        float f3 = 0.0105F;
        GlStateManager.translate(-0.5F, 0.5F, 0.06F);
        float factor = renderInfo.factor;
        GlStateManager.scale(f3 * factor, -f3 * factor, 0.0001f);

        int y = currenty;
        int i = 0;

        for (int yy = 0 ; yy < 3 ; yy++) {
            for (int xx = 0 ; xx < 3 ; xx++) {
                if (!stacks.get(i).isEmpty()) {
                    int x = 7 + xx * 30;
                    renderSlot(y, stacks.get(i), x);
                }
                i++;
            }
            y += 24;
        }

        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.5F, 0.5F, 0.08F);
        f3 = 0.0050F;
        GlStateManager.scale(f3 * factor, -f3 * factor, 0.0001f);

        y = currenty + 30;
        i = 0;

        for (int yy = 0 ; yy < 3 ; yy++) {
            for (int xx = 0 ; xx < 3 ; xx++) {
                if (!stacks.get(i).isEmpty()) {
                    renderSlotOverlay(fontRenderer, y, stacks.get(i), screenData.getAmount(i), 42 + xx * 64);
                }
                i++;
            }
            y += 52;
        }

        GlStateManager.disableLighting();

        boolean insertStackActive = renderInfo.hitx >= 0 && renderInfo.hitx < 60 && renderInfo.hity > 98 && renderInfo.hity <= 120;
        fontRenderer.drawString("Insert Stack", 20, y - 20, insertStackActive ? 0xffffff : 0x666666);
        boolean insertAllActive = renderInfo.hitx >= 60 && renderInfo.hitx <= 120 && renderInfo.hity > 98 && renderInfo.hity <= 120;
        fontRenderer.drawString("Insert All", 120, y - 20, insertAllActive ? 0xffffff : 0x666666);

        GlStateManager.popMatrix();

        RenderHelper.enableStandardItemLighting();
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    private void renderSlot(int currenty, ItemStack stack, int x) {
        RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
        itemRender.renderItemAndEffectIntoGUI(stack, x, currenty);
    }

    private void renderSlotOverlay(FontRenderer fontRenderer, int currenty, ItemStack stack, int amount, int x) {
//                itemRender.renderItemOverlayIntoGUI(fontRenderer, Minecraft.getMinecraft().getTextureManager(), itm, x, currenty);
        renderItemOverlayIntoGUI(fontRenderer, stack, amount, x, currenty);
    }

    private static void renderItemOverlayIntoGUI(FontRenderer fontRenderer, ItemStack itemStack, int size, int x, int y) {
        if (!itemStack.isEmpty()) {
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
            GlStateManager.disableLighting();
            GlStateManager.disableBlend();
            fontRenderer.drawString(s1, x + 19 - 2 - fontRenderer.getStringWidth(s1), y + 6 + 3, 16777215);
            GlStateManager.enableLighting();

            if (itemStack.getItem().showDurabilityBar(itemStack)) {
                double health = itemStack.getItem().getDurabilityForDisplay(itemStack);
                int j1 = (int) Math.round(13.0D - health * 13.0D);
                int k = (int) Math.round(255.0D - health * 255.0D);
                GlStateManager.disableLighting();
                GlStateManager.disableTexture2D();
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
                Tessellator tessellator = Tessellator.getInstance();
                int l = 255 - k << 16 | k << 8;
                int i1 = (255 - k) / 4 << 16 | 16128;
                renderQuad(tessellator, x + 2, y + 13, 13, 2, 0, 0.0D);
                renderQuad(tessellator, x + 2, y + 13, 12, 1, i1, 0.02D);
                renderQuad(tessellator, x + 2, y + 13, j1, 1, l, 0.04D);
                GlStateManager.enableAlpha();
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    private static void renderQuad(Tessellator tessellator, int x, int y, int width, int height, int color, double offset) {
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
//        tessellator.setColorOpaque_I(color);
        buffer.pos(x, y, offset);
        buffer.pos(x, (y + height), offset);
        buffer.pos((x + width), (y + height), offset);
        buffer.pos((x + width), y, offset);
        tessellator.draw();
    }


    @Override
    public void setupFromNBT(CompoundNBT tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            for (int i = 0 ; i < stacks.size() ; i++) {
                if (tagCompound.hasKey("stack"+i)) {
                    stacks.set(i, new ItemStack(tagCompound.getCompoundTag("stack" + i)));
                }
            }
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
