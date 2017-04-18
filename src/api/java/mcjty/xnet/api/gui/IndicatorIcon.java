package mcjty.xnet.api.gui;

import net.minecraft.util.ResourceLocation;

public class IndicatorIcon {
    private final ResourceLocation image;
    private final int u;
    private final int v;
    private final int iw;
    private final int ih;

    public IndicatorIcon(ResourceLocation image, int u, int v, int iw, int ih) {
        this.image = image;
        this.u = u;
        this.v = v;
        this.iw = iw;
        this.ih = ih;
    }

    public ResourceLocation getImage() {
        return image;
    }

    public int getU() {
        return u;
    }

    public int getV() {
        return v;
    }

    public int getIw() {
        return iw;
    }

    public int getIh() {
        return ih;
    }
}
