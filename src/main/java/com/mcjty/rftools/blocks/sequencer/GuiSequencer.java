package com.mcjty.rftools.blocks.sequencer;

import com.mcjty.gui.Window;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.TextField;
import com.mcjty.rftools.RFTools;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiSequencer extends GuiScreen {
    public static final int SEQUENCER_WIDTH = 160;
    public static final int SEQUENCER_HEIGHT = 184;

    private Window window;
    private List<ImageChoiceLabel> bits = new ArrayList<ImageChoiceLabel>();
    private ChoiceLabel mode;
    private TextField speedField;

    private final SequencerTileEntity sequencerTileEntity;

    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiSequencer(SequencerTileEntity sequencerTileEntity) {
        this.sequencerTileEntity = sequencerTileEntity;
    }

    @Override
    public void initGui() {
        super.initGui();
        int k = (this.width - SEQUENCER_WIDTH) / 2;
        int l = (this.height - SEQUENCER_HEIGHT) / 2;

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        for (int row = 0 ; row < 8 ; row++) {
            Panel rowPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredHeight(13);
            toplevel.addChild(rowPanel);
            for (int col = 0 ; col < 8 ; col++) {
                ImageChoiceLabel choiceLabel = new ImageChoiceLabel(mc, this).
                        setDesiredHeight(12).
                        addChoice("0", "Disabled", iconGuiElements, 160, 0).
                        addChoice("1", "Enabled", iconGuiElements, 176, 0);
                bits.add(choiceLabel);
                rowPanel.addChild(choiceLabel);
            }
        }

        Button clearButton = new Button(mc, this).setText("Clear").setTooltips("Clear the grid").setDesiredHeight(13).setDesiredWidth(45);
        Button fillButton = new Button(mc, this).setText("Fill").setTooltips("Fill the grid").setDesiredHeight(13).setDesiredWidth(45);
        Panel buttonPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(clearButton).addChild(fillButton);
        toplevel.addChild(buttonPanel);

        mode = new ChoiceLabel(mc, this).addChoices("Once", "Loop", "Loop Red").setDesiredHeight(13).setDesiredWidth(60);
        mode.setChoiceTooltip("Once", "When a redstone signal is", "received, loop the cycle once");
        mode.setChoiceTooltip("Loop", "Loop the cycle all the time");
        mode.setChoiceTooltip("Loop Red", "Loop the cycle all the time", "but only when redstone signal", "is received");
        Label label = new Label(mc, this).setText("Ticks:");
        speedField = new TextField(mc, this);
        Panel bottomPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(mode).addChild(label).addChild(speedField);
        toplevel.addChild(bottomPanel);


        toplevel.setBounds(new Rectangle(k, l, SEQUENCER_WIDTH, SEQUENCER_HEIGHT));
        window = new Window(this, toplevel);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        window.mouseClicked(x, y, button);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        window.handleMouseInput();
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);
        window.mouseMovedOrUp(x, y, button);
    }

    @Override
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);

        window.draw();
        java.util.List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x, y, mc.fontRenderer);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        window.keyTyped(typedChar, keyCode);
    }

}
