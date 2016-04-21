package mcjty.rftools.blocks.spawner;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.entity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.SyringeItem;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.List;
import java.util.Set;


public class GuiSpawner extends GenericGuiContainer<SpawnerTileEntity> {
    private static final int SPAWNER_WIDTH = 180;
    private static final int SPAWNER_HEIGHT = 152;

    private EnergyBar energyBar;
    private BlockRender blocks[] = new BlockRender[3];
    private Label labels[] = new Label[3];
    private Label name;
    private Label rfTick;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/spawner.png");

    public GuiSpawner(SpawnerTileEntity spawnerTileEntity, SpawnerContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, spawnerTileEntity, container, RFTools.GUI_MANUAL_MAIN, "spawner");
        GenericEnergyStorageTileEntity.setCurrentRF(spawnerTileEntity.getEnergyStored(EnumFacing.DOWN));

        xSize = SPAWNER_WIDTH;
        ySize = SPAWNER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(EnumFacing.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 8, 54)).setShowText(false);
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());

        blocks[0] = new BlockRender(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(110, 5, 18, 18));
        blocks[1] = new BlockRender(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(110, 25, 18, 18));
        blocks[2] = new BlockRender(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(110, 45, 18, 18));
        labels[0] = new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT); labels[0].setLayoutHint(new PositionalLayout.PositionalHint(130, 5, 40, 18));
        labels[1] = new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT); labels[1].setLayoutHint(new PositionalLayout.PositionalHint(130, 25, 40, 18));
        labels[2] = new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT); labels[2].setLayoutHint(new PositionalLayout.PositionalHint(130, 45, 40, 18));
        name = new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT); name.setLayoutHint(new PositionalLayout.PositionalHint(28, 31, 78, 16));
        rfTick = new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT); rfTick.setLayoutHint(new PositionalLayout.PositionalHint(28, 47, 78, 16));

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).
                addChild(blocks[0]).addChild(labels[0]).
                addChild(blocks[1]).addChild(labels[1]).
                addChild(blocks[2]).addChild(labels[2]).
                addChild(rfTick).addChild(name);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        tileEntity.requestRfFromServer(RFTools.MODID);
    }

    private void showSyringeInfo() {
        for (int i = 0 ; i < 3 ; i++) {
            blocks[i].setRenderItem(null);
            labels[i].setText("");
        }
        name.setText("");
        rfTick.setText("");

        ItemStack stack = tileEntity.getStackInSlot(SpawnerContainer.SLOT_SYRINGE);
        if (stack == null || stack.stackSize == 0) {
            return;
        }

        String mobId = SyringeItem.getMobId(stack);
        if (mobId != null) {
            String mobName = SyringeItem.getMobName(stack);
            name.setText(mobName);
            rfTick.setText(SpawnerConfiguration.mobSpawnRf.get(mobId) + "RF");
            int i = 0;
            List<SpawnerConfiguration.MobSpawnAmount> list = SpawnerConfiguration.mobSpawnAmounts.get(mobId);
            if (list != null) {
                for (SpawnerConfiguration.MobSpawnAmount spawnAmount : list) {
                    ItemStack b = spawnAmount.getObject();
                    float amount = spawnAmount.getAmount();
                    if (b == null) {
                        Object[] blocks = {Blocks.LEAVES, Blocks.PUMPKIN, Items.WHEAT, Items.POTATO, Items.BEEF};
                        int index = (int) ((System.currentTimeMillis() / 500) % blocks.length);
                        if (blocks[index] instanceof Block) {
                            this.blocks[i].setRenderItem(new ItemStack((Block) blocks[index], 1, 0));
                        } else {
                            this.blocks[i].setRenderItem(new ItemStack((Item) blocks[index], 1, 0));
                        }
                    } else {
                        blocks[i].setRenderItem(b);
                    }
                    labels[i].setText(Float.toString(amount));
                    i++;
                }
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        showSyringeInfo();

        drawWindow();
        int currentRF = GenericEnergyStorageTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFTools.MODID);
    }
}
