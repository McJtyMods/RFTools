package mcjty.rftools.blocks.screens;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.CapabilityEnergy;

import java.awt.*;

public class GuiScreenController extends GenericGuiContainer<ScreenControllerTileEntity, GenericContainer> {
    public static final int CONTROLLER_WIDTH = 180;
    public static final int CONTROLLER_HEIGHT = 152;

    private EnergyBar energyBar;
    private Label infoLabel;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/screencontroller.png");

    public GuiScreenController(ScreenControllerTileEntity screenControllerTileEntity, GenericContainer container, PlayerInventory inventory) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, screenControllerTileEntity, container, inventory, GuiProxy.GUI_MANUAL_MAIN, "screens");

        xSize = CONTROLLER_WIDTH;
        ySize = CONTROLLER_HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        energyBar = new EnergyBar(minecraft, this).setVertical().setLayoutHint(10, 7, 8, 54).setShowText(false);

        Button scanButton = new Button(minecraft, this)
                .setName("scan")
                .setText("Scan").setTooltips("Find all nearby screens", "and connect to them").setLayoutHint(30, 7, 50, 14);
        Button detachButton = new Button(minecraft, this)
                .setName("detach")
                .setText("Detach").setTooltips("Detach from all screens").setLayoutHint(90, 7, 50, 14);
        infoLabel = new Label(minecraft, this);
        infoLabel.setLayoutHint(30, 25, 140, 14);

        Panel toplevel = new Panel(minecraft, this).setBackground(iconLocation).setLayout(new PositionalLayout())
                .addChildren(energyBar, scanButton, detachButton, infoLabel);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        window.action(RFToolsMessages.INSTANCE, "scan", tileEntity, ScreenControllerTileEntity.ACTION_SCAN);
        window.action(RFToolsMessages.INSTANCE, "detach", tileEntity, ScreenControllerTileEntity.ACTION_DETACH);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();

        tileEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(e -> {
            energyBar.setMaxValue(((GenericEnergyStorage)e).getCapacity());
            energyBar.setValue(((GenericEnergyStorage)e).getEnergy());
        });
    }
}
