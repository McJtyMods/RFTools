package mcjty.rftools.blocks.logic.wireless;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;

import java.awt.Rectangle;

public class GuiRedstoneReceiver extends GenericGuiContainer<RedstoneReceiverTileEntity> {
    public static final int REDSTONE_RECEIVER_WIDTH = 168;
    public static final int REDSTONE_RECEIVER_HEIGHT = 20;

    public GuiRedstoneReceiver(RedstoneReceiverTileEntity redstoneReceiverTileEntity, EmptyContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, redstoneReceiverTileEntity, container, RFTools.GUI_MANUAL_MAIN, "redrec");
        xSize = REDSTONE_RECEIVER_WIDTH;
        ySize = REDSTONE_RECEIVER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        ToggleButton analog = new ToggleButton(mc, this)
                .setName("analog")
                .setChannel("analog")
                .setText("Analog mode").setTooltips("Output the same power", "level as the input, instead", "of always 15 or 0").setCheckMarker(true).setDesiredWidth(160).setDesiredHeight(16);
        analog.setPressed(tileEntity.getAnalog());
        toplevel.addChild(analog);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, REDSTONE_RECEIVER_WIDTH, REDSTONE_RECEIVER_HEIGHT));
        window = new Window(this, toplevel);

        initializeFields();

        window.addChannelEvent("analog", (source, params) -> sendServerCommand(RFToolsMessages.INSTANCE, RedstoneReceiverTileEntity.CMD_SETANALOG, params));
    }

    private void initializeFields() {
        ToggleButton analog = window.findChild("analog");
        analog.setPressed(tileEntity.getAnalog());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow();
    }
}
