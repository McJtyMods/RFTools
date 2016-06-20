package mcjty.rftools.blocks.logic.threelogic;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.network.Argument;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiThreeLogic extends GenericGuiContainer<ThreeLogicTileEntity> {
    public static final int LOGIC3_WIDTH = 188;
    public static final int LOGIC3_HEIGHT = 154;

    private List<ChoiceLabel> outputs = new ArrayList<>();

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/threelogic.png");

    public GuiThreeLogic(ThreeLogicTileEntity threeLogicTileEntity, EmptyContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, threeLogicTileEntity, container, RFTools.GUI_MANUAL_MAIN, "threelogic");
        xSize = LOGIC3_WIDTH;
        ySize = LOGIC3_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        for (int i = 0 ; i < 8 ; i++) {
            final int finalI = i;
            ChoiceLabel tl = new ChoiceLabel(mc, this).addChoices("On", "Off", "Keep")
                    .setDesiredWidth(38).setDesiredHeight(14)
                    .setLayoutHint(new PositionalLayout.PositionalHint(146, 25 + i * 15));
            tl.setChoiceTooltip("On", "Emit redstone signal");
            tl.setChoiceTooltip("Off", "Don't emit redstone signal");
            tl.setChoiceTooltip("Keep", "Keep previous redstone signal");
            int state = tileEntity.getState(i);
            switch (state) {
                case 0: tl.setChoice("Off"); break;
                case 1: tl.setChoice("On"); break;
                default: tl.setChoice("Keep"); break;
            }
            tl.addChoiceEvent((widget, s) -> {
                String current = tl.getCurrentChoice();
                int st = "On".equals(current) ? 1 : "Off".equals(current) ? 0 : -1;
                sendServerCommand(RFToolsMessages.INSTANCE, ThreeLogicTileEntity.CMD_SETSTATE, new Argument("index", finalI), new Argument("state", st));
            });
            outputs.add(tl);
            toplevel.addChild(tl);
        }

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, LOGIC3_WIDTH, LOGIC3_HEIGHT));
        window = new Window(this, toplevel);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}
