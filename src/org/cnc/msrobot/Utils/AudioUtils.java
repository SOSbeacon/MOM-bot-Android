package org.cnc.msrobot.utils;

import java.io.File;
import java.io.IOException;

import android.media.MediaRecorder;

public class AudioUtils {
	private static final String TAG = "AudioUtils";
	private String mFilePath;
	private MediaRecorder mMediaRecorder = null;
	private boolean isRecording = false;

	public void startRecord() {

		// Create temp folder if not exist
		File file = new File(Consts.TEMP_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
		mFilePath = "";

		// Configure media recorder
		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mMediaRecorder.setOutputFile(Consts.RECORD_TEMP_PATH);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

		try {
			mMediaRecorder.prepare();
			mMediaRecorder.start();
		} catch (IOException e) {
			Logger.error(TAG, "prepare() failed");
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
			return;
		}
		isRecording = true;
	}

	public void stopRecord() {
		if (mMediaRecorder != null) {
			Logger.debug(TAG, "There's a record");
			try {
				mMediaRecorder.stop();
				mMediaRecorder.reset();
				mMediaRecorder.release();
				isRecording = false;
			} catch (Exception e) {
				Logger.error(TAG, "stop recording exception:" + e.getMessage());
				e.printStackTrace();
			}
			mMediaRecorder = null;
		}

		// save audio file to final if having record
		setFinalRecordFile();
	}

	/**
	 * Save the audio file from temp path to final path
	 */
	private void setFinalRecordFile() {
		File tempFile = new File(Consts.RECORD_TEMP_PATH);
		// Final path is set with time stamp.
		mFilePath = Consts.TEMP_AUDIO_FILE.replace(Consts.TIME_STAMP, System.currentTimeMillis() + "");
		File tempFinalFile = new File(mFilePath);
		if (tempFile.exists()) {
			tempFile.renameTo(tempFinalFile);
		}
	}

	public String getRecordFilePath() {
		return mFilePath;
	}

	public boolean isRecording() {
		return isRecording;
	}
}
