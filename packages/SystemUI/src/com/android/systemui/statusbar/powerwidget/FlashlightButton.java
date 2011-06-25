package com.android.systemui.statusbar.powerwidget;

import com.android.systemui.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.os.Build;

import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FlashlightButton extends PowerButton {
	private static final String TAG = "FlashlightButton";
	private static int bright;
	
	private static FileWriter mWriter;
	
	private static final int MODE_DEFAULT = 0;
	private static final int MODE_HIGH = 1;
	private static boolean useDeathRay = !Build.DEVICE.equals("bravo");;
	
	private static final String FLASHLIGHT_FILE;
	private static final String FLASHLIGHT_FILE_SPOTLIGHT = "/sys/class/leds/spotlight/brightness";
	static {
		File ff = new File(FLASHLIGHT_FILE_SPOTLIGHT);
		if (ff.exists()) {
			FLASHLIGHT_FILE = FLASHLIGHT_FILE_SPOTLIGHT;
		} else {
			FLASHLIGHT_FILE = "/sys/class/leds/flashlight/brightness";
		}
	}

    public FlashlightButton() { mType = BUTTON_FLASHLIGHT; }

    @Override
    protected void updateState() {
        if(getFlashlightEnabled()) {
            mIcon = R.drawable.stat_flashlight_on;
            mState = STATE_ENABLED;
        } else {
            mIcon = R.drawable.stat_flashlight_off;
            mState = STATE_DISABLED;
        }
    }
	
	public boolean getFlashlightEnabled() {
		try {
			FileInputStream fis = new FileInputStream(FLASHLIGHT_FILE);
			int result = fis.read();
			fis.close();
			return (result != '0');
		} catch (Exception e) {
			return false;
		}
	}
	
	public void setFlashlightEnabled(boolean on) {
		try {
			if (mWriter == null) {
				mWriter = new FileWriter(FLASHLIGHT_FILE);
			}
			int value = 0;
			if (on) {
				switch (bright) {
					case MODE_HIGH:
						value = useDeathRay ? 3 : 128;
						break;
					default:
						value = 1;
						break;
				}
			}
			mWriter.write(String.valueOf(value));
			mWriter.flush();
			if (!on) {
				mWriter.close();
				mWriter = null;
			}
		} catch (Exception e) {
			Log.e(TAG, "setFlashlightEnabled failed", e);
		}
		updateState();
		updateView();
	}
	
    @Override
    protected void toggleState() {
		Context context = mView.getContext();
        bright = Settings.System.getInt(context.getContentResolver(),
                Settings.System.EXPANDED_FLASH_MODE, MODE_DEFAULT);
		boolean enabled = getFlashlightEnabled();
		setFlashlightEnabled(!enabled);
    }
}
