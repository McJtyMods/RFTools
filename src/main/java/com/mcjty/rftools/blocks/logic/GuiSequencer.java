package com.mcjty.rftools.blocks.logic;

import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.events.ChoiceEvent;
import com.mcjty.gui.events.TextEvent;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.TextField;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketServerCommand;
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
                final int bit = row * 8 + col;
                ImageChoiceLabel choiceLabel = new ImageChoiceLabel(mc, this).
                        addChoiceEvent(new ChoiceEvent() {
                            @Override
                            public void choiceChanged(Widget parent, String newChoice) {
                                changeBit(bit, newChoice);
                            }
                        }).
                        setDesiredHeight(12).
                        addChoice("0", "Disabled", iconGuiElements, 160, 0).
                        addChoice("1", "Enabled", iconGuiElements, 176, 0);
                choiceLabel.setCurrentChoice(sequencerTileEntity.getCycleBit(bit) ? 1 : 0);
                bits.add(choiceLabel);
                rowPanel.addChild(choiceLabel);
            }
        }

        Button clearButton = new Button(mc, this).setText("Clear").setTooltips("Clear the grid").setDesiredHeight(13).setDesiredWidth(45).addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                fillGrid(false);
            }
        });
        Button fillButton = new Button(mc, this).setText("Fill").setTooltips("Fill the grid").setDesiredHeight(13).setDesiredWidth(45).addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                fillGrid(true);
            }
        });
        Panel buttonPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(clearButton).addChild(fillButton);
        toplevel.addChild(buttonPanel);

        mode = new ChoiceLabel(mc, this).
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        changeMode();
                    }
                }).setDesiredHeight(13).setDesiredWidth(55);
        for (SequencerMode m : SequencerMode.values()) {
            mode.addChoices(m.getDescription());
        }
        mode.setChoiceTooltip(SequencerMode.MODE_ONCE1.getDescription(), "When a redstone signal is", "received, loop the cycle once.", "Ignore further pulses");
        mode.setChoiceTooltip(SequencerMode.MODE_ONCE2.getDescription(), "When a redstone signal is", "received, loop the cycle once.", "Restart if new pulse arrives");
        mode.setChoiceTooltip(SequencerMode.MODE_LOOP1.getDescription(), "Loop the cycle all the time.", "Ignore redstone signals");
        mode.setChoiceTooltip(SequencerMode.MODE_LOOP2.getDescription(), "Loop the cycle all the time.", "Restart on redstone pulse");
        mode.setChoiceTooltip(SequencerMode.MODE_LOOP3.getDescription(), "Loop the cycle when redstone.", "signal is present");
        mode.setChoiceTooltip(SequencerMode.MODE_STEP.getDescription(), "Do one step in the cycle", "for every redstone pulse");
        mode.setChoice(sequencerTileEntity.getMode().getDescription());
        Label label = new Label(mc, this).setText("Delay:");

        speedField = new TextField(mc, this).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                setDelay();
            }
        });
        int delay = sequencerTileEntity.getDelay();
        if (delay <= 0) {
            delay = 1;
        }
        speedField.setText(String.valueOf(delay));

        Panel bottomPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(mode).addChild(label).addChild(speedField);
        toplevel.addChild(bottomPanel);

        toplevel.setBounds(new Rectangle(k, l, SEQUENCER_WIDTH, SEQUENCER_HEIGHT));
        window = new Window(this, toplevel);
    }

    private void setDelay() {
        String d = speedField.getText();
        int delay = 1;
        try {
            delay = Integer.parseInt(d);
        } catch (NumberFormatException e) {
            delay = 1;
        }
        sequencerTileEntity.setDelay(delay);
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(sequencerTileEntity.xCoord, sequencerTileEntity.yCoord, sequencerTileEntity.zCoord,
                SequencerTileEntity.CMD_SETDELAY,
                new Argument("delay", delay)));
    }

    private void fillGrid(boolean value) {
        for (int bit = 0 ; bit < 64 ; bit++) {
            bits.get(bit).setCurrentChoice(value ? 1 : 0);
        }
        sequencerTileEntity.setCycleBits(0, 63, value);
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(sequencerTileEntity.xCoord, sequencerTileEntity.yCoord, sequencerTileEntity.zCoord,
                SequencerTileEntity.CMD_SETBITS,
                new Argument("start", 0),
                new Argument("stop", 63),
                new Argument("choice", value)));
    }

    private void changeBit(int bit, String choice) {
        boolean newChoice = "1".equals(choice);
        sequencerTileEntity.setCycleBit(bit, newChoice);
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(sequencerTileEntity.xCoord, sequencerTileEntity.yCoord, sequencerTileEntity.zCoord,
                SequencerTileEntity.CMD_SETBIT,
                new Argument("bit", bit),
                new Argument("choice", newChoice)));
    }

    private void changeMode() {
        Integer newMode = SequencerMode.modeToMode.get(mode.getCurrentChoice());
        sequencerTileEntity.setMode(SequencerMode.values()[newMode]);
        PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(sequencerTileEntity.xCoord, sequencerTileEntity.yCoord, sequencerTileEntity.zCoord,
                SequencerTileEntity.CMD_MODE,
                new Argument("mode", newMode)));
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
