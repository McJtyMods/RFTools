package mcjty.rftools.blocks.powercell;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.network.clientinfo.PacketGetInfoFromServer;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiPowerCell extends GenericGuiContainer<PowerCellTileEntity> {
    public static final int POWERCELL_WIDTH = 180;
    public static final int POWERCELL_HEIGHT = 152;

    private EnergyBar energyBar;
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

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar); //.addChild(arrow);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        requestRF();
    }

    private void requestRF() {
        if (tileEntity.getNetworkId() == -1) {
            PowerCellInfoPacketClient.tooltipEnergy = tileEntity.getEnergy();
            PowerCellInfoPacketClient.tooltipBlocks = 1;
        } else {
            if (System.currentTimeMillis() - lastTime > 250) {
                lastTime = System.currentTimeMillis();
                RFToolsMessages.INSTANCE.sendToServer(new PacketGetInfoFromServer(RFTools.MODID, new PowerCellInfoPacketServer(tileEntity.getNetworkId())));
            }
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
