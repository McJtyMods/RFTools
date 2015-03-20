package com.mcjty.rftools.blocks.spawner;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.layout.HorizontalAlignment;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.dimlets.MobConfiguration;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;
import java.util.List;


public class GuiSpawner extends GenericGuiContainer<SpawnerTileEntity> {
    public static final int SPAWNER_WIDTH = 180;
    public static final int SPAWNER_HEIGHT = 152;

    private EnergyBar energyBar;
    private BlockRender blocks[] = new BlockRender[3];
    private Label labels[] = new Label[3];

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/spawner.png");

    public GuiSpawner(SpawnerTileEntity spawnerTileEntity, SpawnerContainer container) {
        super(spawnerTileEntity, container);
        spawnerTileEntity.setCurrentRF(spawnerTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = SPAWNER_WIDTH;
        ySize = SPAWNER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 8, 54)).setShowText(false);
        energyBar.setValue(tileEntity.getCurrentRF());

        blocks[0] = new BlockRender(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(60, 5, 18, 18));
        blocks[1] = new BlockRender(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(60, 25, 18, 18));
        blocks[2] = new BlockRender(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(60, 45, 18, 18));
        labels[0] = new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT); labels[0].setLayoutHint(new PositionalLayout.PositionalHint(80, 5, 90, 18));
        labels[1] = new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT); labels[1].setLayoutHint(new PositionalLayout.PositionalHint(80, 25, 90, 18));
        labels[2] = new Label(mc, this).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT); labels[2].setLayoutHint(new PositionalLayout.PositionalHint(80, 45, 90, 18));

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).
                addChild(blocks[0]).addChild(labels[0]).
                addChild(blocks[1]).addChild(labels[1]).
                addChild(blocks[2]).addChild(labels[2]);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        tileEntity.requestRfFromServer();
    }

    private void showSyringeInfo() {
        for (int i = 0 ; i < 3 ; i++) {
            blocks[i].setRenderItem(null);
            labels[i].setText("");
        }

        ItemStack stack = tileEntity.getStackInSlot(SpawnerContainer.SLOT_SYRINGE);
        if (stack == null || stack.stackSize == 0) {
            return;
        }
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null) {
            String mob = tagCompound.getString("mobName");
            if (mob != null && !mob.isEmpty()) {
                int i = 0;
                List<MobConfiguration.MobSpawnAmount> list = MobConfiguration.mobSpawnAmounts.get(mob);
                for (MobConfiguration.MobSpawnAmount spawnAmount : list) {
                    Object b = spawnAmount.getObject();
                    int meta = spawnAmount.getMeta();
                    float amount = spawnAmount.getAmount();
                    if (b instanceof String) {
                        String s = (String) b;
                        if (s.equals("living")) {
                            blocks[i].setRenderItem(new ItemStack(Blocks.leaves, 1, meta));
                        } else {
                            continue;
                        }
                    } else if (b instanceof Block) {
                        blocks[i].setRenderItem(new ItemStack((Block) b, 1, meta));
                    } else if (b instanceof Item) {
                        blocks[i].setRenderItem(new ItemStack((Item) b, 1, meta));
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

        window.draw();
        int currentRF = tileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer();
    }
}
