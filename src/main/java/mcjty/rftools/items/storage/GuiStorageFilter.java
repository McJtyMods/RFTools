package mcjty.rftools.items.storage;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.events.ChoiceEvent;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.widgets.ImageChoiceLabel;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.Widget;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.storage.ModularStorageTileEntity;
import mcjty.network.Argument;
import mcjty.network.PacketHandler;
import mcjty.rftools.network.PacketUpdateNBTItem;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.awt.*;


public class GuiStorageFilter extends GenericGuiContainer<ModularStorageTileEntity> {
    public static final int CONTROLLER_WIDTH = 180;
    public static final int CONTROLLER_HEIGHT = 152;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/storagefilter.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private ImageChoiceLabel blacklistMode;
    private ImageChoiceLabel oredictMode;
    private ImageChoiceLabel damageMode;
    private ImageChoiceLabel nbtMode;
    private ImageChoiceLabel modMode;

    public GuiStorageFilter(StorageFilterContainer container) {
        super(null, container, RFTools.GUI_MANUAL_MAIN, "storfilter");
        xSize = CONTROLLER_WIDTH;
        ySize = CONTROLLER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        blacklistMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(130, 9, 16, 16)).setTooltips("Black or whitelist mode").addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                updateSettings();
            }
        });
        blacklistMode.addChoice("Black", "Blacklist items", guiElements, 14 * 16, 32);
        blacklistMode.addChoice("White", "Whitelist items", guiElements, 15 * 16, 32);

        oredictMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(148, 9, 16, 16)).setTooltips("Filter based on ore dictionary").addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                updateSettings();
            }
        });
        oredictMode.addChoice("Off", "Oredict matching off", guiElements, 10 * 16, 32);
        oredictMode.addChoice("On", "Oredict matching on", guiElements, 11 * 16, 32);

        damageMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(130, 27, 16, 16)).setTooltips("Filter ignoring damage").addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                updateSettings();
            }
        });
        damageMode.addChoice("Off", "Ignore damage", guiElements, 6 * 16, 32);
        damageMode.addChoice("On", "Damage must match", guiElements, 7 * 16, 32);

        nbtMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(148, 27, 16, 16)).setTooltips("Filter ignoring NBT").addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                updateSettings();
            }
        });
        nbtMode.addChoice("Off", "Ignore MBT", guiElements, 8 * 16, 32);
        nbtMode.addChoice("On", "NBT must match", guiElements, 9 * 16, 32);

        modMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(130, 45, 16, 16)).setTooltips("Filter ignoring mod").addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                updateSettings();
            }
        });
        modMode.addChoice("Off", "Don't match on mod", guiElements, 12 * 16, 32);
        modMode.addChoice("On", "Only mod must match", guiElements, 13 * 16, 32);

        NBTTagCompound tagCompound = Minecraft.getMinecraft().thePlayer.getHeldItem().getTagCompound();
        if (tagCompound != null) {
            setBlacklistMode(tagCompound.getString("blacklistMode"));
            oredictMode.setCurrentChoice(tagCompound.getBoolean("oredictMode") ? 1 : 0);
            damageMode.setCurrentChoice(tagCompound.getBoolean("damageMode") ? 1 : 0);
            nbtMode.setCurrentChoice(tagCompound.getBoolean("nbtMode") ? 1 : 0);
            modMode.setCurrentChoice(tagCompound.getBoolean("modMode") ? 1 : 0);
        }

        Panel toplevel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(iconLocation).addChild(blacklistMode).addChild(oredictMode).addChild(damageMode).
                addChild(nbtMode).addChild(modMode);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void setBlacklistMode(String mode) {
        int idx = this.blacklistMode.findChoice(mode);
        if (idx == -1) {
            this.blacklistMode.setCurrentChoice("Black");
        } else {
            this.blacklistMode.setCurrentChoice(idx);
        }
    }

    private void updateSettings() {
        PacketHandler.INSTANCE.sendToServer(new PacketUpdateNBTItem(
                new Argument("blacklistMode", blacklistMode.getCurrentChoice()),
                new Argument("oredictMode", oredictMode.getCurrentChoiceIndex() == 1),
                new Argument("damageMode", damageMode.getCurrentChoiceIndex() == 1),
                new Argument("modMode", modMode.getCurrentChoiceIndex() == 1),
                new Argument("nbtMode", nbtMode.getCurrentChoiceIndex() == 1)));
   }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}
