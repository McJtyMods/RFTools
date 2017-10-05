package mcjty.rftools.blocks.shaper;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.RFTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class ProjectorSounds {

    private static final Map<GlobalCoordinate, MovingSound> sounds = new HashMap<>();

    public static SoundEvent scanSound;

    public static void init() {
        scanSound = registerSound(new ResourceLocation(RFTools.MODID, "scan"));
    }

    private static SoundEvent registerSound(ResourceLocation sound) {
        SoundEvent event = new SoundEvent(sound).setRegistryName(sound);
        SoundEvent.REGISTRY.register(-1, sound, event);
        return event;
    }

    public static void stopSound(World worldObj, BlockPos pos) {
        GlobalCoordinate g = new GlobalCoordinate(pos, worldObj.provider.getDimension());
        if (sounds.containsKey(g)) {
            MovingSound movingSound = sounds.get(g);
            Minecraft.getMinecraft().getSoundHandler().stopSound(movingSound);
            sounds.remove(g);
        }
    }

    private static void playSound(World worldObj, BlockPos pos, MovingSound sound) {
        stopSound(worldObj, pos);
        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
        GlobalCoordinate g = new GlobalCoordinate(pos, worldObj.provider.getDimension());
        sounds.put(g, sound);
    }


    public static void playScan(World worldObj, BlockPos pos) {
        MovingSound sound = new ProjectorSound(worldObj, pos.getX(), pos.getY(), pos.getZ());
        playSound(worldObj, pos, sound);
    }

    public static boolean isScanPlaying(World worldObj, BlockPos pos) {
        GlobalCoordinate g = new GlobalCoordinate(pos, worldObj.provider.getDimension());
        MovingSound movingSound = sounds.get(g);
        return movingSound instanceof ProjectorSound;
    }
}
