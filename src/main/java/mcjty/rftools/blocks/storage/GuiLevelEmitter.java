package mcjty.rftools.blocks.storage;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftools.RFTools;
import mcjty.rftools.setup.GuiProxy;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.*;


public class GuiLevelEmitter extends GenericGuiContainer<LevelEmitterTileEntity> {
    public static final int STORAGE_WIDTH = 180;
    public static final int STORAGE_HEIGHT = 152;

    public static final String OREDICT_USE = "Use";
    public static final String OREDICT_IGNORE = "Ignore";
    public static final String STARRED = "Routable";
    public static final String NOTSTARRED = "All";


    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/levelemitter.png");

    public GuiLevelEmitter(LevelEmitterTileEntity levelEmitterTileEntity, LevelEmitterContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, levelEmitterTileEntity, container, GuiProxy.GUI_MANUAL_MAIN, "levelemit");

        xSize = STORAGE_WIDTH;
        ySize = STORAGE_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        mcjty.lib.gui.widgets.TextField amountField = new mcjty.lib.gui.widgets.TextField(mc, this).setTooltips("Set the amount of items in slot")
                .setName("amount")
                .setLayoutHint(60, 3, 80, 14);

        ChoiceLabel starredLabel = new ChoiceLabel(mc, this)
                .setName("starred")
                .addChoices(NOTSTARRED, STARRED)
                .setChoiceTooltip(NOTSTARRED, "All inventories are considered")
                .setChoiceTooltip(STARRED, "Only routable inventories are considered");
        starredLabel.setLayoutHint(60, 19, 80, 14);
        starredLabel.setChoice(tileEntity.isStarred() ? STARRED : NOTSTARRED);

        ChoiceLabel oreDictLabel = new ChoiceLabel(mc, this)
                .setName("oredict")
                .addChoices(OREDICT_IGNORE, OREDICT_USE)
                .setChoiceTooltip(OREDICT_IGNORE, "Ingore ore dictionary")
                .setChoiceTooltip(OREDICT_USE, "Use ore dictionary matching");
        oreDictLabel.setLayoutHint(60, 35, 80, 14);
        oreDictLabel.setChoice(tileEntity.isOreDict() ? OREDICT_USE : OREDICT_IGNORE);

        toplevel
                .addChild(new Label(mc, this).setText("Amount:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                        .setLayoutHint(10, 3, 50, 14))
                .addChild(amountField)
                .addChild(new Label(mc, this).setText("Routable:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                        .setLayoutHint(10, 19, 50, 14))
                .addChild(starredLabel)
                .addChild(new Label(mc, this).setText("Oredict:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                        .setLayoutHint(10, 35, 50, 14))
                .addChild(oreDictLabel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));
        window = new Window(this, toplevel);

        window.bind(RFToolsMessages.INSTANCE, "amount", tileEntity, LevelEmitterTileEntity.VALUE_AMOUNT.getName());
        window.bind(RFToolsMessages.INSTANCE, "starred", tileEntity, LevelEmitterTileEntity.VALUE_STARRED.getName());
        window.bind(RFToolsMessages.INSTANCE, "oredict", tileEntity, LevelEmitterTileEntity.VALUE_OREDICT.getName());
    }
}
