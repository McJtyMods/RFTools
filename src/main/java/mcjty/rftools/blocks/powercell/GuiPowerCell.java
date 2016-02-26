package mcjty.rftools.blocks.powercell;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.network.clientinfo.PacketGetInfoFromServer;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.Rectangle;

public class GuiPowerCell extends GenericGuiContainer<PowerCellTileEntity> {
    public static final int POWERCELL_WIDTH = 180;
    public static final int POWERCELL_HEIGHT = 152;

    private EnergyBar energyBar;
    private Button allNone;
    private Button allInput;
    private Button allOutput;

    private static long lastTime = 0;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/powercell.png");

    public GuiPowerCell(PowerCellTileEntity PowerCellTileEntity, PowerCellContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, PowerCellTileEntity, container, 0/*@todoRFTools.GUI_MANUAL_DIMENSION*/, "infuser");

        xSize = POWERCELL_WIDTH;
        ySize = POWERCELL_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(1000).setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 8, 54)).setShowText(false);
        energyBar.setValue(0);

        allNone = new Button(mc, this).setText("None").setTooltips("Set all sides to 'none'")
                .setLayoutHint(new PositionalLayout.PositionalHint(140, 10, 32, 15))
                .addButtonEvent(e -> sendServerCommand(RFToolsMessages.INSTANCE, PowerCellTileEntity.CMD_SETNONE));
        allInput = new Button(mc, this).setText("In").setTooltips("Set all sides to", "accept energy")
                .setLayoutHint(new PositionalLayout.PositionalHint(140, 27, 32, 15))
                .addButtonEvent(e -> sendServerCommand(RFToolsMessages.INSTANCE, PowerCellTileEntity.CMD_SETINPUT));
        allOutput = new Button(mc, this).setText("Out").setTooltips("Set all sides to", "send energy")
                .setLayoutHint(new PositionalLayout.PositionalHint(140, 44, 32, 15))
                .addButtonEvent(e -> sendServerCommand(RFToolsMessages.INSTANCE, PowerCellTileEntity.CMD_SETOUTPUT));

        Label label = new Label(mc, this);
        label.setText("Link:").setTooltips("Link a powercard to card", "on the left").setLayoutHint(new PositionalLayout.PositionalHint(26, 46, 40, 18));

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar)
                .addChild(allNone).addChild(allInput).addChild(allOutput).addChild(label);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        requestRF();
    }

    private void requestRF() {
        if (System.currentTimeMillis() - lastTime > 250) {
            lastTime = System.currentTimeMillis();
            RFToolsMessages.INSTANCE.sendToServer(new PacketGetInfoFromServer(RFTools.MODID, new PowerCellInfoPacketServer(tileEntity)));
        }
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();

        requestRF();
        energyBar.setMaxValue(PowerCellInfoPacketClient.tooltipBlocks * PowerCellConfiguration.rfPerCell);
        energyBar.setValue(PowerCellInfoPacketClient.tooltipEnergy);
    }
}
