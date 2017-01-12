package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.rftools.api.screens.ModuleRenderInfo;
import mcjty.rftools.proxy.ClientProxy;
import net.minecraft.client.gui.FontRenderer;

public class ScreenTextCache {

    private boolean large = false;
    private int align = 0;  // 0 == left, 1 == center, 2 == right

    private boolean dirty = true;
    private int textx;
    private String text;
    private boolean truetype = false;

    public int getTextx() {
        return textx;
    }

    public String getText() {
        return text;
    }

    public void setDirty() {
        this.dirty = true;
    }

    public boolean isLarge() {
        return large;
    }

    public void setLarge(boolean large) {
        this.large = large;
    }

    public int getAlign() {
        return align;
    }

    public void setAlign(int align) {
        this.align = align;
    }

    public void setup(FontRenderer fontRenderer, String line, int width, ModuleRenderInfo renderInfo) {
        if ((!dirty) && truetype == renderInfo.truetype) {
            return;
        }
        dirty = false;
        truetype = renderInfo.truetype;
        if (renderInfo.truetype) {
            textx = large ? 3 : 7;
            text = ClientProxy.font.trimStringToWidth(line, (large ? (width/2) : width)-textx);
//            int w = large ? 240 : 472;
            int w = large ? (int) (width / 2.13f) : (int) (width / 1.084f);
            switch (align) {
                case 0:
                    break;
                case 1:
                    textx += (int)((w -textx - ClientProxy.font.getWidth(text)) / 2) / 4;
                    break;
                case 2:
                    textx += (int)(w -textx - ClientProxy.font.getWidth(text)) / 4;
                    break;
            }
        } else {
            textx = large ? 4 : 7;
            text = fontRenderer.trimStringToWidth(line, (large ? (width/8) : (width/4))-textx);
//            int w = large ? 58 : 115;
            int w = large ? (int) (width / 8.8f) : (int) (width / 4.45f);
            switch (align) {
                case 0:
                    break;
                case 1:
                    textx += (w - fontRenderer.getStringWidth(text)) / 2;
                    break;
                case 2:
                    textx += w - fontRenderer.getStringWidth(text);
                    break;
            }
        }
    }

    public void renderText(FontRenderer fontRenderer, int color, int x, int y, ModuleRenderInfo renderInfo) {
        if (renderInfo.truetype) {
            float r = (color >> 16 & 255) / 255.0f;
            float g = (color >> 8 & 255) / 255.0f;
            float b = (color & 255) / 255.0f;
            ClientProxy.font.drawString(textx + x, 128 - y, text, 0.25f, 0.25f, -512f-40f, r, g, b, 1.0f);
        } else {
            fontRenderer.drawString(text, textx + x, y, color);
        }

    }

}
