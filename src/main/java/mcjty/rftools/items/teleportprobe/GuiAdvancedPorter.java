package mcjty.rftools.items.teleportprobe;

import mcjty.gui.GuiItemScreen;
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

import java.awt.*;

public class GuiAdvancedPorter extends GuiItemScreen {

    private final static int xSize = 340;
    private final static int ySize = 70;

    private Panel[] panels = new Panel[AdvancedChargedPorterItem.MAXTARGETS];
    private TextField[] destinations = new TextField[AdvancedChargedPorterItem.MAXTARGETS];

    private static int target = -1;
    private static int[] targets = new int[AdvancedChargedPorterItem.MAXTARGETS];
    private static String[] names = new String[AdvancedChargedPorterItem.MAXTARGETS];

    public GuiAdvancedPorter() {
        super(RFTools.instance, RFToolsMessages.INSTANCE, xSize, ySize, RFTools.GUI_MANUAL_MAIN, "porter");
    }

    public static void setInfo(int target, int[] targets, String[] names) {
        GuiAdvancedPorter.target = target;
        GuiAdvancedPorter.targets = targets;
        GuiAdvancedPorter.names = names;
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

        drawWindow();
    }
}
