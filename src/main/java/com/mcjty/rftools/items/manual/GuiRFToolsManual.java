package com.mcjty.rftools.items.manual;

import com.mcjty.gui.Window;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.TextPage;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.RFTools;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class GuiRFToolsManual extends GuiScreen {

    /** The X size of the window in pixels. */
    protected int xSize = 356;
    /** The Y size of the window in pixels. */
    protected int ySize = 180;

    private int pageIndex = 0;
    private Window window;

//    private static final ResourceLocation manualText = new ResourceLocation(RFTools.MODID, "docs/manual.txt");


    public GuiRFToolsManual() {
    }

    @Override
    public void initGui() {
        super.initGui();

        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;


        TextPage textPage = new TextPage(mc, this).
                addLine("ºlThis is the first line").
                addLine("This is the second line").
                addLine("This is the third line").
                addLine("This is the first line").
                addLine("This is the second line").
                addLine("This is the third line").
                addLine("This is the first line").
                addLine("This is the second line").
                addLine("This is the third line").
                addLine("This is the first line").
                addLine("This is the second line");

        Button prevButton = new Button(mc, this).setText("<");
        Button nextButton = new Button(mc, this).setText(">");
        Panel buttonPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredHeight(16).addChild(prevButton).addChild(nextButton);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout()).addChild(textPage).addChild(buttonPanel);
        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));

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
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        window.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);

        window.draw();
        java.util.List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x-guiLeft, y-guiTop, mc.fontRenderer);
        }
    }
}
