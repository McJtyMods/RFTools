package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.gui.events.TextEvent;
import mcjty.gui.layout.HorizontalAlignment;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.layout.VerticalLayout;
import mcjty.gui.widgets.Label;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.TextField;
import mcjty.gui.widgets.Widget;
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
        Panel panel = new Panel(mc, gui).setLayout(new VerticalLayout());
        Label label1 = new Label(mc, gui).setText("Contents of this module is").setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
        Label label2 = new Label(mc, gui).setText("controlled with a computer.").setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
        Label label3 = new Label(mc, gui).setText("Only works with OpenComputers.").setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
        panel.addChild(label1).addChild(label2).addChild(label3);
        addOptionPanel(mc, gui, currentData, moduleGuiChanged, panel);
        return panel;
    }

    private void addOptionPanel(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged, Panel panel) {
        Panel optionPanel = new Panel(mc, gui).setLayout(new HorizontalLayout()).setDesiredHeight(16);
        Label label = new Label(mc, gui).setText("Tag:");
        TextField tagField = new TextField(mc, gui).setDesiredWidth(100);
        tagField.setText(currentData.getString("moduleTag"));
        tagField.addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                currentData.setString("moduleTag", newText);
                moduleGuiChanged.updateData();
            }
        });
        optionPanel.addChild(label).addChild(tagField);
        panel.addChild(optionPanel);
    }


    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
