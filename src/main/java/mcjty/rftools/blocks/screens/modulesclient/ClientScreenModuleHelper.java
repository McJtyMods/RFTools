package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.gui.RenderHelper;
import mcjty.rftools.api.screens.FormatStyle;
import mcjty.rftools.api.screens.IModuleRenderHelper;
import mcjty.rftools.api.screens.data.IModuleDataContents;
import net.minecraft.client.gui.FontRenderer;

import java.text.DecimalFormat;

public class ClientScreenModuleHelper implements IModuleRenderHelper {

    @Override
    public void renderLevel(FontRenderer fontRenderer, int xoffset, int currenty, IModuleDataContents screenData, String label, boolean hidebar, boolean hidetext, boolean showpct, boolean showdiff,
                                   int poscolor, int negcolor,
                                   int gradient1, int gradient2, FormatStyle formatStyle) {
        if (screenData == null) {
            return;
        }

        long maxContents  = screenData.getMaxContents();
        if (maxContents > 0) {
            if (!hidebar) {
                long contents = screenData.getContents();

                int width = 80 - xoffset + 7 + 40;
                long value = contents * width / maxContents;
                if (value < 0) {
                    value = 0;
                } else if (value > width) {
                    value = width;
                }
                RenderHelper.drawHorizontalGradientRect(xoffset, currenty, (int) (xoffset + value), currenty + 8, gradient1, gradient2);
            }
        }
        if (!hidetext) {
            if (showdiff) {
                long diff = screenData.getLastPerTick();
                if (diff < 0) {
                    fontRenderer.drawString(diff + " " + label + "/t", xoffset, currenty, negcolor);
                } else {
                    fontRenderer.drawString("+" + diff + " " + label + "/t", xoffset, currenty, poscolor);
                }

            } else if (maxContents > 0) {
                long contents = screenData.getContents();
                if (showpct) {
                    long value = contents * 100 / maxContents;
                    if (value < 0) {
                        value = 0;
                    } else if (value > 100) {
                        value = 100;
                    }
                    fontRenderer.drawString(value + "%", xoffset, currenty, poscolor);
                } else {
                    fontRenderer.drawString(format(String.valueOf(contents), formatStyle) + label, xoffset, currenty, poscolor);
                }
            }
        }
    }

    private static DecimalFormat dfCommas = new DecimalFormat("###,###");

    @Override
    public String format(String in, FormatStyle style) {
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
                char pre = "KMGTP".charAt(exp-1);
                return String.format("%.1f %s", contents / Math.pow(unit, exp), pre);
            }
            case MODE_COMMAS:
                return dfCommas.format(Long.parseLong(in));
        }
        return in;
    }

}
