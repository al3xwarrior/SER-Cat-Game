package Utils;

import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;

// This class has some useful image methods that are used when loading in images to the game
public class ImageUtils {
	// cache of tinted images so the same image/color combination isn't re-rendered every frame
	private static final HashMap<String, BufferedImage> tintCache = new HashMap<>();

	// returns a copy of the given image tinted with the given color, preserving the image's original transparency/silhouette
	// used to give the player a "powered up" look (e.g. while invincible) without needing separate sprite art
	public static BufferedImage tintImage(BufferedImage image, Color tintColor) {
		String cacheKey = System.identityHashCode(image) + ":" + tintColor.getRGB();
		if (tintCache.containsKey(cacheKey)) {
			return tintCache.get(cacheKey);
		}

		BufferedImage tintedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = tintedImage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.setComposite(AlphaComposite.SrcAtop);
		g.setColor(tintColor);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		g.dispose();

		tintCache.put(cacheKey, tintedImage);
		return tintedImage;
	}
	// changes desired color to be transparent (the chosen color will not be seen in game when drawn)
	public static BufferedImage transformColorToTransparency(BufferedImage image, Color transparentColor) {
		BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = newImage.createGraphics();
		int transparentColorIndex = transparentColor.getRGB();

		// iterates through each pixel of the image
		// if pixel is equal to the transparent color, changes that pixel to be fully transparent
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				int rgb = image.getRGB(i, j);
				if (rgb == transparentColorIndex) {
					g.setColor(new Color(0, true));
					g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
				}
				else {
					g.setColor(new Color(rgb, false));
					g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
				}
				g.drawRect(i, j, 1, 1);
			}
		}
		g.dispose();
		return newImage;
	}

	// https://stackoverflow.com/a/4216315
	// resizes an image
	public static BufferedImage resizeImage(BufferedImage image, int newWidth, int newHeight) {
		BufferedImage resized = new BufferedImage(newWidth, newHeight, image.getType());
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(image, 0, 0, newWidth, newHeight, 0, 0, image.getWidth(),
				image.getHeight(), null);
		g.dispose();
		return resized;
	}

	public static BufferedImage createSolidImage(Color color) {
		BufferedImage nothing = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		nothing = ImageUtils.transformColorToTransparency(nothing, color);
		return nothing;
	}

	public static BufferedImage createSolidImage(Color color, int width, int height) {
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		image = ImageUtils.transformColorToTransparency(image, color);
		return resizeImage(image, width, height);
	}
}
