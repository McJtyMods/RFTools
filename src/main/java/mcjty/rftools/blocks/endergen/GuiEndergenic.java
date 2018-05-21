package mcjty.rftools.blocks.endergen;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.network.PacketRequestDataFromServer;
import mcjty.lib.tileentity.GenericEnergyStorageTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import static mcjty.lib.tileentity.GenericEnergyStorageTileEntity.getCurrentRF;

public class GuiEndergenic extends GenericGuiContainer<EndergenicTileEntity> {

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

    public GuiEndergenic(EndergenicTileEntity endergenicTileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, endergenicTileEntity, container, RFTools.GUI_MANUAL_MAIN, "power");
        GenericEnergyStorageTileEntity.setCurrentRF(endergenicTileEntity.getEnergyStored());
    }

    @Override
    public void initGui() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/endergenic.gui"));
        super.initGui();

        initializeFields();

        tileEntity.requestRfFromServer(RFTools.MODID);
    }

    private void initializeFields() {
        energyBar = window.findChild("energybar");
        lastRfPerTick = window.findChild("lastrft");
        lastLostPearls = window.findChild("lastlost");
        lastLaunchedPearls = window.findChild("lastlaunched");
        lastOpportunities = window.findChild("lastopp");

        energyBar.setMaxValue(tileEntity.getMaxEnergyStored());
        energyBar.setValue(getCurrentRF());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
        long currentRF = GenericEnergyStorageTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFTools.MODID);

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
            tileEntity.requestDataFromServer(RFTools.MODID, EndergenicTileEntity.CMD_GETSTATS, TypedMap.EMPTY);
        }
    }
}
