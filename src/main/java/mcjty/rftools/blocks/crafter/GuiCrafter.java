package mcjty.rftools.blocks.crafter;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.ItemStackList;
import mcjty.rftools.RFTools;
import mcjty.rftools.craftinggrid.CraftingRecipe;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;

public class GuiCrafter extends GenericGuiContainer<CrafterBaseTE, GenericContainer> {
    private EnergyBar energyBar;
    private WidgetList recipeList;
    private ChoiceLabel keepItem;
    private ChoiceLabel internalRecipe;
    private Button applyButton;

    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private static int lastSelected = -1;

    public GuiCrafter(CrafterBaseTE te, GenericContainer container, PlayerInventory inventory) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, te, container, inventory, GuiProxy.GUI_MANUAL_MAIN, "crafter");
    }

    @Override
    public void init() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/crafter.gui"));
        super.init();

        initializeFields();

        Integer sizeInventory = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(h -> h.getSlots()).orElse(0);
        if (lastSelected != -1 && lastSelected < sizeInventory) {
            recipeList.setSelected(lastSelected);
        }
//        sendChangeToServer(-1, null, null, false, CraftingRecipe.CraftMode.EXT);

        window.event("apply", (source, params) -> applyRecipe());
        window.event("select", (source, params) -> selectRecipe());
    }

    private void initializeFields() {
        recipeList = window.findChild("recipes");
        energyBar = window.findChild("energybar");
        applyButton = window.findChild("apply");
        keepItem = window.findChild("keep");
        internalRecipe = window.findChild("internal");

        ((ImageChoiceLabel) window.findChild("redstone")).setCurrentChoice(tileEntity.getRSMode().ordinal());
        ((ImageChoiceLabel) window.findChild("speed")).setCurrentChoice(tileEntity.getSpeedMode());

        populateList();
    }


    private void populateList() {
        recipeList.removeChildren();
        for (int i = 0 ; i < tileEntity.getSupportedRecipes() ; i++) {
            CraftingRecipe recipe = tileEntity.getRecipe(i);
            addRecipeLine(recipe.getResult());
        }
    }

    private void addRecipeLine(ItemStack craftingResult) {
        String readableName = BlockTools.getReadableName(craftingResult);
        int color = StyleConfig.colorTextInListNormal;
        if (craftingResult.isEmpty()) {
            readableName = "<no recipe>";
            color = 0xFF505050;
        }
        Panel panel = new Panel(minecraft, this).setLayout(new HorizontalLayout())
                .addChild(new BlockRender(minecraft, this)
                        .setRenderItem(craftingResult)
                        .setTooltips("Double click to edit this recipe"))
                .addChild(new Label(minecraft, this)
                        .setColor(color)
                        .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                        .setDynamic(true)
                        .setText(readableName)
                        .setTooltips("Double click to edit this recipe"));
        recipeList.addChild(panel);
    }

    private void selectRecipe() {
        int selected = recipeList.getSelected();
        lastSelected = selected;
        if (selected == -1) {
            for (int i = 0 ; i < 10 ; i++) {
                container.getSlot(i).putStack(ItemStack.EMPTY);
            }
            keepItem.setChoice("All");
            internalRecipe.setChoice("Ext");
            return;
        }
        CraftingRecipe craftingRecipe = tileEntity.getRecipe(selected);
        CraftingInventory inv = craftingRecipe.getInventory();
        for (int i = 0 ; i < 9 ; i++) {
            container.getSlot(i).putStack(inv.getStackInSlot(i));
        }
        container.getSlot(9).putStack(craftingRecipe.getResult());
        keepItem.setChoice(craftingRecipe.isKeepOne() ? "Keep" : "All");
        internalRecipe.setChoice(craftingRecipe.getCraftMode().getDescription());
    }

    private void testRecipe() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }

        CraftingInventory inv = new CraftingInventory(new Container(null, -1) {
            @Override
            public boolean canInteractWith(PlayerEntity var1) {
                return false;
            }
        }, 3, 3);

        for (int i = 0 ; i < 9 ; i++) {
            inv.setInventorySlotContents(i, container.getSlot(i).getStack());
        }

        // Compare current contents to avoid unneeded slot update.
        IRecipe recipe = CraftingRecipe.findRecipe(minecraft.world, inv);
        ItemStack newResult;
        if (recipe == null) {
            newResult = ItemStack.EMPTY;
        } else {
            newResult = recipe.getCraftingResult(inv);
        }
        container.getSlot(9).putStack(newResult);
    }

    private void applyRecipe() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }

        if (selected >= tileEntity.getSupportedRecipes()) {
            recipeList.setSelected(-1);
            return;
        }

        CraftingRecipe craftingRecipe = tileEntity.getRecipe(selected);
        CraftingInventory inv = craftingRecipe.getInventory();

        for (int i = 0 ; i < 9 ; i++) {
            ItemStack oldStack = inv.getStackInSlot(i);
            ItemStack newStack = container.getSlot(i).getStack();
            if (!itemStacksEqual(oldStack, newStack)) {
                inv.setInventorySlotContents(i, newStack);
            }
        }

        // Compare current contents to avoid unneeded slot update.
        IRecipe recipe = CraftingRecipe.findRecipe(minecraft.world, inv);
        ItemStack newResult;
        if (recipe == null) {
            newResult = ItemStack.EMPTY;
        } else {
            newResult = recipe.getCraftingResult(inv);
        }
        ItemStack oldResult = container.getSlot(9).getStack();
        if (!itemStacksEqual(oldResult, newResult)) {
            container.getSlot(9).putStack(newResult);
        }

        craftingRecipe.setResult(newResult);
        updateRecipe();
        populateList();
    }

    private void updateRecipe() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }
        CraftingRecipe craftingRecipe = tileEntity.getRecipe(selected);
        boolean keepOne = "Keep".equals(keepItem.getCurrentChoice());
        CraftingRecipe.CraftMode mode;
        if ("Int".equals(internalRecipe.getCurrentChoice())) {
            mode = CraftingRecipe.CraftMode.INT;
        } else if ("Ext".equals(internalRecipe.getCurrentChoice())) {
            mode = CraftingRecipe.CraftMode.EXT;
        } else {
            mode = CraftingRecipe.CraftMode.EXTC;
        }
        craftingRecipe.setKeepOne(keepOne);
        craftingRecipe.setCraftMode(mode);
        sendChangeToServer(selected, craftingRecipe.getInventory(), craftingRecipe.getResult(), keepOne, mode);
    }

    private boolean itemStacksEqual(ItemStack matches, ItemStack oldStack) {
        if (matches.isEmpty()) {
            return oldStack.isEmpty();
        } else {
            return !oldStack.isEmpty() && matches.isItemEqual(oldStack);
        }
    }

    private void sendChangeToServer(int index, CraftingInventory inv, ItemStack result, boolean keepOne,
                                    CraftingRecipe.CraftMode mode) {

        RFToolsMessages.INSTANCE.sendToServer(new PacketCrafter(tileEntity.getPos(), index, inv,
                result, keepOne, mode));
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void render(int par1, int par2, float par3) {
        updateButtons();
        super.render(par1, par2, par3);
        testRecipe();
    }

    private void updateButtons() {
        boolean selected = recipeList.getSelected() != -1;
        keepItem.setEnabled(selected);
        internalRecipe.setEnabled(selected);
        applyButton.setEnabled(selected);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int x, int y) {
        drawWindow();

        tileEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(e -> {
            energyBar.setMaxValue(((GenericEnergyStorage)e).getCapacity());
            energyBar.setValue(((GenericEnergyStorage)e).getEnergy());
        });

        // Draw the ghost slots here
        drawGhostSlots();
    }

    private void drawGhostSlots() {
        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translatef(guiLeft, guiTop, 0.0F);
        GlStateManager.color4f(1.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 240 / 1.0F, 240 / 1.0F);

        ItemStackList ghostSlots = tileEntity.getGhostSlots();
        itemRenderer.zLevel = 100.0F;
        GlStateManager.enableDepthTest();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();

        for (int i = 0 ; i < ghostSlots.size() ; i++) {
            ItemStack stack = ghostSlots.get(i);
            if (!stack.isEmpty()) {
                int slotIdx;
                if (i < CrafterContainer.BUFFER_SIZE) {
                    slotIdx = i + CrafterContainer.SLOT_BUFFER;
                } else {
                    slotIdx = i + CrafterContainer.SLOT_BUFFEROUT - CrafterContainer.BUFFER_SIZE;
                }
                Slot slot = container.getSlot(slotIdx);
                if (!slot.getHasStack()) {
                    itemRenderer.renderItemAndEffectIntoGUI(stack, slot.xPos, slot.yPos);

                    GlStateManager.disableLighting();
                    GlStateManager.enableBlend();
                    GlStateManager.disableDepthTest();
                    this.minecraft.getTextureManager().bindTexture(iconGuiElements);
                    RenderHelper.drawTexturedModalRect(slot.xPos, slot.yPos, 14 * 16, 3 * 16, 16, 16);
                    GlStateManager.enableDepthTest();
                    GlStateManager.disableBlend();
                    GlStateManager.enableLighting();
                }
            }

        }
        itemRenderer.zLevel = 0.0F;

        GlStateManager.popMatrix();
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
    }
}
