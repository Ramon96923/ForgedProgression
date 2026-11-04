package com.forgedprogression.client;

import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Recolors a vanilla texture: picks out the "metal" pixels (gray for iron, cyan for diamond,
 * any colored pixel for ore spots) and repaints them in a target color, keeping their shading.
 * Everything else (wooden handles, stone background) is left alone or taken from a background texture.
 */
public final class Recolor {

    public enum Match {
        /** Low-saturation pixels: iron tools, armor, ingots, blocks. Leaves wooden handles alone. */
        GRAY,
        /** Cyan/teal pixels: diamond items and blocks. */
        CYAN,
        /** Any saturated pixel: the colored spots on ore textures (stone stays as is). */
        COLORED
    }

    public static final class Spec {
        final ResourceLocation source;
        final Match match;
        final int color;
        final ResourceLocation background; // nullable: replaces unmatched pixels (e.g. end stone behind ore spots)

        public Spec(String source, Match match, int color) {
            this(source, match, color, null);
        }

        public Spec(String source, Match match, int color, String background) {
            this.source = new ResourceLocation(source);
            this.match = match;
            this.color = color;
            this.background = background == null ? null : new ResourceLocation(background);
        }

        public BufferedImage render(IResourceManager manager) throws IOException {
            BufferedImage src = read(manager, source);
            BufferedImage bg = background == null ? null : read(manager, background);
            return apply(src, match, color, bg);
        }
    }

    public static BufferedImage read(IResourceManager manager, ResourceLocation loc) throws IOException {
        try (IResource res = manager.getResource(loc)) {
            return TextureUtil.readBufferedImage(res.getInputStream());
        }
    }

    public static BufferedImage apply(BufferedImage src, Match match, int target, BufferedImage background) {
        int w = src.getWidth();
        int h = src.getHeight();
        int[] px = src.getRGB(0, 0, w, h, null, 0, w);
        boolean[] hit = new boolean[px.length];

        // average brightness of the matched pixels, so the target color lands on the mid-tone
        double sum = 0;
        int count = 0;
        float[] hsb = new float[3];
        for (int i = 0; i < px.length; i++) {
            int a = px[i] >>> 24;
            if (a == 0) {
                continue;
            }
            int r = (px[i] >> 16) & 0xFF, g = (px[i] >> 8) & 0xFF, b = px[i] & 0xFF;
            Color.RGBtoHSB(r, g, b, hsb);
            if (matches(match, hsb)) {
                hit[i] = true;
                sum += luma(r, g, b);
                count++;
            }
        }
        double mean = count == 0 ? 128 : Math.max(1, sum / count);

        int tr = (target >> 16) & 0xFF, tg = (target >> 8) & 0xFF, tb = target & 0xFF;
        boolean useBg = background != null && background.getWidth() == w && background.getHeight() >= h;
        int[] bgPx = useBg ? background.getRGB(0, 0, w, h, null, 0, w) : null;

        int[] out = new int[px.length];
        for (int i = 0; i < px.length; i++) {
            int a = px[i] >>> 24;
            if (hit[i]) {
                int r = (px[i] >> 16) & 0xFF, g = (px[i] >> 8) & 0xFF, b = px[i] & 0xFF;
                double ratio = luma(r, g, b) / mean;
                out[i] = (a << 24) | (shade(tr, ratio) << 16) | (shade(tg, ratio) << 8) | shade(tb, ratio);
            } else if (useBg && a != 0) {
                out[i] = bgPx[i];
            } else {
                out[i] = px[i];
            }
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, w, h, out, 0, w);
        return img;
    }

    private static boolean matches(Match match, float[] hsb) {
        float hue = hsb[0] * 360F, sat = hsb[1], bri = hsb[2];
        switch (match) {
            case GRAY:
                return sat < 0.22F;
            case CYAN:
                return sat > 0.12F && hue >= 140F && hue <= 220F;
            case COLORED:
                return sat > 0.15F && bri > 0.15F;
            default:
                return false;
        }
    }

    /** Darker than average: scale the target down. Brighter: blend toward white for highlights. */
    private static int shade(int c, double ratio) {
        double v = ratio <= 1.0 ? c * ratio : c + (255 - c) * Math.min(1.0, (ratio - 1.0) * 1.3);
        return Math.max(0, Math.min(255, (int) Math.round(v)));
    }

    private static double luma(int r, int g, int b) {
        return 0.299 * r + 0.587 * g + 0.114 * b;
    }

    private Recolor() {}
}
