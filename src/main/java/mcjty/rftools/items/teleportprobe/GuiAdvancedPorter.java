package mcjty.rftools.items.teleportprobe;

import mcjty.gui.Window;
import mcjty.gui.events.ButtonEvent;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.layout.VerticalLayout;
import mcjty.gui.widgets.Button;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.TextField;
import mcjty.gui.widgets.Widget;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.List;

public class GuiAdvancedPorter extends GuiScreen {

    private int xSize = 356;
    private int ySize = 72;

    private Window window;
    private Panel[] panels = new Panel[AdvancedChargedPorterItem.MAXTARGETS];
    private TextField[] destinations = new TextField[AdvancedChargedPorterItem.MAXTARGETS];

    private static int target = -1;
    private static int[] targets = new int[AdvancedChargedPorterItem.MAXTARGETS];
    private static String[] names = new String[AdvancedChargedPorterItem.MAXTARGETS];

    public GuiAdvancedPorter() {
    }

    public static void setInfo(int target, int[] targets, String[] names) {
        GuiAdvancedPorter.target = target;
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

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout().setSpacing(0));

        for (int i = 0 ; i < AdvancedChargedPorterItem.MAXTARGETS ; i++) {
            destinations[i] = new TextField(mc, this);
            panels[i] = createPanel(destinations[i], i);
            toplevel.addChild(panels[i]);
        }

        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));

        window = new Window(this, toplevel);

        updateInfoFromServer();
    }

    private Panel createPanel(final TextField destination, final int i) {
        return new Panel(mc, this).setLayout(new HorizontalLayout())
                    .addChild(destination)
                    .addChild(new Button(mc, this).setText("Set").setDesiredWidth(30).setDesiredHeight(16).addButtonEvent(new ButtonEvent() {
                        @Override
                        public void buttonClicked(Widget parent) {
                            if (targets[i] != -1) {
                                RFToolsMessages.INSTANCE.sendToServer(new PacketSetTarget(targets[i]));
                                target = targets[i];
                            }
                        }
                    }))
                    .addChild(new Button(mc, this).setText("Clear").setDesiredWidth(40).setDesiredHeight(16).addButtonEvent(new ButtonEvent() {
                        @Override
                        public void buttonClicked(Widget parent) {
                            if (targets[i] != -1 && targets[i] == target) {
                                target = -1;
                            }
                            RFToolsMessages.INSTANCE.sendToServer(new PacketClearTarget(i));
                            targets[i] = -1;
                        }
                    })).setDesiredHeight(16);
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
        panels[i].setFilledBackground(-1);
        if (targets[i] == -1) {
            destinations[i].setText("No target set");
        } else {
            destinations[i].setText(targets[i] + ": " + names[i]);
            if (targets[i] == target) {
                panels[i].setFilledBackground(0xffeedd33);
            }
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
