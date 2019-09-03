package mcjty.rftools.blocks.elevator;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.RFTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;
import java.util.Map;

public class ElevatorSounds {

    private static final Map<GlobalCoordinate, TickableSound> sounds = new HashMap<>();

    public static SoundEvent startSound;
    public static SoundEvent loopSound;
    public static SoundEvent stopSound;

    public static void init(IForgeRegistry<SoundEvent> registry) {
        startSound = registerSound(registry, new ResourceLocation(RFTools.MODID, "elevator_start"));
        loopSound = registerSound(registry, new ResourceLocation(RFTools.MODID, "elevator_loop"));
        stopSound = registerSound(registry, new ResourceLocation(RFTools.MODID, "elevator_stop"));
    }

    private static SoundEvent registerSound(IForgeRegistry<SoundEvent> registry, ResourceLocation sound) {
        SoundEvent event = new SoundEvent(sound).setRegistryName(sound);
        registry.register(event);
        return event;
    }

    public static void moveSound(World world, BlockPos pos, float y) {
        GlobalCoordinate g = new GlobalCoordinate(pos, world.getDimension().getType().getId());
        if (sounds.containsKey(g)) {
            TickableSound movingSound = sounds.get(g);
            // @todo slightly dirty. A superclass would be nice. Move to mcjtylib perhaps?
            if (movingSound instanceof ElevatorLoopSound) {
                ((ElevatorLoopSound) movingSound).move(movingSound.getX(), y, movingSound.getZ());
            } else if (movingSound instanceof ElevatorStartupSound) {
                ((ElevatorStartupSound) movingSound).move(movingSound.getX(), y, movingSound.getZ());
            } else if (movingSound instanceof ElevatorStopSound) {
                ((ElevatorStopSound) movingSound).move(movingSound.getX(), y, movingSound.getZ());
            }
        }
    }


    public static void stopSound(World worldObj, BlockPos pos) {
        GlobalCoordinate g = new GlobalCoordinate(pos, worldObj.getDimension().getType().getId());
        if (sounds.containsKey(g)) {
            TickableSound movingSound = sounds.get(g);
            Minecraft.getInstance().getSoundHandler().stop(movingSound);
            sounds.remove(g);
        }
    }

    private static void playSound(World worldObj, BlockPos pos, TickableSound sound) {
        stopSound(worldObj, pos);
        Minecraft.getInstance().getSoundHandler().play(sound);
        GlobalCoordinate g = new GlobalCoordinate(pos, worldObj.getDimension().getType().getId());
        sounds.put(g, sound);
    }


    public static void playStartup(World worldObj, BlockPos pos) {
        TickableSound sound = new ElevatorStartupSound(worldObj, pos.getX(), pos.getY(), pos.getZ());
        playSound(worldObj, pos, sound);
    }

    public static void playLoop(World worldObj, BlockPos pos) {
        TickableSound sound = new ElevatorLoopSound(worldObj, pos.getX(), pos.getY(), pos.getZ());
        playSound(worldObj, pos, sound);
    }

    public static void playStop(World worldObj, BlockPos pos) {
        TickableSound sound = new ElevatorStopSound(worldObj, pos.getX(), pos.getY(), pos.getZ());
        playSound(worldObj, pos, sound);
    }

    public static boolean isStartupPlaying(World worldObj, BlockPos pos) {
        GlobalCoordinate g = new GlobalCoordinate(pos, worldObj.getDimension().getType().getId());
        TickableSound movingSound = sounds.get(g);
        return movingSound instanceof ElevatorStartupSound;
    }

    public static boolean isLoopPlaying(World worldObj, BlockPos pos) {
        GlobalCoordinate g = new GlobalCoordinate(pos, worldObj.getDimension().getType().getId());
        TickableSound movingSound = sounds.get(g);
        return movingSound instanceof ElevatorLoopSound;
    }

    public static boolean isStopPlaying(World worldObj, BlockPos pos) {
        GlobalCoordinate g = new GlobalCoordinate(pos, worldObj.getDimension().getType().getId());
        TickableSound movingSound = sounds.get(g);
        return movingSound instanceof ElevatorStopSound;
    }
}
