package com.mcjty.rftools.dimension;

public class SkyDescriptor {
    private final Float sunBrightnessFactor;
    private final Float starBrightnessFactor;

    private SkyDescriptor(Builder builder) {
        sunBrightnessFactor = builder.sunBrightnessFactor;
        starBrightnessFactor = builder.starBrightnessFactor;
    }

    public float getSunBrightnessFactor() {
        return sunBrightnessFactor == null ? 1.0f : sunBrightnessFactor;
    }

    public float getStarBrightnessFactor() {
        return starBrightnessFactor == null ? 1.0f : starBrightnessFactor;
    }

    public static class Builder {
        private Float sunBrightnessFactor = null;
        private Float starBrightnessFactor = null;

        public Builder combine(SkyDescriptor descriptor) {
            if (descriptor.starBrightnessFactor != null) {
                starBrightnessFactor(descriptor.getStarBrightnessFactor());
            }
            if (descriptor.sunBrightnessFactor != null) {
                sunBrightnessFactor(descriptor.getSunBrightnessFactor());
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

        public SkyDescriptor build() {
            return new SkyDescriptor(this);
        }
    }
}
