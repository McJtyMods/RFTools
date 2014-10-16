package com.mcjty.rftools.items.manual;

import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.TextPage;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.rftools.RFTools;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GuiRFToolsManual extends GuiScreen {

    /** The X size of the window in pixels. */
    protected int xSize = 356;
    /** The Y size of the window in pixels. */
    protected int ySize = 180;

    private int pageIndex = 0;
    private Window window;
    private TextPage textPage;
    private Button prevButton;
    private Button nextButton;

    private static final ResourceLocation manualText = new ResourceLocation(RFTools.MODID, "docs/manual.txt");

    private final List<TextPage.Page> pages = new ArrayList<TextPage.Page>();

    public GuiRFToolsManual() {
    }

    private void newPage(TextPage.Page page) {
        if (!page.isEmpty()) {
            pages.add(page);
        }
    }

    static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private void readManual() {
        TextPage.Page page = new TextPage.Page();
        try {
            IResourceManager resourceManager = mc.getResourceManager();
            IResource iresource = resourceManager.getResource(manualText);
            InputStream inputstream = iresource.getInputStream();
            String manualText = convertStreamToString(inputstream);
            String[] lines = manualText.split("\n");

            for (String line : lines) {
                if ("---".equals(line)) {
                    newPage(page);
                    page = new TextPage.Page();
                } else {
                    page.addLine(line);
                }
            }
            newPage(page);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        readManual();

        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        textPage = new TextPage(mc, this).setPage(pages.get(pageIndex));

        prevButton = new Button(mc, this).setText("<").addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                prevPage();
            }
        });
        nextButton = new Button(mc, this).setText(">").addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                nextPage();
            }
        });
        Panel buttonPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredHeight(16).addChild(prevButton).addChild(nextButton);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout()).addChild(textPage).addChild(buttonPanel);
        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void prevPage() {
        pageIndex--;
        if (pageIndex < 0) {
            pageIndex = 0;
        }
        textPage.setPage(pages.get(pageIndex));
    }

    private void nextPage() {
        pageIndex++;
        if (pageIndex >= pages.size()) {
            pageIndex = pages.size()-1;
        }
        textPage.setPage(pages.get(pageIndex));
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
        if (!window.keyTyped(typedChar, keyCode)) {
            if (keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_LEFT) {
                prevPage();
            } else if (keyCode == Keyboard.KEY_SPACE || keyCode == Keyboard.KEY_RIGHT) {
                nextPage();
            }
        }
    }

    @Override
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);

        prevButton.setEnabled(pageIndex > 0);
        nextButton.setEnabled(pageIndex < pages.size()-1);

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
