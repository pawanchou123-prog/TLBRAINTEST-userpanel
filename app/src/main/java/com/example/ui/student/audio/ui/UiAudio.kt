package com.example.ui.student.audio.ui

import com.example.core.audio.SoundEffect
import com.example.ui.student.audio.StudentAudioManager

/**
 * UI Audio domain helper for the TL Brain Test Student User Panel.
 * Provides clean, specialized audio functions for buttons, tabs, navigation, dialogs, and alerts.
 */
object UiAudio {

    fun playClick() {
        StudentAudioManager.playClick()
    }

    fun playSelect() {
        StudentAudioManager.playSelect()
    }

    fun playBack() {
        StudentAudioManager.playBack()
    }

    fun playOpen() {
        StudentAudioManager.playOpen()
    }

    fun playClose() {
        StudentAudioManager.playClose()
    }

    fun playTabChange() {
        StudentAudioManager.play(SoundEffect.TAB_CHANGE, 0.85f)
    }

    fun playToggle(checked: Boolean) {
        StudentAudioManager.play(if (checked) SoundEffect.TOGGLE_ON else SoundEffect.TOGGLE_OFF, 0.85f)
    }

    fun playSuccess() {
        StudentAudioManager.playSuccess()
    }

    fun playError() {
        StudentAudioManager.playError()
    }

    fun playNotification() {
        StudentAudioManager.playNotification()
    }

    fun playAvatarSelect() {
        StudentAudioManager.play(SoundEffect.AVATAR_SELECT, 0.9f)
    }

    fun playSettingsSaved() {
        StudentAudioManager.play(SoundEffect.SETTINGS_SAVED, 0.95f)
    }

    fun playChatSent() {
        StudentAudioManager.play(SoundEffect.CHAT_SENT, 0.9f)
    }

    fun playSubscriptionAction() {
        StudentAudioManager.play(SoundEffect.REQUEST_SUBMITTED, 0.95f)
    }
}
