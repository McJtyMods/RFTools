package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.proxy.ClientProxy;
import net.minecraft.client.gui.FontRenderer;

public class ScreenTextCache {

    private boolean large = false;
    private int align = 0;

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

    public void setup(FontRenderer fontRenderer, String line) {
        if ((!dirty) && truetype == ScreenConfiguration.useTruetype) {
            return;
        }
        dirty = false;
        truetype = ScreenConfiguration.useTruetype;
        if (ScreenConfiguration.useTruetype) {
            int w = large ? 270 : 512;
            textx = large ? 3 : 7;
            text = ClientProxy.font.trimStringToWidth(line, w);
            switch (align) {
                case 0:
                    break;
                case 1:
                    textx += ((w-24 -textx - ClientProxy.font.getWidth(text)) / 2) / 4;
                    break;
                case 2:
                    textx += (w-24 -textx - ClientProxy.font.getWidth(text)) / 4;
                    break;
            }
        } else {
            int w = large ? 60 : 115;
            textx = large ? 4 : 7;
            text = fontRenderer.trimStringToWidth(line, w);
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

}
