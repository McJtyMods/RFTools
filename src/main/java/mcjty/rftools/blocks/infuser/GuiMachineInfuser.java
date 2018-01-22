package mcjty.rftools.blocks.infuser;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.entity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiMachineInfuser extends GenericGuiContainer<MachineInfuserTileEntity> {
    public static final int INFUSER_WIDTH = 180;
    public static final int INFUSER_HEIGHT = 152;

    private EnergyBar energyBar;
//    private ImageLabel arrow;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/machineinfuser.png");

    public GuiMachineInfuser(MachineInfuserTileEntity machineInfuserTileEntity, MachineInfuserContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, machineInfuserTileEntity, container, 0/*@todoRFTools.GUI_MANUAL_DIMENSION*/, "infuser");
        GenericEnergyStorageTileEntity.setCurrentRF(machineInfuserTileEntity.getEnergyStored());

        xSize = INFUSER_WIDTH;
        ySize = INFUSER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored();
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 8, 54)).setShowText(false);
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());

//        arrow = new ImageLabel(mc, this).setImage(iconGuiElements, 192, 0);
//        arrow.setLayoutHint(new PositionalLayout.PositionalHint(90, 26, 16, 16));

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar); //.addChild(arrow);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        tileEntity.requestRfFromServer(RFTools.MODID);
//        tileEntity.requestResearchingFromServer();
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
//        int researching = tileEntity.getResearching();
//        if (researching > 0) {
//            arrow.setImage(iconGuiElements, 144, 0);
//        } else {
//            arrow.setImage(iconGuiElements, 192, 0);
//        }

        drawWindow();

        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());

        tileEntity.requestRfFromServer(RFTools.MODID);
//        tileEntity.requestResearchingFromServer();
    }
}
