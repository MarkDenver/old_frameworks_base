package com.android.systemui.statusbar.powerwidget;

import com.android.systemui.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.view.KeyEvent;

public class MediaPlayPauseButton extends MediaKeyEventButton {

    private static final int MEDIA_STATE_UNKNOWN  = -1;
    private static final int MEDIA_STATE_INACTIVE =  0;
    private static final int MEDIA_STATE_ACTIVE   =  1;

    private int mCurrentState = MEDIA_STATE_UNKNOWN;
	
	private static final String PLAY_STATE_CHANGED = "com.android.music.playstatechanged";
	
	private boolean shouldBePlaying = false;
	private Handler handler = new Handler();

	public MediaPlayPauseButton() {
		mType = BUTTON_MEDIA_PLAY_PAUSE;
	}

    @Override
    protected void updateState() {
        mState = STATE_DISABLED;
        if(isMusicActive()) {
            mIcon = R.drawable.stat_media_pause;
        } else {
            mIcon = R.drawable.stat_media_play;
        }
    }

    @Override
    protected void toggleState() {
        sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);

        mCurrentState = (isMusicActive() ? MEDIA_STATE_INACTIVE : MEDIA_STATE_ACTIVE);

		handler.postDelayed (new Runnable () {
			public void run () {
				update();
			}
		}, 20);
    }

    private boolean isMusicActive() {
        if(mCurrentState == MEDIA_STATE_UNKNOWN) {
            mCurrentState = MEDIA_STATE_INACTIVE;
			try {
				AudioManager am = getAudioManager(mView.getContext());
				if(am != null) {
					mCurrentState = (am.isMusicActive() ? MEDIA_STATE_ACTIVE : MEDIA_STATE_INACTIVE);
				}
				if (shouldBePlaying)
					return true;
			} catch (Exception e) {
				// do nothing
			}

            return (mCurrentState == MEDIA_STATE_ACTIVE);
        } else {
            boolean active = (mCurrentState == MEDIA_STATE_ACTIVE);
            mCurrentState = MEDIA_STATE_UNKNOWN;
            return active;
        }
    }

	@Override
	protected IntentFilter getBroadcastIntentFilter() {
		IntentFilter iF = new IntentFilter();
		iF.addAction(PLAY_STATE_CHANGED);
		return iF;
	}

	@Override
	protected void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(PLAY_STATE_CHANGED)) {
			if (intent.hasExtra("playing")) {
				shouldBePlaying = intent.getBooleanExtra("playing", false);
				handler.postDelayed (new Runnable () {
					public void run () {
						update();
					}
				}, 120);
			}
		}
	}
}
