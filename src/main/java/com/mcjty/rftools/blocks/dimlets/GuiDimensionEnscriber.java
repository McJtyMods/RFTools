package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.events.TextEvent;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.gui.widgets.TextField;
import com.mcjty.gui.widgets.Widget;
import com.mcjty.gui.widgets.Label;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.world.types.ControllerType;
import com.mcjty.rftools.dimension.world.types.FeatureType;
import com.mcjty.rftools.dimension.world.types.TerrainType;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletObjectMapping;
import com.mcjty.rftools.items.dimlets.DimletType;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Counter;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GuiDimensionEnscriber extends GenericGuiContainer<DimensionEnscriberTileEntity> {
    public static final int ENSCRIBER_WIDTH = 256;
    public static final int ENSCRIBER_HEIGHT = 224;

    private Button extractButton;
    private Button storeButton;
    private TextField nameField;
    private Label validateField;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/dimensionenscriber.png");

    public GuiDimensionEnscriber(DimensionEnscriberTileEntity dimensionEnscriberTileEntity, DimensionEnscriberContainer container) {
        super(dimensionEnscriberTileEntity, container);

        xSize = ENSCRIBER_WIDTH;
        ySize = ENSCRIBER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        extractButton = new Button(mc, this).setText("Extract").setLayoutHint(new PositionalLayout.PositionalHint(13, 164, 60, 16)).addButtonEvent(
                new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        extractDimlets();
                    }
                }
        ).setTooltips("Extract the dimlets out of", "a realized dimension tab");
        storeButton = new Button(mc, this).setText("Store").setLayoutHint(new PositionalLayout.PositionalHint(13, 182, 60, 16)).addButtonEvent(
                new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        storeDimlets();
                    }
                }
        ).setTooltips("Store dimlets in a", "empty dimension tab");
        nameField = new TextField(mc, this).addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                storeName(newText);
            }
        }).setLayoutHint(new PositionalLayout.PositionalHint(13, 200, 60, 16));
        validateField = new Label(mc, this).setText("Val");
        validateField.setTooltips("Hover here for errors...");
        validateField.setLayoutHint(new PositionalLayout.PositionalHint(35, 142, 38, 16));

        setNameFromDimensionTab();

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(extractButton).addChild(storeButton).
                addChild(nameField).addChild(validateField);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void storeName(String name) {
        sendServerCommand(DimensionEnscriberTileEntity.CMD_SETNAME, new Argument("name", name));
    }

    private void extractDimlets() {
        for (int i = 0 ; i < DimensionEnscriberContainer.SIZE_DIMLETS ; i++) {
            ItemStack stack = inventorySlots.getSlot(i + DimensionEnscriberContainer.SLOT_DIMLETS).getStack();
            if (stack != null && stack.stackSize > 0) {
                // Cannot extract. There are still items in the way.
                RFTools.warn(mc.thePlayer, "You cannot extract. Remove all dimlets first!");
                return;
            }
        }
        sendServerCommand(DimensionEnscriberTileEntity.CMD_EXTRACT);
    }

    private void storeDimlets() {
        sendServerCommand(DimensionEnscriberTileEntity.CMD_STORE, new Argument("name", nameField.getText()));
    }

    private void enableButtons() {
        Slot slot = inventorySlots.getSlot(DimensionEnscriberContainer.SLOT_TAB);
        extractButton.setEnabled(false);
        storeButton.setEnabled(false);
        if (slot.getStack() != null) {
            if (slot.getStack().getItem() == ModItems.emptyDimensionTab) {
                storeButton.setEnabled(true);
            } else if (slot.getStack().getItem() == ModItems.realizedDimensionTab) {
                extractButton.setEnabled(true);
            }
        }
    }

    private List<DimletKey> extractModifiersForType(List<DimletKey> modifiers, DimletType type) {
        List<DimletKey> modifiersForType = new ArrayList<DimletKey>();
        int i = 0;
        while (i < modifiers.size()) {
            DimletKey modifier = modifiers.get(i);
            if (type.dimletType.isModifiedBy(modifier.getType())) {
                modifiersForType.add(modifier);
                modifiers.remove(i);
            } else {
                i++;
            }
        }
        return modifiersForType;
    }

    private String shortenName(String name) {
        int idx = name.indexOf('_');
        if (idx == -1) {
            return name;
        } else {
            return name.substring(idx+1);
        }
    }

    private void validateDimlets() {
        List<String> tooltips = new ArrayList<String>();

        TerrainType terrainType = null;
        int cntTerrain = 0;
        int cntBiomes = 0;
        int cntController = 0;
        for (int i = DimensionEnscriberContainer.SLOT_DIMLETS ; i < DimensionEnscriberContainer.SLOT_TAB ; i++) {
            Slot slot = inventorySlots.getSlot(i);
            if (slot != null && slot.getStack() != null && slot.getStack().stackSize > 0) {
                ItemStack stack = slot.getStack();
                DimletKey key = KnownDimletConfiguration.getDimletKey(stack, Minecraft.getMinecraft().theWorld);
                if (key.getType() == DimletType.DIMLET_TERRAIN) {
                    cntTerrain++;
                    terrainType = DimletObjectMapping.idToTerrainType.get(key);
                } else if (key.getType() == DimletType.DIMLET_BIOME) {
                    cntBiomes++;
                } else if (key.getType() == DimletType.DIMLET_CONTROLLER) {
                    cntController++;
                }
            }
        }
        if (cntTerrain > 1) {
            tooltips.add("Using more then one TERRAIN is not useful!");
            terrainType = null;
        }
        if (cntController > 1) {
            tooltips.add("Using more then one CONTROLLER is not useful!");
        }

        List<DimletKey> modifiers = new ArrayList<DimletKey>();
        for (int i = DimensionEnscriberContainer.SLOT_DIMLETS ; i < DimensionEnscriberContainer.SLOT_TAB ; i++) {
            Slot slot = inventorySlots.getSlot(i);
            if (slot != null && slot.getStack() != null && slot.getStack().stackSize > 0) {
                ItemStack stack = slot.getStack();
                DimletKey key = KnownDimletConfiguration.getDimletKey(stack, Minecraft.getMinecraft().theWorld);
                DimletType type = key.getType();
                if (type.dimletType.isModifier()) {
                    modifiers.add(key);
                } else {
                    List<DimletKey> modifiersForType = extractModifiersForType(modifiers, type);
                    if (type == DimletType.DIMLET_TERRAIN) {
                        if (DimletObjectMapping.idToTerrainType.get(key) == TerrainType.TERRAIN_VOID && !modifiersForType.isEmpty()) {
                            tooltips.add("VOID terrain cannot use modifiers");
                        }
                    } else if (type == DimletType.DIMLET_FEATURE) {
                        FeatureType featureType = DimletObjectMapping.idToFeatureType.get(key);
                        Counter<DimletType> modifierAmountUsed = new Counter<DimletType>();
                        for (DimletKey modifier : modifiersForType) {
                            modifierAmountUsed.increment(modifier.getType());
                        }
                        for (Map.Entry<DimletType, Integer> entry : modifierAmountUsed.entrySet()) {
                            Integer amountSupported = featureType.getSupportedModifierAmount(entry.getKey());
                            if (amountSupported == null) {
                                tooltips.add(shortenName(featureType.name()) + " does not use " + shortenName(entry.getKey().name()) + " modifiers!");
                            } else if (amountSupported == 1 && entry.getValue() > 1) {
                                tooltips.add(shortenName(featureType.name()) + " only needs one " + shortenName(entry.getKey().name()) + " modifier!");
                            }
                        }

                        if (terrainType == null && !featureType.supportsAllTerrains()) {
                            tooltips.add(shortenName(featureType.name()) + " does not work on all terrains and no terrain was specified!");
                        }
                        if (terrainType != null && !featureType.isTerrainSupported(terrainType)) {
                            tooltips.add(shortenName(featureType.name()) + " does not work for terrain " + shortenName(terrainType.name()) + "!");
                        }
                    } else if (type == DimletType.DIMLET_CONTROLLER) {
                        ControllerType controllerType = DimletObjectMapping.idToControllerType.get(key);
                        int neededBiomes = controllerType.getNeededBiomes();
                        if (neededBiomes != -1) {
                            if (cntBiomes > neededBiomes) {
                                tooltips.add("Too many biomes specified for " + shortenName(controllerType.name()) + "!");
                            } else if (cntBiomes < neededBiomes) {
                                tooltips.add("Too few biomes specified for " + shortenName(controllerType.name()) + "!");
                            }
                        }
                    }
                }
            }
        }

        if (!modifiers.isEmpty()) {
            tooltips.add("There are dangling modifiers in this descriptor");
        }

        boolean error = true;
        if (tooltips.isEmpty()) {
            tooltips.add("Everything appears to be allright");
            error = false;
        }
        validateField.setTooltips(tooltips.toArray(new String[tooltips.size()]));
        validateField.setColor(error ? 0xFF0000 : 0x008800);
        validateField.setText(error ? "Warn" : "Ok");
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        enableButtons();
        validateDimlets();

        if (tileEntity.hasTabSlotChangedAndClear()) {
            setNameFromDimensionTab();
        }

        window.draw();
    }

    private void setNameFromDimensionTab() {
        Slot slot = inventorySlots.getSlot(DimensionEnscriberContainer.SLOT_TAB);
        if (slot.getStack() != null && slot.getStack().getItem() == ModItems.realizedDimensionTab) {
            NBTTagCompound tagCompound = slot.getStack().getTagCompound();
            if (tagCompound != null) {
                String name = tagCompound.getString("name");
                if (name != null) {
                    nameField.setText(name);
                }
            }
        }
    }
}
