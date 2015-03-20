package com.mcjty.rftools.blocks.spawner;

import com.mcjty.container.GenericGuiContainer;
import com.mcjty.gui.Window;
import com.mcjty.gui.layout.HorizontalAlignment;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.rftools.RFTools;
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
    private Label name;
    private Label rfTick;

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

        tileEntity.requestRfFromServer();
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
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null) {
            String mob = tagCompound.getString("mobName");
            if (mob != null && !mob.isEmpty()) {
                name.setText(mob);
                rfTick.setText(SpawnerConfiguration.mobSpawnRf.get(mob) + "RF");
                int i = 0;
                List<SpawnerConfiguration.MobSpawnAmount> list = SpawnerConfiguration.mobSpawnAmounts.get(mob);
                for (SpawnerConfiguration.MobSpawnAmount spawnAmount : list) {
                    ItemStack b = spawnAmount.getObject();
                    float amount = spawnAmount.getAmount();
                    if (b == null) {
                        blocks[i].setRenderItem(new ItemStack(Blocks.leaves, 1, 0));
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

        window.draw();
        int currentRF = tileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer();
    }
}
