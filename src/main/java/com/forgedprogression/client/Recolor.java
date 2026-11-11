package com.forgedprogression.client;

import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

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
        COLORED,
        /** Brown wood tones: bow limbs. Gray grip and string are left alone. */
        WOOD
    }

    /**
     * How source brightness maps onto the target color.
     *
     * @param percentile   which brightness (as a percentile of the matched pixels) gets exactly the target color
     * @param highlight    how fast pixels brighter than that blend toward white
     * @param contrast     exponent on the brightness ratio; above 1 deepens shadows and lifts highlights
     * @param sparkle      fraction of the brightest source pixels turned into near-white glints (0 = none)
     * @param sparkleAmount how far those glints are pushed toward white (0..1)
     */
    public static final class Shading {
        /** Tools, ingots, blocks: anchored high so near-white iron keeps its color. */
        public static final Shading DEFAULT = new Shading(0.9, 0.4, 1.0, 0.0, 0.0);
        /** Armor icons: a bit more contrast and highlight than tools. */
        public static final Shading ARMOR_ITEM = new Shading(0.6, 1.2, 1.4, 0.0, 0.0);
        /** Worn armor: strong contrast plus diamond-style glints, since vanilla iron armor is flat gray. */
        public static final Shading ARMOR_WORN = new Shading(0.7, 1.5, 2.2, 0.08, 0.85);

        final double percentile, highlight, contrast, sparkle, sparkleAmount;

        public Shading(double percentile, double highlight, double contrast, double sparkle, double sparkleAmount) {
            this.percentile = percentile;
            this.highlight = highlight;
            this.contrast = contrast;
            this.sparkle = sparkle;
            this.sparkleAmount = sparkleAmount;
        }
    }

    public static final class Spec {
        final ResourceLocation source;
        final Match match;
        final int color;
        final ResourceLocation background; // nullable: replaces unmatched pixels (e.g. end stone behind ore spots)
        final Shading shading;
        /** Vanilla's pulled-back bow textures draw the arrow on the diagonal; skip it so the arrow stays brown. */
        boolean skipBowArrow;
        /** Optional second color: the target fades from color (bottom-left) to color2 (top-right). -1 = none. */
        int color2 = -1;

        public Spec skipBowArrow() {
            this.skipBowArrow = true;
            return this;
        }

        public Spec gradientTo(int color2) {
            this.color2 = color2;
            return this;
        }

        public Spec(String source, Match match, int color) {
            this(source, match, color, null, Shading.DEFAULT);
        }

        public Spec(String source, Match match, int color, Shading shading) {
            this(source, match, color, null, shading);
        }

        public Spec(String source, Match match, int color, String background, Shading shading) {
            this.source = new ResourceLocation(source);
            this.match = match;
            this.color = color;
            this.background = background == null ? null : new ResourceLocation(background);
            this.shading = shading;
        }

        public BufferedImage render(IResourceManager manager) throws IOException {
            BufferedImage src = read(manager, source);
            BufferedImage bg = background == null ? null : read(manager, background);
            return apply(src, match, color, color2, bg, shading, skipBowArrow);
        }
    }

    public static BufferedImage read(IResourceManager manager, ResourceLocation loc) throws IOException {
        try (IResource res = manager.getResource(loc)) {
            return TextureUtil.readBufferedImage(res.getInputStream());
        }
    }

    public static BufferedImage apply(BufferedImage src, Match match, int target, int target2, BufferedImage background,
                                      Shading s, boolean skipBowArrow) {
        int w = src.getWidth();
        int h = src.getHeight();
        int[] px = src.getRGB(0, 0, w, h, null, 0, w);
        boolean[] hit = new boolean[px.length];

        double[] lumas = new double[px.length];
        int count = 0;
        float[] hsb = new float[3];
        for (int i = 0; i < px.length; i++) {
            int a = px[i] >>> 24;
            if (a == 0) {
                continue;
            }
            int r = (px[i] >> 16) & 0xFF, g = (px[i] >> 8) & 0xFF, b = px[i] & 0xFF;
            Color.RGBtoHSB(r, g, b, hsb);
            if (skipBowArrow && onArrowDiagonal(i % w, i / w, w)) {
                continue;
            }
            if (matches(match, hsb)) {
                hit[i] = true;
                lumas[count++] = luma(r, g, b);
            }
        }
        double reference = 128;
        double sparkleFrom = Double.MAX_VALUE;
        if (count > 0) {
            double[] sorted = Arrays.copyOf(lumas, count);
            Arrays.sort(sorted);
            reference = Math.max(1, sorted[Math.min(count - 1, (int) (count * s.percentile))]);
            if (s.sparkle > 0) {
                sparkleFrom = sorted[Math.min(count - 1, (int) (count * (1.0 - s.sparkle)))];
            }
        }

        int r1 = (target >> 16) & 0xFF, g1 = (target >> 8) & 0xFF, b1 = target & 0xFF;
        int c2 = target2 < 0 ? target : target2;
        int r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        boolean useBg = background != null && background.getWidth() == w && background.getHeight() >= h;
        int[] bgPx = useBg ? background.getRGB(0, 0, w, h, null, 0, w) : null;

        int[] out = new int[px.length];
        for (int i = 0; i < px.length; i++) {
            int a = px[i] >>> 24;
            if (hit[i]) {
                int r = (px[i] >> 16) & 0xFF, g = (px[i] >> 8) & 0xFF, b = px[i] & 0xFF;
                // diagonal position, 0 at bottom-left to 1 at top-right
                double t = (i % w + (h - 1 - i / w)) / (double) Math.max(1, (w - 1) + (h - 1));
                int tr = (int) Math.round(r1 + (r2 - r1) * t);
                int tg = (int) Math.round(g1 + (g2 - g1) * t);
                int tb = (int) Math.round(b1 + (b2 - b1) * t);
                double l = luma(r, g, b);
                double ratio = l / reference;
                if (s.contrast != 1.0) {
                    ratio = Math.pow(ratio, s.contrast);
                }
                int nr = shade(tr, ratio, s.highlight), ng = shade(tg, ratio, s.highlight), nb = shade(tb, ratio, s.highlight);
                if (l >= sparkleFrom) {
                    nr = toWhite(nr, s.sparkleAmount);
                    ng = toWhite(ng, s.sparkleAmount);
                    nb = toWhite(nb, s.sparkleAmount);
                }
                out[i] = (a << 24) | (nr << 16) | (ng << 8) | nb;
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
            case WOOD:
                return sat > 0.35F && hue >= 20F && hue <= 55F;
            default:
                return false;
        }
    }

    /** In vanilla's 16x16 pulling textures the arrow shaft sits at x == y and x == y + 1 (scaled for HD packs). */
    private static boolean onArrowDiagonal(int x, int y, int width) {
        int sx = x * 16 / width;
        int sy = y * 16 / width;
        return sx == sy || sx == sy + 1;
    }

    /** Darker than the reference: scale the target down. Brighter: blend toward white. */
    private static int shade(int c, double ratio, double highlight) {
        double v = ratio <= 1.0 ? c * ratio : c + (255 - c) * Math.min(1.0, (ratio - 1.0) * highlight);
        return clamp(v);
    }

    private static int toWhite(int c, double amount) {
        return clamp(c + (255 - c) * amount);
    }

    private static int clamp(double v) {
        return Math.max(0, Math.min(255, (int) Math.round(v)));
    }

    private static double luma(int r, int g, int b) {
        return 0.299 * r + 0.587 * g + 0.114 * b;
    }

    private Recolor() {}
}
