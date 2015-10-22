package mcjty.rftools.blocks.endergen;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.network.PacketRequestIntegerFromServer;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

public class GuiEndergenic extends GenericGuiContainer<EndergenicTileEntity> {
    public static final int ENDERGENIC_WIDTH = 190;
    public static final int ENDERGENIC_HEIGHT = 110;

    private EnergyBar energyBar;
    private TextField lastRfPerTick;
    private TextField lastLostPearls;
    private TextField lastLaunchedPearls;
    private TextField lastOpportunities;

    public static int fromServer_lastRfPerTick = 0;
    public static int fromServer_lastPearlsLost = 0;
    public static int fromServer_lastPearlsLaunched = 0;
    public static int fromServer_lastPearlOpportunities = 0;


    private int timer = 10;

    public GuiEndergenic(EndergenicTileEntity endergenicTileEntity, EmptyContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, endergenicTileEntity, container, RFTools.GUI_MANUAL_MAIN, "power");
        endergenicTileEntity.setCurrentRF(endergenicTileEntity.getEnergyStored(ForgeDirection.DOWN));
        xSize = ENDERGENIC_WIDTH;
        ySize = ENDERGENIC_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setFilledRectThickness(1).setHorizontal().setDesiredHeight(12).setMaxValue(maxEnergyStored).setShowText(true);
        energyBar.setValue(tileEntity.getCurrentRF());

        Label descriptionLabel = new Label(mc, this).setText("Averages over last 5 seconds:").setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);

        lastRfPerTick = new TextField(mc, this).setText("0 RF/tick").setDesiredWidth(90).setDesiredHeight(14);
        Panel p1 = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(new Label(mc, this).setText("Gain:").setDesiredWidth(70)).addChild(lastRfPerTick);

        lastLostPearls = new TextField(mc, this).setText("0").setDesiredWidth(90).setDesiredHeight(14);
        Panel p2 = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(new Label(mc, this).setText("Lost:").setDesiredWidth(70)).addChild(lastLostPearls);

        lastLaunchedPearls = new TextField(mc, this).setText("0").setDesiredWidth(90).setDesiredHeight(14);
        Panel p3 = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(new Label(mc, this).setText("Launched:").setDesiredWidth(70)).addChild(lastLaunchedPearls);

        lastOpportunities = new TextField(mc, this).setText("0").setDesiredWidth(90).setDesiredHeight(14);
        Panel p4 = new Panel(mc, this).setLayout(new HorizontalLayout()).addChild(new Label(mc, this).setText("Chances:").setDesiredWidth(70)).addChild(lastOpportunities);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout()).addChild(energyBar).
                addChild(descriptionLabel).
                addChild(p1).addChild(p2).addChild(p3).addChild(p4);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, ENDERGENIC_WIDTH, ENDERGENIC_HEIGHT));
        window = new mcjty.lib.gui.Window(this, toplevel);
        tileEntity.requestRfFromServer(RFToolsMessages.INSTANCE);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
        int currentRF = tileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFToolsMessages.INSTANCE);

        checkStats();

        lastRfPerTick.setText(fromServer_lastRfPerTick + " RF/tick");
        lastLostPearls.setText(fromServer_lastPearlsLost + " pearls");
        lastLaunchedPearls.setText(fromServer_lastPearlsLaunched + " pearls");
        lastOpportunities.setText(fromServer_lastPearlOpportunities + " times");

    }

    private void checkStats() {
        timer--;
        if (timer <= 0) {
            timer = 20;
            RFToolsMessages.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord,
                    EndergenicTileEntity.CMD_GETSTAT_RF, EndergenicTileEntity.CLIENTCMD_GETSTAT_RF));
            RFToolsMessages.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord,
                    EndergenicTileEntity.CMD_GETSTAT_LOST, EndergenicTileEntity.CLIENTCMD_GETSTAT_LOST));
            RFToolsMessages.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord,
                    EndergenicTileEntity.CMD_GETSTAT_LAUNCHED, EndergenicTileEntity.CLIENTCMD_GETSTAT_LAUNCHED));
            RFToolsMessages.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord,
                    EndergenicTileEntity.CMD_GETSTAT_OPPORTUNITIES, EndergenicTileEntity.CLIENTCMD_GETSTAT_OPPORTUNITIES));
        }
    }
}
