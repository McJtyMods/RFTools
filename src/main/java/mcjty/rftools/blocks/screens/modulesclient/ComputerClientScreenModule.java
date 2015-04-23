package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.gui.widgets.Panel;
import mcjty.rftools.blocks.screens.ModuleGuiChanged;
import mcjty.rftools.blocks.screens.modules.ComputerScreenModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ComputerClientScreenModule implements ClientScreenModule {
    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(FontRenderer fontRenderer, int currenty, Object[] screenData, float factor) {
        GL11.glDisable(GL11.GL_LIGHTING);
        if (screenData != null) {
            int x = 7;
            for (Object o : screenData) {
                ComputerScreenModule.ColoredText ct = (ComputerScreenModule.ColoredText) o;
                fontRenderer.drawString(ct.getText(), x, currenty, ct.getColor());
                x += fontRenderer.getStringWidth(ct.getText());
            }
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public Panel createGui(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged) {
        return new ScreenModuleGuiBuilder(mc, gui, currentData, moduleGuiChanged).
                leftLabel("Contents of this module is").nl().
                leftLabel("controlled with a computer.").nl().
                leftLabel("Only works with OC or CC.").nl().
                label("Tag:").text("moduleTag", "Tag used by LUA to identify module").nl().
                build();
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
