package mcjty.rftools.items.manual;

import mcjty.gui.Window;
import mcjty.gui.events.ButtonEvent;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.layout.VerticalLayout;
import mcjty.gui.widgets.Button;
import mcjty.gui.widgets.Label;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.*;
import mcjty.rftools.RFTools;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class GuiRFToolsManual extends GuiScreen {

    /** The X size of the window in pixels. */
    private int xSize = 400;
    /** The Y size of the window in pixels. */
    private int ySize = 224;

    private Window window;
    private TextPage textPage;
    private Label pageLabel;
    private Button prevButton;
    private Button nextButton;

    public static int MANUAL_MAIN = 0;
    public static int MANUAL_DIMENSION = 1;
    private ResourceLocation manualText;

    // If this is set when the manual opens the given page will be located.
    public static String locatePage = null;

    private static final ResourceLocation manualMainText = new ResourceLocation(RFTools.MODID, "text/manual.txt");
    private static final ResourceLocation manualDimensionText = new ResourceLocation(RFTools.MODID, "text/manual_dimension.txt");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiRFToolsManual(int manual) {
        if (manual == MANUAL_MAIN) {
            manualText = manualMainText;
        } else {
            manualText = manualDimensionText;
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        textPage = new TextPage(RFTools.instance, mc, this).setText(manualText).setArrowImage(iconGuiElements, 144, 0).setCraftingGridImage(iconGuiElements, 0, 192);

        prevButton = new Button(mc, this).setText("<").addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                textPage.prevPage();
                window.setTextFocus(textPage);
            }
        });
        pageLabel = new Label(mc, this).setText("0 / 0");
        nextButton = new Button(mc, this).setText(">").addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                textPage.nextPage();
                window.setTextFocus(textPage);
            }
        });
        Panel buttonPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredHeight(16).addChild(prevButton).addChild(pageLabel).addChild(nextButton);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout()).addChild(textPage).addChild(buttonPanel);
        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));

        window = new Window(this, toplevel);
        window.setTextFocus(textPage);

        if (locatePage != null) {
            textPage.gotoNode(locatePage);
            locatePage = null;
        }
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

        int index = textPage.getPageIndex();
        int count = textPage.getPageCount();
        pageLabel.setText((index + 1) + "/" + count);
        prevButton.setEnabled(index > 0);
        nextButton.setEnabled(index < count - 1);

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
