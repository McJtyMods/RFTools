package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.api.MachineInformation;
import mcjty.lib.gui.events.ChoiceEvent;
import mcjty.lib.gui.events.ColorChoiceEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.blocks.screens.IModuleGuiChanged;
import mcjty.rftools.api.screens.IModuleRenderHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class MachineInformationClientScreenModule implements IClientScreenModule {

    private String line = "";
    private int labcolor = 0xffffff;
    private int txtcolor = 0xffffff;
    protected int dim = 0;
    protected BlockPos coordinate = BlockPosTools.INVALID;

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, Object[] screenData, float factor) {
        GlStateManager.disableLighting();
        int xoffset;
        if (!line.isEmpty()) {
            fontRenderer.drawString(line, 7, currenty, labcolor);
            xoffset = 7 + 40;
        } else {
            xoffset = 7;
        }

        if ((!BlockPosTools.INVALID.equals(coordinate)) && screenData != null && screenData.length >= 1 && screenData[0] instanceof String) {
            fontRenderer.drawString((String)screenData[0], xoffset, currenty, txtcolor);
        } else {
            fontRenderer.drawString("<invalid>", xoffset, currenty, 0xff0000);
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        // @todo Hacky, solve this better
        ScreenModuleGuiBuilder screenModuleGuiBuilder = (ScreenModuleGuiBuilder) guiBuilder;
        Minecraft mc = Minecraft.getMinecraft();
        Gui gui = screenModuleGuiBuilder.getGui();
        NBTTagCompound currentData = screenModuleGuiBuilder.getCurrentData();
        IModuleGuiChanged moduleGuiChanged = screenModuleGuiBuilder.getModuleGuiChanged();

        Panel panel = new Panel(mc, gui).setLayout(new VerticalLayout());
        TextField textField = new TextField(mc, gui).setDesiredHeight(16).setTooltips("Text to use as label").addTextEvent((parent, newText) -> {
            currentData.setString("text", newText);
            moduleGuiChanged.updateData();
        });
        panel.addChild(textField);
        addColorPanel(mc, gui, currentData, moduleGuiChanged, panel);
        addOptionPanel(mc, gui, currentData, moduleGuiChanged, panel);
        addMonitorPanel(mc, gui, currentData, panel);

        if (currentData != null) {
            textField.setText(currentData.getString("text"));
        }

        screenModuleGuiBuilder.overridePanel(panel);
    }

    private void addOptionPanel(Minecraft mc, Gui gui, final NBTTagCompound currentData, final IModuleGuiChanged moduleGuiChanged, Panel panel) {
        Panel optionPanel = new Panel(mc, gui).setLayout(new HorizontalLayout()).setDesiredHeight(16);

        final Map<String,Integer> choiceToIndex = new HashMap<String, Integer>();
        final ChoiceLabel tagButton = new ChoiceLabel(mc, gui).setDesiredHeight(16).setDesiredWidth(80);
        optionPanel.addChild(tagButton);

//        int dim = currentData.getInteger("dim");
        int x = currentData.getInteger("monitorx");
        int y = currentData.getInteger("monitory");
        int z = currentData.getInteger("monitorz");
        TileEntity tileEntity = mc.theWorld.getTileEntity(new BlockPos(x, y, z));

        if (tileEntity instanceof MachineInformation) {
            int current = currentData.getInteger("monitorTag");
            MachineInformation information = (MachineInformation) tileEntity;
            String currentTag = null;
            for (int i = 0 ; i < information.getTagCount() ; i++) {
                String tag = information.getTagName(i);
                choiceToIndex.put(tag, i);
                tagButton.addChoices(tag);
                tagButton.setChoiceTooltip(tag, information.getTagDescription(i));
                if (current == i) {
                    currentTag = tag;
                }
            }
            if (currentTag != null) {
                tagButton.setChoice(currentTag);
            }
        }

        tagButton.addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                String choice = tagButton.getCurrentChoice();
                Integer index = choiceToIndex.get(choice);
                if (index != null) {
                    currentData.setInteger("monitorTag", index);
                }
                moduleGuiChanged.updateData();
            }
        });


        panel.addChild(optionPanel);
    }

    private void addMonitorPanel(Minecraft mc, Gui gui, final NBTTagCompound currentData, Panel panel) {
        Panel monitorPanel = new Panel(mc, gui).setLayout(new HorizontalLayout()).
                setDesiredHeight(16);
        String monitoring;
        if (currentData.hasKey("monitorx")) {
            int dim = currentData.getInteger("dim");
            World world = mc.thePlayer.worldObj;
            if (dim == world.provider.getDimensionId()) {
                int x = currentData.getInteger("monitorx");
                int y = currentData.getInteger("monitory");
                int z = currentData.getInteger("monitorz");
                monitoring = currentData.getString("monitorname");
                Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                monitorPanel.addChild(new BlockRender(mc, gui).setRenderItem(block)).setDesiredWidth(20);
                monitorPanel.addChild(new Label(mc, gui).setText(x + "," + y + "," + z).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(150));
            } else {
                monitoring = "<unreachable>";
            }
        } else {
            monitoring = "<not set>";
        }
        panel.addChild(monitorPanel);
        panel.addChild(new Label(mc, gui).setText(monitoring));
    }

    private void addColorPanel(Minecraft mc, Gui gui, final NBTTagCompound currentData, final IModuleGuiChanged moduleGuiChanged, Panel panel) {
        ColorChoiceLabel labelColorSelector = addColorSelector(mc, gui, currentData, moduleGuiChanged, "color").setTooltips("Color for the label");
        ColorChoiceLabel txtColorSelector = addColorSelector(mc, gui, currentData, moduleGuiChanged, "txtcolor").setTooltips("Color for the text");
        Panel colorPanel = new Panel(mc, gui).setLayout(new HorizontalLayout()).
                addChild(new Label(mc, gui).setText("L:")).addChild(labelColorSelector).
                addChild(new Label(mc, gui).setText("Txt:")).addChild(txtColorSelector).
                setDesiredHeight(12);
        panel.addChild(colorPanel);
    }


    private ColorChoiceLabel addColorSelector(Minecraft mc, Gui gui, final NBTTagCompound currentData, final IModuleGuiChanged moduleGuiChanged, final String tagName) {
        ColorChoiceLabel colorChoiceLabel = new ColorChoiceLabel(mc, gui).addColors(0xffffff, 0xff0000, 0x00ff00, 0x0000ff, 0xffff00, 0xff00ff, 0x00ffff).setDesiredWidth(26).setDesiredHeight(14).addChoiceEvent(new ColorChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, Integer newColor) {
                currentData.setInteger(tagName, newColor);
                moduleGuiChanged.updateData();
            }
        });
        if (currentData != null) {
            int currentColor = currentData.getInteger(tagName);
            if (currentColor != 0) {
                colorChoiceLabel.setCurrentColor(currentColor);
            }
        }
        return colorChoiceLabel;
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            if (tagCompound.hasKey("color")) {
                labcolor = tagCompound.getInteger("color");
            } else {
                labcolor = 0xffffff;
            }
            if (tagCompound.hasKey("txtcolor")) {
                txtcolor = tagCompound.getInteger("txtcolor");
            } else {
                txtcolor = 0xffffff;
            }

            setupCoordinateFromNBT(tagCompound, dim, x, y, z);
        }
    }

    protected void setupCoordinateFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        coordinate = BlockPosTools.INVALID;
        if (tagCompound.hasKey("monitorx")) {
            this.dim = tagCompound.getInteger("dim");
            if (dim == this.dim) {
                BlockPos c = new BlockPos(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
                int dx = Math.abs(c.getX() - x);
                int dy = Math.abs(c.getY() - y);
                int dz = Math.abs(c.getZ() - z);
                if (dx <= 64 && dy <= 64 && dz <= 64) {
                    coordinate = c;
                }
            }
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
