package mcjty.rftools.blocks.logic.threelogic;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.rftools.RFTools;
import mcjty.rftools.gui.GuiProxy;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.lib.typed.TypedMap;
import net.minecraft.util.ResourceLocation;

import static mcjty.rftools.blocks.logic.threelogic.ThreeLogicTileEntity.PARAM_INDEX;
import static mcjty.rftools.blocks.logic.threelogic.ThreeLogicTileEntity.PARAM_STATE;

public class GuiThreeLogic extends GenericGuiContainer<ThreeLogicTileEntity> {

    public GuiThreeLogic(ThreeLogicTileEntity threeLogicTileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, threeLogicTileEntity, container, GuiProxy.GUI_MANUAL_MAIN, "threelogic");
    }

    @Override
    public void initGui() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/threelogic.gui"));
        super.initGui();

        initializeFields();
        setupEvents();
    }

    private void setupEvents() {
        window.event("choice", (source, params) -> {
            String name = source.getName();
            int i = Integer.parseInt(name.substring(name.length()-1));
            String current = params.get(ChoiceLabel.PARAM_CHOICE);
            int st = "On".equals(current) ? 1 : "Off".equals(current) ? 0 : -1;
            sendServerCommand(RFToolsMessages.INSTANCE, ThreeLogicTileEntity.CMD_SETSTATE,
                    TypedMap.builder()
                        .put(PARAM_INDEX, i)
                        .put(PARAM_STATE, st)
                        .build());
        });
    }

    private void initializeFields() {
        for (int i = 0 ; i < 8 ; i++) {
            ChoiceLabel tl = window.findChild("choice" + i);
            int state = tileEntity.getState(i);
            switch (state) {
                case 0: tl.setChoice("Off"); break;
                case 1: tl.setChoice("On"); break;
                default: tl.setChoice("Keep"); break;
            }
        }
    }
}
