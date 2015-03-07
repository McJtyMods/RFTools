package com.mcjty.rftools.dimension.description;

import com.mcjty.rftools.dimension.world.types.WeatherType;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class WeatherDescriptor {
    private final float rainStrength;
    private final float thunderStrength;

    private WeatherDescriptor(Builder builder) {
        this.rainStrength = builder.rainStrength;
        this.thunderStrength = builder.thunderStrength;
    }

    public float getRainStrength() {
        return rainStrength;
    }

    public float getThunderStrength() {
        return thunderStrength;
    }

    public void toBytes(ByteBuf buf) {
        buf.writeFloat(rainStrength);
        buf.writeFloat(thunderStrength);
    }

    public void writeToNBT(NBTTagCompound compound) {
        compound.setFloat("rainStrength", rainStrength);
        compound.setFloat("thunderStrength", thunderStrength);
    }


    public static class Builder {
        private float rainStrength = -1.0f;
        private float thunderStrength = -1.0f;

        public Builder fromBytes(ByteBuf buf) {
            rainStrength = buf.readFloat();
            thunderStrength = buf.readFloat();
            return this;
        }

        public Builder fromNBT(NBTTagCompound compound) {
            rainStrength = compound.getFloat("rainStrength");
            thunderStrength = compound.getFloat("thunderStrength");
            return this;
        }

        public Builder combine(WeatherDescriptor descriptor) {
            if (descriptor.getRainStrength() > -0.1f) {
                this.rainStrength = descriptor.getRainStrength();
            }
            if (descriptor.getThunderStrength() > -0.1f) {
                this.thunderStrength = descriptor.getThunderStrength();
            }
            return this;
        }

        public Builder rainStrength(float rainStrength) {
            this.rainStrength = rainStrength;
            return this;
        }

        public Builder thunderStrength(float thunderStrength) {
            this.thunderStrength = thunderStrength;
            return this;
        }

        public Builder weatherType(WeatherType weatherType) {
            this.rainStrength = weatherType.getRainStrength();
            this.thunderStrength = weatherType.getThunderStrength();
            return this;
        }

        public WeatherDescriptor build() {
            return new WeatherDescriptor(this);
        }

    }

}
