package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.gui.widgets.Panel;
import mcjty.rftools.blocks.screens.ModuleGuiChanged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ClockClientScreenModule implements ClientScreenModule {
    private int color = 0xffffff;
    private String line = "";
    private boolean large = false;

    @Override
    public TransformMode getTransformMode() {
        return large ? TransformMode.TEXTLARGE : TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return large ? 20 : 10;
    }

    @Override
    public void render(FontRenderer fontRenderer, int currenty, Object[] screenData, float factor) {
        GL11.glDisable(GL11.GL_LIGHTING);
        Minecraft minecraft = Minecraft.getMinecraft();
        double time = 0.0D;

        if (minecraft.theWorld != null && minecraft.thePlayer != null) {
            if (minecraft.theWorld.provider.isSurfaceWorld()) {
                time = minecraft.theWorld.getCelestialAngle(1.0F);
            } else {
                time = Math.random();
            }
        }
        int minutes = (int) (time * ((24 * 60) - 0.1f));
        int hours = minutes / 60;
        hours = (hours + 12) % 24;
        minutes = minutes % 60;
        String timeString;
        if (hours < 10) {
            timeString = "0" + hours;
        } else {
            timeString = Integer.toString(hours);
        }
        timeString += ':';
        if (minutes < 10) {
            timeString += "0" + minutes;
        } else {
            timeString += Integer.toString(minutes);
        }

        if (large) {
            fontRenderer.drawString(line + " " + timeString, 4, currenty / 2 + 1, color);
        } else {
            fontRenderer.drawString(line + " " + timeString, 7, currenty, color);
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public Panel createGui(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged) {
        return new ScreenModuleGuiBuilder(mc, gui, currentData, moduleGuiChanged).
                label("Label:").text("text", "Label text").color("color", "Label color").nl().
                toggle("large", "Large", "Large or small font").nl().
                build();
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            if (tagCompound.hasKey("color")) {
                color = tagCompound.getInteger("color");
            } else {
                color = 0xffffff;
            }
            large = tagCompound.getBoolean("large");
        }
    }

    @Override
    public boolean needsServerData() {
        return false;
    }
}
