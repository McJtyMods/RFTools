package com.mcjty.rftools.blocks.shards;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class DimensionalShardBlock extends Block {

    private IIcon icon;

    public DimensionalShardBlock() {
        super(Material.rock);
        setHardness(3.0f);
        setResistance(5.0f);
        setHarvestLevel("pickaxe", 2);
        setBlockName("dimensionalShardBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getLightValue() {
        return 6;
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int meta) {
        if (world.isRemote) {
            for (int i = 0 ; i < 10 ; i++) {
                world.spawnParticle("fireworksSpark", x + 0.5f, y + 0.5f, z + 0.5f, rand.nextGaussian()/3.0f, rand.nextGaussian()/3.0f, rand.nextGaussian()/3.0f);
            }
        }
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(RFTools.MODID + ":dimensionalShardOre");
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return icon;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return icon;
    }

    @Override
    public Item getItemDropped(int p_149650_1_, Random random, int p_149650_3_) {
        return ModItems.dimensionalShard;
    }

    @Override
    public int quantityDropped(Random random) {
        return 2 + random.nextInt(3);
    }

    @Override
    public int quantityDroppedWithBonus(int bonus, Random random) {
        int j = random.nextInt(bonus + 2) - 1;
        if (j < 0) {
            j = 0;
        }

        return this.quantityDropped(random) * (j + 1);
    }

    private Random rand = new Random();

    @Override
    public int getExpDrop(IBlockAccess world, int metadata, int fortune) {
        return MathHelper.getRandomIntegerInRange(rand, 3, 7);
    }
}
