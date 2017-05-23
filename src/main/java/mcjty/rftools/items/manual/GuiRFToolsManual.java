package mcjty.rftools.items.manual;

import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.ButtonEvent;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.*;
import mcjty.rftools.RFTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;

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
    private ResourceLocation manualText;

    // If this is set when the manual opens the given page will be located.
    public static String locatePage = null;

    private static final ResourceLocation manualMainText = new ResourceLocation(RFTools.MODID, "text/manual.txt");
    private static final ResourceLocation manualDimensionText = new ResourceLocation(RFTools.MODID, "text/manual_dimension.txt");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiRFToolsManual(int manual) {
        String gameLocale = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode().toLowerCase(java.util.Locale.ENGLISH);
        if (manual == MANUAL_MAIN) {
            if (gameLocale.equals("en_us")) {
                manualText = manualMainText;
            } else {
                manualText = new ResourceLocation(RFTools.MODID, "text/manual-" + gameLocale + ".txt");
            }
        } else {
            if (gameLocale.equals("en_us")) {
                manualText = manualDimensionText;
            } else {
                manualText = new ResourceLocation(RFTools.MODID, "text/manual_dimension-" + gameLocale + ".txt");
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        textPage = new TextPage(RFTools.instance, mc, this).setText(manualText).setArrowImage(iconGuiElements, 144, 0).setCraftingGridImage(iconGuiElements, 0, 192);

        prevButton = new Button(mc, this).setText("<").addButtonEvent(parent -> {
            System.out.println("GuiRFToolsManual.buttonClicked: <");
            textPage.prevPage();
            window.setTextFocus(textPage);
        });
        pageLabel = new Label(mc, this).setText("0 / 0");
        nextButton = new Button(mc, this).setText(">").addButtonEvent(parent -> {
            System.out.println("GuiRFToolsManual.buttonClicked: >");
            textPage.nextPage();
            window.setTextFocus(textPage);
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
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        window.mouseClicked(x, y, button);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        window.handleMouseInput();
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        window.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
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
