package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

object SirenAudioPlayer {

    private var sirenJob: Job? = null
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false

    fun isSirenActive(): Boolean = isPlaying

    fun startSiren(scope: CoroutineScope) {
        if (isPlaying) return
        isPlaying = true

        sirenJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = (minBufferSize * 2).coerceAtLeast(sampleRate / 2)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack = track
            track.play()

            val samples = ShortArray(bufferSize)
            var phase = 0.0
            var t = 0.0
            val dt = 1.0 / sampleRate

            try {
                while (isActive && isPlaying) {
                    // Waver frequency between 700 Hz and 1600 Hz like emergency police siren
                    val modulation = (sin(2.0 * Math.PI * 1.5 * t) + 1.0) / 2.0
                    val currentFreq = 700.0 + (modulation * 900.0)

                    for (i in 0 until bufferSize) {
                        val sample = (sin(phase) * Short.MAX_VALUE * 0.95).toInt().toShort()
                        samples[i] = sample
                        phase += 2.0 * Math.PI * currentFreq * dt
                        if (phase > 2.0 * Math.PI) {
                            phase -= 2.0 * Math.PI
                        }
                        t += dt
                    }
                    track.write(samples, 0, bufferSize)
                }
            } catch (_: Exception) {
            } finally {
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        }
    }

    fun stopSiren() {
        isPlaying = false
        sirenJob?.cancel()
        sirenJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (_: Exception) {}
    }
}
