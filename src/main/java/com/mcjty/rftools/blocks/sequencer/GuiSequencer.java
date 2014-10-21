package com.mcjty.rftools.blocks.sequencer;

import com.mcjty.gui.Window;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.Widget;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;

public class GuiSequencer extends GuiScreen {
    public static final int SEQUENCER_WIDTH = 140;
    public static final int SEQUENCER_HEIGHT = 50;

    private Window window;

    private final SequencerTileEntity sequencerTileEntity;

    public GuiSequencer(SequencerTileEntity sequencerTileEntity) {
        this.sequencerTileEntity = sequencerTileEntity;
    }

    @Override
    public void initGui() {
        super.initGui();
        int k = (this.width - SEQUENCER_WIDTH) / 2;
        int l = (this.height - SEQUENCER_HEIGHT) / 2;

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());
        toplevel.setBounds(new Rectangle(k, l, SEQUENCER_WIDTH, SEQUENCER_HEIGHT));
        window = new Window(this, toplevel);
    }


}
