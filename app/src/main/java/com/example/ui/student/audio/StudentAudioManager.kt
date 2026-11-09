package com.example.ui.student.audio

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.SystemClock
import android.util.Log
import com.example.core.audio.PcmSoundSynthesizer
import com.example.core.audio.SoundEffect
import com.example.ui.student.audio.music.HomeMusicPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * Centralized Lightweight Audio Manager for the TL Brain Test Student User Panel.
 * Brand Owner: PRITAM CHOUDHARY.
 *
 * Implements:
 * - STEP 1: Centralized Audio Architecture with clean reusable functions:
 *   playClick(), playSelect(), playBack(), playOpen(), playClose(),
 *   playSuccess(), playError(), playNotification(), playGameStart(), playGameComplete().
 * - STEP 2: Strict click integration without audio spam on passive elements or continuous typing.
 * - STEP 3: Anti-spam debounce protection (80ms minimum gap) preventing double-triggering
 *   and distortion on rapid taps.
 * - STEP 4: Independent SFX and Background Music toggles and volume sliders persisted in SharedPreferences.
 * - STEP 5: Home background music engine with automatic Game Audio Priority arbitration.
 * - STEP 6 & 7: 100% Free-Tier, CC0 Public Domain procedural audio synthesis. Zero cloud cost.
 */
object StudentAudioManager {

    private const val TAG = "StudentAudioManager"
    private const val PREFS_NAME = "tl_student_audio_prefs"

    private const val KEY_SFX_ENABLED = "student_sfx_enabled"
    private const val KEY_SFX_VOLUME = "student_sfx_volume"
    private const val KEY_MUSIC_ENABLED = "student_music_enabled"
    private const val KEY_MUSIC_VOLUME = "student_music_volume"

    // Debounce threshold for rapid clicks to prevent sound spam and distortion
    private const val MIN_CLICK_DEBOUNCE_MS = 80L

    private var appContext: Context? = null
    private var prefs: SharedPreferences? = null

    private var soundPool: SoundPool? = null
    private val soundIdMap = ConcurrentHashMap<SoundEffect, Int>()
    private val loadedSoundSet = ConcurrentHashMap.newKeySet<Int>()

    // Home Background Music Engine
    private var homeMusicPlayer: HomeMusicPlayer? = null

    // Reactive StateFlows for Jetpack Compose UI reactivity
    private val _sfxEnabledFlow = MutableStateFlow(true)
    val sfxEnabledFlow: StateFlow<Boolean> = _sfxEnabledFlow.asStateFlow()

    private val _sfxVolumeFlow = MutableStateFlow(0.85f)
    val sfxVolumeFlow: StateFlow<Float> = _sfxVolumeFlow.asStateFlow()

    private val _musicEnabledFlow = MutableStateFlow(true)
    val musicEnabledFlow: StateFlow<Boolean> = _musicEnabledFlow.asStateFlow()

    private val _musicVolumeFlow = MutableStateFlow(0.50f)
    val musicVolumeFlow: StateFlow<Float> = _musicVolumeFlow.asStateFlow()

    private var lastClickTimeElapsedRealtime: Long = 0L
    private var isInitialized = false
    private val initLock = Any()

    /**
     * Initializes the Student User Panel Audio Manager with the Application Context.
     */
    fun init(context: Context) {
        if (isInitialized) return
        synchronized(initLock) {
            if (isInitialized) return
            try {
                val app = context.applicationContext
                appContext = app
                val sp = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs = sp

                val savedSfxEnabled = sp.getBoolean(KEY_SFX_ENABLED, true)
                val savedSfxVolume = sp.getFloat(KEY_SFX_VOLUME, 0.85f).coerceIn(0f, 1f)
                val savedMusicEnabled = sp.getBoolean(KEY_MUSIC_ENABLED, true)
                val savedMusicVolume = sp.getFloat(KEY_MUSIC_VOLUME, 0.50f).coerceIn(0f, 1f)

                _sfxEnabledFlow.value = savedSfxEnabled
                _sfxVolumeFlow.value = savedSfxVolume
                _musicEnabledFlow.value = savedMusicEnabled
                _musicVolumeFlow.value = savedMusicVolume

                setupSoundPool(app)

                val musicPlayer = HomeMusicPlayer(app)
                musicPlayer.setMusicEnabled(savedMusicEnabled)
                musicPlayer.setMusicVolume(savedMusicVolume)
                homeMusicPlayer = musicPlayer

                isInitialized = true
            } catch (t: Throwable) {
                Log.w(TAG, "Student audio subsystem initialization fallback: ${t.message}")
            }
        }
    }

