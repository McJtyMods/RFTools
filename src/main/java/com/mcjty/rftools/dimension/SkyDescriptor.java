package com.mcjty.rftools.dimension;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class SkyDescriptor {
    private final Float sunBrightnessFactor;
    private final Float starBrightnessFactor;

    private final Float skyColorFactorR;
    private final Float skyColorFactorG;
    private final Float skyColorFactorB;

    private SkyDescriptor(Builder builder) {
        sunBrightnessFactor = builder.sunBrightnessFactor;
        starBrightnessFactor = builder.starBrightnessFactor;
        skyColorFactorR = builder.skyColorFactorR;
        skyColorFactorG = builder.skyColorFactorG;
        skyColorFactorB = builder.skyColorFactorB;
    }

    public void toBytes(ByteBuf buf) {
        writeFloat(buf, sunBrightnessFactor);
        writeFloat(buf, starBrightnessFactor);
        writeFloat(buf, skyColorFactorR);
        writeFloat(buf, skyColorFactorG);
        writeFloat(buf, skyColorFactorB);
    }

    public void writeToNBT(NBTTagCompound compound) {
        if (sunBrightnessFactor != null) {
            compound.setFloat("sunBrightness", sunBrightnessFactor);
        }
        if (starBrightnessFactor != null) {
            compound.setFloat("starBrightness", starBrightnessFactor);
        }
        if (skyColorFactorR != null) {
            compound.setFloat("skyColorFactorR", skyColorFactorR);
        }
        if (skyColorFactorG != null) {
            compound.setFloat("skyColorFactorG", skyColorFactorG);
        }
        if (skyColorFactorB != null) {
            compound.setFloat("skyColorFactorB", skyColorFactorB);
        }
    }

    private void writeFloat(ByteBuf buf, Float value) {
        if (value == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeFloat(value);
        }
    }

    public float getSunBrightnessFactor() {
        return sunBrightnessFactor == null ? 1.0f : sunBrightnessFactor;
    }

    public float getStarBrightnessFactor() {
        return starBrightnessFactor == null ? 1.0f : starBrightnessFactor;
    }

    public float getSkyColorFactorR() {
        return skyColorFactorR == null ? 1.0f : skyColorFactorR;
    }

    public float getSkyColorFactorG() {
        return skyColorFactorG == null ? 1.0f : skyColorFactorG;
    }

    public float getSkyColorFactorB() {
        return skyColorFactorB == null ? 1.0f : skyColorFactorB;
    }

    public static class Builder {
        private Float sunBrightnessFactor = null;
        private Float starBrightnessFactor = null;
        private Float skyColorFactorR = null;
        private Float skyColorFactorG = null;
        private Float skyColorFactorB = null;

        public Builder fromBytes(ByteBuf buf) {
            sunBrightnessFactor = readFloat(buf);
            starBrightnessFactor = readFloat(buf);
            skyColorFactorR = readFloat(buf);
            skyColorFactorG = readFloat(buf);
            skyColorFactorB = readFloat(buf);
            return this;
        }

        public Builder fromNBT(NBTTagCompound compound) {
            if (compound.hasKey("sunBrightness")) {
                sunBrightnessFactor = compound.getFloat("sunBrightness");
            } else {
                sunBrightnessFactor = null;
            }
            if (compound.hasKey("starBrightness")) {
                starBrightnessFactor = compound.getFloat("starBrightness");
            } else {
                starBrightnessFactor = null;
            }
            if (compound.hasKey("skyColorFactorR")) {
                skyColorFactorR = compound.getFloat("skyColorFactorR");
            } else {
                skyColorFactorR = null;
            }
            if (compound.hasKey("skyColorFactorG")) {
                skyColorFactorG = compound.getFloat("skyColorFactorG");
            } else {
                skyColorFactorG = null;
            }
            if (compound.hasKey("skyColorFactorB")) {
                skyColorFactorB = compound.getFloat("skyColorFactorB");
            } else {
                skyColorFactorB = null;
            }
            return this;
        }

        private Float readFloat(ByteBuf buf) {
            if (buf.readBoolean()) {
                return buf.readFloat();
            } else {
                return null;
            }
        }

        public Builder combine(SkyDescriptor descriptor) {
            if (descriptor.starBrightnessFactor != null) {
                starBrightnessFactor(descriptor.getStarBrightnessFactor());
            }
            if (descriptor.sunBrightnessFactor != null) {
                sunBrightnessFactor(descriptor.getSunBrightnessFactor());
            }
            if (descriptor.skyColorFactorR != null) {
                if (this.skyColorFactorR == null) {
                    this.skyColorFactorR = descriptor.skyColorFactorR;
                } else {
                    this.skyColorFactorR *= descriptor.skyColorFactorR;
                }
            }
            if (descriptor.skyColorFactorG != null) {
                if (this.skyColorFactorG == null) {
                    this.skyColorFactorG = descriptor.skyColorFactorG;
                } else {
                    this.skyColorFactorG *= descriptor.skyColorFactorG;
                }
            }
            if (descriptor.skyColorFactorB != null) {
                if (this.skyColorFactorB == null) {
                    this.skyColorFactorB = descriptor.skyColorFactorB;
                } else {
                    this.skyColorFactorB *= descriptor.skyColorFactorB;
                }
            }
            return this;
        }

        public Builder sunBrightnessFactor(float f) {
            this.sunBrightnessFactor = f;
            return this;
        }

        public Builder starBrightnessFactor(float f) {
            this.starBrightnessFactor = f;
            return this;
        }

        public Builder skyColorFactor(float r, float g, float b) {
            this.skyColorFactorR = r;
            this.skyColorFactorG = g;
            this.skyColorFactorB = b;
            return this;
        }

        public SkyDescriptor build() {
            return new SkyDescriptor(this);
        }
    }
}
