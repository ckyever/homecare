package com.example.sayyaf.homecare.communication;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.example.sayyaf.homecare.R;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Class handles audio sounds played when call is sent or call is incoming
 * Adapted from Sinch SDK
 */

public class AudioPlayer {

    static final String TAG = AudioPlayer.class.getSimpleName();

    private Context mContext;

    private MediaPlayer mMediaPlayer;

    private AudioTrack mProgressTone;

    private final static int SAMPLE_RATE = 16000;

    public AudioPlayer(Context context) {
        this.mContext = context.getApplicationContext();
    }

    /**
     * Plays ringtone when requested, such as when asynch call from incoming call
     * or when sending a call and call is ringing
     */
    public void playRingtone() {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        /* Check if phone is on silent mode. If so, don't ring the tone. Otherwise
        ring
         */
        switch (manager.getRingerMode()) {
            case AudioManager.RINGER_MODE_NORMAL:
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);

                try {
                    //Sets the audio file to be played on ring
                    mMediaPlayer.setDataSource(mContext,
                            Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.phone_loud1));
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    mMediaPlayer = null;
                    Log.e(TAG, "Error setting up ringtone");

                    return;
                }
                mMediaPlayer.setLooping(true);
                mMediaPlayer.start();
                break;
        }
    }

    /**
     * Method to stop the ring tone when necessary state has been reached
     */
    public void stopRingtone() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * Method to stop the progressing call ring tone when necessary state has been reached
     */
    public void stopProgressTone() {
        if (mProgressTone != null) {
            mProgressTone.stop();
            mProgressTone.release();
            mProgressTone = null;
        }
    }


    /**
     * Creates the progress tone for the outgoing call.
     * @param context context of the voice or video call screen depending on call type
     * @return audio file to run
     * @throws IOException
     */
    private static AudioTrack createProgressTone(Context context) throws IOException {
        AssetFileDescriptor fd = context.getResources().openRawResourceFd(R.raw.progress_tone);
        int length = (int) fd.getLength();

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, length, AudioTrack.MODE_STATIC);

        byte[] data = new byte[length];
        convertRingerFileToBytes(fd, data);

        audioTrack.write(data, 0, data.length);
        audioTrack.setLoopPoints(0, data.length / 2, 30);

        return audioTrack;
    }

    /**
     * Play the progress tone when requested
     */
    public void playProgressTone() {
        stopProgressTone();
        try {
            mProgressTone = createProgressTone(mContext);
            mProgressTone.play();
        } catch (Exception e) {
            Log.e(TAG, "Error playing progress tone", e);
        }
    }

    /**
     * For audio file, convert to an array of bytes for use
     * @param fileDescriptor audio file to convert
     * @param data array to fill with bytes
     * @throws IOException
     */
    private static void convertRingerFileToBytes(AssetFileDescriptor fileDescriptor, byte[] data) throws IOException {
        FileInputStream stream = fileDescriptor.createInputStream();

        int bytes = 0;
        while (bytes < data.length) {
            int resolution = stream.read(data, bytes, (data.length - bytes));
            if (resolution == -1) {
                break;
            }
            bytes+= resolution;
        }
    }
}
