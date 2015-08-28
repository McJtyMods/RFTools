package mcjty.rftools.items.teleportprobe;

import mcjty.gui.Window;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.layout.VerticalLayout;
import mcjty.gui.widgets.Button;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.TextField;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.List;

public class GuiAdvancedPorter extends GuiScreen {

    private int xSize = 356;
    private int ySize = 80;

    private Window window;
    private TextField[] destinations = new TextField[AdvancedChargedPorterItem.MAXTARGETS];

    private static int target = -1;
    private static String name;
    private static int[] targets = new int[AdvancedChargedPorterItem.MAXTARGETS];
    private static String[] names = new String[AdvancedChargedPorterItem.MAXTARGETS];

    public GuiAdvancedPorter() {
    }

    public static void setInfo(int target, String name, int[] targets, String[] names) {
        GuiAdvancedPorter.target = target;
        GuiAdvancedPorter.name = name;
        GuiAdvancedPorter.targets = targets;
        GuiAdvancedPorter.names = names;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();

        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        for (int i = 0 ; i < AdvancedChargedPorterItem.MAXTARGETS ; i++) {
            destinations[i] = new TextField(mc, this);
            Panel dest = createPanel(destinations[i]);
            toplevel.addChild(dest);
        }

        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));

        window = new Window(this, toplevel);

        updateInfoFromServer();
    }

    private Panel createPanel(TextField destination) {
        return new Panel(mc, this).setLayout(new HorizontalLayout())
                    .addChild(destination)
                    .addChild(new Button(mc, this).setText("Set").setDesiredWidth(30))
                    .addChild(new Button(mc, this).setText("Clear").setDesiredWidth(30));
    }

    private void updateInfoFromServer() {
        RFToolsMessages.INSTANCE.sendToServer(new PacketGetTargets());
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


    private void setTarget(int i) {
        if (targets[i] == -1) {
            destinations[i].setText("No target set");
        } else {
            destinations[i].setText(targets[i] + " (" + names[i] + ")");
        }
    }

    @Override
    public void drawScreen(int xSize_lo, int ySize_lo, float par3) {
        super.drawScreen(xSize_lo, ySize_lo, par3);

        for (int i = 0 ; i < AdvancedChargedPorterItem.MAXTARGETS ; i++) {
            setTarget(i);
        }

        window.draw();

        List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x-guiLeft, y-guiTop, mc.fontRenderer);
        }
    }
}
