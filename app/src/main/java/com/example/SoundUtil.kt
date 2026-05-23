package com.example

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper

object SoundUtil {
    fun playConfirmationSound() {
        try {
            // Use ToneGenerator to play a quick, high-quality, lightweight beep sound
            val toneG = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
            // TONE_PROP_ACK is a pleasant double pip/beep indicating acknowledgment/success
            toneG.startTone(ToneGenerator.TONE_PROP_ACK, 200)
            
            // Release the native tone generator instance after the sound has finished playing
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    toneG.release()
                } catch (e: Exception) {
                    // Ignore exception on release
                }
            }, 300)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
