package mcjty.rftools.blocks.logic;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.TextEvent;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.network.Argument;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;

import java.awt.*;

public class GuiInvChecker extends GenericGuiContainer<InvCheckerTileEntity> {
    public static final int INVCHECKER_WIDTH = 200;
    public static final int INVCHECKER_HEIGHT = 30;

    private TextField amountField;
    private TextField slotField;

    public GuiInvChecker(InvCheckerTileEntity invCheckerTileEntity, EmptyContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, invCheckerTileEntity, container, RFTools.GUI_MANUAL_MAIN, "invchecker");
        xSize = INVCHECKER_WIDTH;
        ySize = INVCHECKER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        amountField = new TextField(mc, this).setTooltips("Set the amount of items in slot").addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                setAmount();
            }
        });
        int amount = tileEntity.getAmount();
        amountField.setText(String.valueOf(amount));

        slotField = new TextField(mc, this).setTooltips("Set the slot index").addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                setSlot();
            }
        });
        int current = tileEntity.getSlot();
        slotField.setText(String.valueOf(current));

        Panel bottomPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).
                addChild(new Label(mc, this).setText("Amount:")).addChild(amountField).
                addChild(new Label(mc, this).setText("Slot:")).addChild(slotField);
        toplevel.addChild(bottomPanel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, INVCHECKER_WIDTH, INVCHECKER_HEIGHT));
        window = new Window(this, toplevel);
    }

    private void setAmount() {
        String d = amountField.getText();
        int amount;
        try {
            amount = Integer.parseInt(d);
        } catch (NumberFormatException e) {
            amount = 1;
        }
        tileEntity.setAmount(amount);
        sendServerCommand(RFToolsMessages.INSTANCE, InvCheckerTileEntity.CMD_SETAMOUNT, new Argument("amount", amount));
    }

    private void setSlot() {
        String d = slotField.getText();
        int slot;
        try {
            slot = Integer.parseInt(d);
        } catch (NumberFormatException e) {
            slot = 0;
        }
        tileEntity.setSlot(slot);
        sendServerCommand(RFToolsMessages.INSTANCE, InvCheckerTileEntity.CMD_SETSLOT, new Argument("slot", slot));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}