    private fun setupSoundPool(context: Context) {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(audioAttributes)
                .build()

            pool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    loadedSoundSet.add(sampleId)
                }
            }

            soundPool = pool

            // Preload sounds asynchronously on IO dispatcher
            CoroutineScope(Dispatchers.IO).launch {
                preloadCoreSounds(context, pool)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "SoundPool unavailable: ${t.message}")
        }
    }

    private fun preloadCoreSounds(context: Context, pool: SoundPool) {
        try {
            val soundDir = File(context.cacheDir, "tl_brain_audio_sfx").apply { mkdirs() }

            SoundEffect.entries.forEach { effect ->
                try {
                    val soundFile = File(soundDir, "${effect.id}.wav")
                    if (!soundFile.exists() || soundFile.length() < 44) {
                        val wavBytes = PcmSoundSynthesizer.synthesizeWav(effect)
                        FileOutputStream(soundFile).use { it.write(wavBytes) }
                    }
                    val soundId = pool.load(soundFile.absolutePath, 1)
                    if (soundId > 0) {
                        soundIdMap[effect] = soundId
                    }
                } catch (e: Exception) {
                    Log.v(TAG, "Failed caching sound: ${effect.id} (${e.message})")
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Preload audio notice: ${t.message}")
        }
    }

    // =========================================================================
    // STEP 1 REUSABLE HIGH-LEVEL FUNCTIONS
    // =========================================================================

    /**
     * Crisp, subtle tactile click for buttons, dialog actions, and main menu items.
     * Enforces minimum 80ms debounce interval to prevent spamming on rapid taps.
     */
    fun playClick() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTimeElapsedRealtime < MIN_CLICK_DEBOUNCE_MS) return
        lastClickTimeElapsedRealtime = now
        playInternal(SoundEffect.CLICK, volumeMultiplier = 0.9f)
    }

    /**
     * Light selection tap for tabs, segmented controls, chips, and radio options.
     */
    fun playSelect() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTimeElapsedRealtime < MIN_CLICK_DEBOUNCE_MS) return
        lastClickTimeElapsedRealtime = now
        playInternal(SoundEffect.SELECT, volumeMultiplier = 0.85f)
    }

    /**
     * Soft descending back navigation tap.
     */
    fun playBack() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTimeElapsedRealtime < MIN_CLICK_DEBOUNCE_MS) return
        lastClickTimeElapsedRealtime = now
        playInternal(SoundEffect.MENU_CLOSE, volumeMultiplier = 0.8f)
    }

    /**
     * Warm ascending pop/chime when opening popups, dialogs, bottom sheets, or menus.
     */
    fun playOpen() {
        playInternal(SoundEffect.POPUP_OPEN, volumeMultiplier = 0.95f)
    }

    /**
     * Subtle pop when closing popups, dialogs, or menus.
     */
    fun playClose() {
        playInternal(SoundEffect.POPUP_CLOSE, volumeMultiplier = 0.85f)
    }

    /**
     * Bright confirmation chime on completing an action, saving profile, or passing quiz.
     */
    fun playSuccess() {
        playInternal(SoundEffect.SUCCESS, volumeMultiplier = 1.0f)
    }

    /**
     * Muted low warning buzz indicating an error or invalid input.
     */
    fun playError() {
        playInternal(SoundEffect.ERROR, volumeMultiplier = 0.95f)
    }

    /**
     * Crystal two-tone notification alert.
     */
    fun playNotification() {
        playInternal(SoundEffect.NOTIFICATION, volumeMultiplier = 0.95f)
    }

    /**
     * Energizing intro chime for game start.
     * Automatically coordinates game audio priority by pausing Home background music!
     */
    fun playGameStart() {
        homeMusicPlayer?.pauseForGame()
        playInternal(SoundEffect.GAME_START, volumeMultiplier = 1.0f)
    }

    /**
     * Celebratory flourish when completing a game session.
     */
    fun playGameComplete() {
        playInternal(SoundEffect.GAME_COMPLETED, volumeMultiplier = 1.0f)
    }

    // =========================================================================
    // STEP 5: HOME MUSIC & GAME AUDIO PRIORITY
    // =========================================================================

    /**
     * Starts or resumes Home background music if enabled by the user.
     */
    fun startHomeMusic() {
        homeMusicPlayer?.startHomeMusic()
    }

    /**
     * Pauses Home background music immediately when entering a game or quiz.
     * Releases Home music audio focus and grants exclusive priority to game audio.
     */
    fun onGameEntered() {
        homeMusicPlayer?.pauseForGame()
    }

    /**
     * Restores Home background music upon exiting a game, ONLY if enabled by the user.
     */
    fun onGameExited() {
        homeMusicPlayer?.resumeAfterGame()
    }

    /**
     * Stops Home background music completely.
     */
    fun stopHomeMusic() {
        homeMusicPlayer?.stopHomeMusic()
    }

    // =========================================================================
    // STEP 4: USER SETTINGS (SFX & MUSIC CONTROLS)
    // =========================================================================

    fun setSfxEnabled(enabled: Boolean) {
        _sfxEnabledFlow.value = enabled
        prefs?.edit()?.putBoolean(KEY_SFX_ENABLED, enabled)?.apply()
    }

    fun isSfxEnabled(): Boolean = _sfxEnabledFlow.value

    fun setSfxVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _sfxVolumeFlow.value = clamped
        prefs?.edit()?.putFloat(KEY_SFX_VOLUME, clamped)?.apply()
    }

    fun getSfxVolume(): Float = _sfxVolumeFlow.value

    fun setMusicEnabled(enabled: Boolean) {
        _musicEnabledFlow.value = enabled
        prefs?.edit()?.putBoolean(KEY_MUSIC_ENABLED, enabled)?.apply()
        homeMusicPlayer?.setMusicEnabled(enabled)
    }

    fun isMusicEnabled(): Boolean = _musicEnabledFlow.value

    fun setMusicVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _musicVolumeFlow.value = clamped
        prefs?.edit()?.putFloat(KEY_MUSIC_VOLUME, clamped)?.apply()
        homeMusicPlayer?.setMusicVolume(clamped)
    }

    fun getMusicVolume(): Float = _musicVolumeFlow.value

    // =========================================================================
    // INTERNAL PLAYBACK DISPATCHER
    // =========================================================================

    fun play(effect: SoundEffect, volumeMultiplier: Float = 1.0f) {
        playInternal(effect, volumeMultiplier)
    }

    private fun playInternal(effect: SoundEffect, volumeMultiplier: Float = 1.0f) {
        if (!_sfxEnabledFlow.value) return
        val masterVol = _sfxVolumeFlow.value
        if (masterVol <= 0.001f) return

        val pool = soundPool ?: return
        val soundId = soundIdMap[effect] ?: return

        try {
            val vol = (masterVol * volumeMultiplier).coerceIn(0.0f, 1.0f)
            val priority = when (effect) {
                SoundEffect.CORRECT, SoundEffect.WRONG, SoundEffect.QUIZ_COMPLETE,
                SoundEffect.HIGH_SCORE, SoundEffect.LEVEL_UP, SoundEffect.GAME_OVER -> 2
                else -> 1
            }
            pool.play(soundId, vol, vol, priority, 0, 1.0f)
        } catch (t: Throwable) {
            Log.v(TAG, "Audio playback fallback for ${effect.id}: ${t.message}")
        }
    }

    /**
     * Releases all audio resources on shutdown.
     */
    fun release() {
        try {
            homeMusicPlayer?.release()
            homeMusicPlayer = null

            soundPool?.release()
            soundPool = null
            soundIdMap.clear()
            loadedSoundSet.clear()
            isInitialized = false
        } catch (t: Throwable) {
            Log.w(TAG, "Audio release notice: ${t.message}")
        }
    }
}
