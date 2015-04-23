package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.gui.RenderHelper;
import mcjty.gui.events.ChoiceEvent;
import mcjty.gui.widgets.ChoiceLabel;
import mcjty.gui.widgets.Widget;
import mcjty.rftools.blocks.screens.ModuleGuiChanged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.NBTTagCompound;

import java.text.DecimalFormat;

public class ClientScreenModuleHelper {

    public static void renderLevel(FontRenderer fontRenderer, int xoffset, int currenty, Object[] screenData, String label, boolean hidebar, boolean hidetext, boolean showpct, boolean showdiff,
                                   int poscolor, int negcolor,
                                   int gradient1, int gradient2, FormatStyle formatStyle) {
        if (screenData == null) {
            return;
        }

        long maxContents = 0;
        try {
            maxContents = (Long) screenData[1];
        } catch (Exception e) {
            return;
        }
        if (maxContents > 0) {
            if (!hidebar) {
                long contents = (Long) screenData[0];

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
                long diff = (Long) screenData[2];
                if (diff < 0) {
                    fontRenderer.drawString(diff + " " + label + "/t", xoffset, currenty, negcolor);
                } else {
                    fontRenderer.drawString("+" + diff + " " + label + "/t", xoffset, currenty, poscolor);
                }

            } else if (maxContents > 0) {
                long contents = (Long) screenData[0];
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

    public static String format(String in, FormatStyle style) {
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
