package course.examples.audiovideo.audiorecording;

import java.io.IOException;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class AudioRecordingActivity extends Activity {
	private static final String TAG = "AudioRecordTest";
	private static final String mFileName = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/audiorecordtest.3gp";
    private static final int PERMISSIONS_REQUEST_AUDIO_RECORD = 1;
    private static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 2;
    private boolean mRecordPermissionGranted = true;
    private boolean mStoragePermissionGranted = true;
    private MediaRecorder mRecorder;
	private MediaPlayer mPlayer;
	private AudioManager mAudioManager;
    ToggleButton mRecordButton;
    ToggleButton mPlayButton;
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);

		mRecordButton = (ToggleButton) findViewById(R.id.record_button);
		mPlayButton = (ToggleButton) findViewById(R.id.play_button);

		//live record permission check method
        ensureRecordPermissions();

		// Set up record Button
		mRecordButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {

				// Set enabled state
				mPlayButton.setEnabled(!isChecked);

				// Start/stop recording
				onRecordPressed(isChecked);

			}
		});

		// Set up play Button
		mPlayButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {

				// Set enabled state
				mRecordButton.setEnabled(!isChecked);

				// Start/stop playback
				onPlayPressed(isChecked);
			}
		});

		// Get AudioManager
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		// Request audio focus
		mAudioManager.requestAudioFocus(afChangeListener,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

	}

	// Toggle recording
	private void onRecordPressed(boolean shouldStartRecording) {

		if (shouldStartRecording) {
			startRecording();
		} else {
			stopRecording();
		}

	}

	// Start recording with MediaRecorder
	private void startRecording() {

		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(mFileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e(TAG, "Couldn't prepare and start MediaRecorder");
		}

		mRecorder.start();
	}

	// Stop recording. Release resources
	private void stopRecording() {

		if (null != mRecorder) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}

	}

	// Toggle playback
	private void onPlayPressed(boolean shouldStartPlaying) {

		if (shouldStartPlaying) {
			startPlaying();
		} else {
			stopPlaying();
		}

	}

	// Playback audio using MediaPlayer
	private void startPlaying() {

		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mFileName);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			Log.e(TAG, "Couldn't prepare and start MediaPlayer");
		}

	}

	// Stop playback. Release resources
	private void stopPlaying() {
		if (null != mPlayer) {
			if (mPlayer.isPlaying())
				mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}

	// Listen for Audio Focus changes
	OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {

		@Override
		public void onAudioFocusChange(int focusChange) {

			if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				mAudioManager.abandonAudioFocus(afChangeListener);

				// Stop playback, if necessary
				if (null != mPlayer && mPlayer.isPlaying())
					stopPlaying();
			}

		}

	};

	// Release recording and playback resources, if necessary
	@Override
	public void onPause() {
		super.onPause();

		if (null != mRecorder) {
			mRecorder.release();
			mRecorder = null;
		}

		if (null != mPlayer) {
			mPlayer.release();
			mPlayer = null;
		}

	}

    private void ensureRecordPermissions() {

        // Check whether we don't already have granted a record permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Since we don't granted permission, request permission from user
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_AUDIO_RECORD);
            //nothing to do more, wait for response, till that disable
            // ability for using that feature
            mRecordButton.setEnabled(false);
            mPlayButton.setEnabled(false);
            mRecordPermissionGranted = false;
        }

        // Check whether we don't already have granted a file-write permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Since we don't granted permission, request permission from user
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
            //nothing to do more, wait for response, till that disable
            // ability for using that feature
            mRecordButton.setEnabled(false);
            mPlayButton.setEnabled(false);
            mStoragePermissionGranted= false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSIONS_REQUEST_AUDIO_RECORD: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! we can use it,
                    mRecordPermissionGranted =true;

                } else {

                    // permission denied, boo! We should keep the functionality
                    // disabled.. We also my inform user
                    Toast.makeText(getApplicationContext(),"Sorry, you have to give us permission !",
                            Toast.LENGTH_LONG).show();
                }
                break;
            }

            case PERMISSIONS_REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! we can use it,
                    mStoragePermissionGranted = true;

                } else {

                    // permission denied, boo! We should keep the functionality
                    // disabled.. We also my inform user
                    Toast.makeText(getApplicationContext(),"Sorry, you have to give us permission !",
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
        if (mRecordPermissionGranted && mStoragePermissionGranted){
            mRecordButton.setEnabled(true);
            mPlayButton.setEnabled(true);
        }
    }

}