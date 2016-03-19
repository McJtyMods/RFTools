package mcjty.rftools.blocks.generator;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.entity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiCoalGenerator extends GenericGuiContainer<CoalGeneratorTileEntity> {
    public static final int INFUSER_WIDTH = 180;
    public static final int INFUSER_HEIGHT = 152;

    private EnergyBar energyBar;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/coalgenerator.png");

    public GuiCoalGenerator(CoalGeneratorTileEntity tileEntity, CoalGeneratorContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, tileEntity, container, RFTools.GUI_MANUAL_MAIN, "coalgen");
        GenericEnergyStorageTileEntity.setCurrentRF(tileEntity.getEnergyStored(EnumFacing.DOWN));

        xSize = INFUSER_WIDTH;
        ySize = INFUSER_HEIGHT;
    }

    @Override
    public void initGui() {
        System.out.println("GuiCoalGenerator.initGui");
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(EnumFacing.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 8, 54)).setShowText(false);
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar); //.addChild(arrow);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        tileEntity.requestRfFromServer(RFTools.MODID);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();

        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());

        tileEntity.requestRfFromServer(RFTools.MODID);
    }
}
