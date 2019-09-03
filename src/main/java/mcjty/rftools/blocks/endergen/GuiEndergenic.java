package mcjty.rftools.blocks.endergen;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.CapabilityEnergy;

public class GuiEndergenic extends GenericGuiContainer<EndergenicTileEntity, GenericContainer> {

    private EnergyBar energyBar;
    private TextField lastRfPerTick;
    private TextField lastLostPearls;
    private TextField lastLaunchedPearls;
    private TextField lastOpportunities;

    public static long fromServer_lastRfPerTick = 0;
    public static int fromServer_lastPearlsLost = 0;
    public static int fromServer_lastPearlsLaunched = 0;
    public static int fromServer_lastPearlOpportunities = 0;


    private int timer = 10;

    public GuiEndergenic(EndergenicTileEntity endergenicTileEntity, GenericContainer container, PlayerInventory inventory) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, endergenicTileEntity, container, inventory, GuiProxy.GUI_MANUAL_MAIN, "power");
    }

    @Override
    public void init() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/endergenic.gui"));
        super.init();

        initializeFields();
    }

    private void initializeFields() {
        energyBar = window.findChild("energybar");
        lastRfPerTick = window.findChild("lastrft");
        lastLostPearls = window.findChild("lastlost");
        lastLaunchedPearls = window.findChild("lastlaunched");
        lastOpportunities = window.findChild("lastopp");
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();

        tileEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(e -> {
            energyBar.setMaxValue(((GenericEnergyStorage)e).getCapacity());
            energyBar.setValue(((GenericEnergyStorage)e).getEnergy());
        });

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
