package com.research.util;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.LruCache;
import android.widget.ImageView;

public class ImageUtil extends LruCache<String, Bitmap> {
	// display density
	private static final float displayDensity = Resources.getSystem()
			.getDisplayMetrics().density;
	// lruCache
	private static LruCache<String, Bitmap> lruCache = new ImageUtil(
			((int) Runtime.getRuntime().maxMemory()) / 16);

	public ImageUtil(int maxSize) {
		super(maxSize);
	}

	@Override
	protected int sizeOf(String key, Bitmap value) {
		return value.getByteCount();
	}

	public static void setImage(String str, PackageManager packageManager,
			ImageView imageView) {
		if (imageView != null) {
			if (TextUtils.isEmpty(str) || packageManager == null) {
				imageView.setImageResource(17301651);
				return;
			}
			Bitmap bitmap = (Bitmap) lruCache.get(str);
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
				return;
			}
			try {
				Drawable loadIcon = packageManager.getApplicationInfo(str, 1)
						.loadIcon(packageManager);
				if (loadIcon instanceof BitmapDrawable) {
					bitmap = Bitmap.createScaledBitmap(
							((BitmapDrawable) loadIcon).getBitmap(),
							dp2Px(40), dp2Px(40), false);
					imageView.setImageBitmap(bitmap);
					lruCache.put(str, bitmap);
					return;
				}
				imageView.setImageDrawable(loadIcon);
			} catch (Exception e) {
				imageView.setImageResource(17301651);
				e.printStackTrace();
			}
		}
	}

	/***
	 * dp to px
	 * @param i
	 * @return
	 */
	public static int dp2Px(int i) {
		return Math.round(((float) i) * displayDensity);
	}

}
