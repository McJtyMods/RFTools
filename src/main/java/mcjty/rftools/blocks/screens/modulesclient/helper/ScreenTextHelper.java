package mcjty.rftools.blocks.screens.modulesclient.helper;

import mcjty.rftools.api.screens.ITextRenderHelper;
import mcjty.rftools.api.screens.ModuleRenderInfo;
import mcjty.rftools.api.screens.TextAlign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class ScreenTextHelper implements ITextRenderHelper {

    private boolean large = false;
    private TextAlign align = TextAlign.ALIGN_LEFT;

    private boolean dirty = true;
    private int textx;
    private String text;
    private boolean truetype = false;

    public int getTextx() {
        return textx;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setDirty() {
        this.dirty = true;
    }

    @Override
    public boolean isLarge() {
        return large;
    }

    @Override
    public ITextRenderHelper large(boolean large) {
        dirty = true;
        this.large = large;
        return this;
    }

    @Override
    public TextAlign getAlign() {
        return align;
    }

    @Override
    public ITextRenderHelper align(TextAlign align) {
        dirty = true;
        this.align = align;
        return this;
    }

    @Override
    public void setup(String line, int width, ModuleRenderInfo renderInfo) {
        if ((!dirty) && truetype == (renderInfo.font != null)) {
            return;
        }
        dirty = false;
        truetype = renderInfo.font != null;
        if (renderInfo.font != null) {
            textx = large ? 3 : 7;
            text = renderInfo.font.trimStringToWidth(line, (large ? (width/2) : width)-textx);
//            int w = large ? 240 : 472;
            int w = large ? (int) (width / 2.13f) : (int) (width / 1.084f);
            switch (align) {
                case ALIGN_LEFT:
                    break;
                case ALIGN_CENTER:
                    textx += (int)((w -textx - renderInfo.font.getWidth(text)) / 2) / 4;
                    break;
                case ALIGN_RIGHT:
                    textx += (int)(w -textx - renderInfo.font.getWidth(text)) / 4;
                    break;
            }
        } else {
            textx = large ? 4 : 7;
            FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
            text = fontRenderer.trimStringToWidth(line, (large ? (width/8) : (width/4))-textx);
//            int w = large ? 58 : 115;
            int w = large ? (int) (width / 8.8f) : (int) (width / 4.45f);
            switch (align) {
                case ALIGN_LEFT:
                    break;
                case ALIGN_CENTER:
                    textx += (w - fontRenderer.getStringWidth(text)) / 2;
                    break;
                case ALIGN_RIGHT:
                    textx += w - fontRenderer.getStringWidth(text);
                    break;
            }
        }
    }

    @Override
    public void renderText(int x, int y, int color, ModuleRenderInfo renderInfo) {
        if (renderInfo.font != null) {
            float r = (color >> 16 & 255) / 255.0f;
            float g = (color >> 8 & 255) / 255.0f;
            float b = (color & 255) / 255.0f;
            renderInfo.font.drawString(textx + x, 128 - y, text, 0.25f, 0.25f, -512f-40f, r, g, b, 1.0f);
        } else {
            FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
            fontRenderer.drawString(text, textx + x, y, color);
        }
    }
}
