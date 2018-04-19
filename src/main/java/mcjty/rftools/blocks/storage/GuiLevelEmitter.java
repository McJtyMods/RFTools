package mcjty.rftools.blocks.storage;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.network.Argument;
import mcjty.rftools.RFTools;
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

    private mcjty.lib.gui.widgets.TextField amountField;
    private ChoiceLabel oreDictLabel;
    private ChoiceLabel starredLabel;


    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/levelemitter.png");

    public GuiLevelEmitter(LevelEmitterTileEntity levelEmitterTileEntity, LevelEmitterContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, levelEmitterTileEntity, container, RFTools.GUI_MANUAL_MAIN, "levelemit");

        xSize = STORAGE_WIDTH;
        ySize = STORAGE_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        amountField = new mcjty.lib.gui.widgets.TextField(mc, this).setTooltips("Set the amount of items in slot")
                .setLayoutHint(60, 3, 80, 14)
                .addTextEvent((parent, newText) -> setAmount());
        int amount = tileEntity.getAmount();
        amountField.setText(String.valueOf(amount));

        starredLabel = new ChoiceLabel(mc, this)
                .addChoices(NOTSTARRED, STARRED)
                .addChoiceEvent((parent, newChoice) -> setMetaUsage())
                .setChoiceTooltip(NOTSTARRED, "All inventories are considered")
                .setChoiceTooltip(STARRED, "Only routable inventories are considered");
        starredLabel.setLayoutHint(60, 19, 80, 14);
        starredLabel.setChoice(tileEntity.isStarred() ? STARRED : NOTSTARRED);

        oreDictLabel = new ChoiceLabel(mc, this)
                .addChoices(OREDICT_IGNORE, OREDICT_USE)
                .addChoiceEvent((parent, newChoice) -> setOredictUsage())
                .setChoiceTooltip(OREDICT_IGNORE, "Ingore ore dictionary")
                .setChoiceTooltip(OREDICT_USE, "Use ore dictionary matching");
        oreDictLabel.setLayoutHint(60, 35, 80, 14);
        oreDictLabel.setChoice(tileEntity.isOreDict() ? OREDICT_USE : OREDICT_IGNORE);

        toplevel
                .addChild(new Label(mc, this).setText("Amount:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT)
                        .setLayoutHint(10, 3, 50, 14))
                .addChild(amountField)
                .addChild(new Label(mc, this).setText("Routable:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT)
                        .setLayoutHint(10, 19, 50, 14))
                .addChild(starredLabel)
                .addChild(new Label(mc, this).setText("Oredict:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT)
                        .setLayoutHint(10, 35, 50, 14))
                .addChild(oreDictLabel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));
        window = new Window(this, toplevel);
    }

    private void setMetaUsage() {
        boolean b = STARRED.equals(starredLabel.getCurrentChoice());
        tileEntity.setStarred(b);
        sendServerCommand(RFToolsMessages.INSTANCE, LevelEmitterTileEntity.CMD_SETSTARRED, new Argument("b", b));
    }

    private void setOredictUsage() {
        boolean b = OREDICT_USE.equals(oreDictLabel.getCurrentChoice());
        tileEntity.setOreDict(b);
        sendServerCommand(RFToolsMessages.INSTANCE, LevelEmitterTileEntity.CMD_SETOREDICT, new Argument("b", b));
    }

    private void setAmount() {
        String d = amountField.getText();
        int amount;
        try {
            amount = Integer.parseInt(d);
        } catch (NumberFormatException e) {
            amount = 1;
        }
        tileEntity.setAmount(amount);
        sendServerCommand(RFToolsMessages.INSTANCE, LevelEmitterTileEntity.CMD_SETAMOUNT, new Argument("amount", amount));
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}
