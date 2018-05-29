package mcjty.rftools.blocks.screens.modulesclient.helper;

import mcjty.lib.client.RenderHelper;
import mcjty.rftools.api.screens.FormatStyle;
import mcjty.rftools.api.screens.ILevelRenderHelper;
import mcjty.rftools.api.screens.ModuleRenderInfo;
import mcjty.rftools.api.screens.data.IModuleDataContents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;

public class ScreenLevelHelper implements ILevelRenderHelper {

    private boolean hidebar = false;
    private boolean hidetext = false;
    private boolean showdiff = false;
    private boolean showpct = false;
    private FormatStyle formatStyle = FormatStyle.MODE_FULL;
    private int poscolor = 0xffffff;
    private int negcolor = 0xffffff;
    private int gradient1 = 0xffff0000;
    private int gradient2 = 0xff333300;
    private String label = "";


    @Override
    public void render(int x, int y, @Nullable IModuleDataContents data, @Nonnull ModuleRenderInfo renderInfo) {
        if (data == null) {
            return;
        }

        long maxContents  = data.getMaxContents();
        if (maxContents > 0) {
            if (!hidebar) {
                long contents = data.getContents();

                int width = 80 - x + 7 + 40;
                long value = contents * width / maxContents;
                if (value < 0) {
                    value = 0;
                } else if (value > width) {
                    value = width;
                }
                RenderHelper.drawHorizontalGradientRect(x, y, (int) (x + value), y + 8, gradient1, gradient2);
            }
        }
        if (!hidetext) {
            String diffTxt = null;
            int col = poscolor;
            if (showdiff) {
                long diff = data.getLastPerTick();
                if (diff < 0) {
                    col = negcolor;
                    diffTxt = diff + " " + label + "/t";
                } else {
                    diffTxt = "+" + diff + " " + label + "/t";
                }
            } else if (maxContents > 0) {
                long contents = data.getContents();
                if (showpct) {
                    long value = contents * 100 / maxContents;
                    if (value < 0) {
                        value = 0;
                    } else if (value > 100) {
                        value = 100;
                    }
                    diffTxt = value + "%";
                } else {
                    diffTxt = format(String.valueOf(contents), formatStyle) + label;
                }
            }
            if (diffTxt != null) {
                if (renderInfo.font != null) {
                    float r = (col >> 16 & 255) / 255.0f;
                    float g = (col >> 8 & 255) / 255.0f;
                    float b = (col & 255) / 255.0f;
                    renderInfo.font.drawString(x, 128 - y, diffTxt, 0.25f, 0.25f, -512f-40f, r, g, b, 1.0f);
                } else {
                    FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
                    fontRenderer.drawString(diffTxt, x, y, col);
                }
            }
        }
    }

    @Override
    public ILevelRenderHelper label(String label) {
        this.label = label;
        return this;
    }

    @Override
    public ILevelRenderHelper settings(boolean hidebar, boolean hidetext, boolean showpct, boolean showdiff) {
        this.hidebar = hidebar;
        this.hidetext = hidetext;
        this.showpct = showpct;
        this.showdiff = showdiff;
        return this;
    }

    @Override
    public ILevelRenderHelper color(int poscolor, int negcolor) {
        this.poscolor = poscolor;
        this.negcolor = negcolor;
        return this;
    }

    @Override
    public ILevelRenderHelper gradient(int gradient1, int gradient2) {
        this.gradient1 = gradient1;
        this.gradient2 = gradient2;
        return this;
    }

    @Override
    public ILevelRenderHelper format(FormatStyle formatStyle) {
        this.formatStyle = formatStyle;
        return this;
    }

    private static DecimalFormat dfCommas = new DecimalFormat("###,###");

    private String format(String in, FormatStyle style) {
        switch (style) {
            case MODE_FULL:
                return in;
            case MODE_COMPACT: {
                long contents = Long.parseLong(in);
                int unit = 1000;
                if (contents < unit) {
                    return in;
                }
                int exp = (int) (Math.log(contents) / Math.log(unit));
                char pre = "KMGTPEZY".charAt(exp-1);
                return String.format("%.1f %s", contents / Math.pow(unit, exp), pre);
            }
            case MODE_COMMAS:
                return dfCommas.format(Long.parseLong(in));
        }
        return in;
    }
}
