package com.example.ui.student.audio.games

import com.example.core.audio.SoundEffect
import com.example.ui.student.audio.StudentAudioManager

/**
 * Brain Games Arena audio domain helper for the TL Brain Test Student User Panel.
 * Handles game start, move feedbacks, combo matches, countdowns, and game completion.
 *
 * STEP 5 IMPLEMENTATION:
 * When a game arena is entered, Home Music is immediately paused, releasing audio focus
 * so that game sounds take sole priority. When exiting, Home Music is restored if enabled.
 */
object GameAudio {

    fun onGameArenaEntered() {
        StudentAudioManager.onGameEntered()
    }

    fun onGameArenaExited() {
        StudentAudioManager.onGameExited()
    }

    fun playStart() {
        StudentAudioManager.playGameStart()
    }

    fun playClick() {
        StudentAudioManager.playClick()
    }

    fun playSelect() {
        StudentAudioManager.playSelect()
    }

    fun playCorrectMove() {
        StudentAudioManager.play(SoundEffect.GAME_CORRECT_MOVE, 0.95f)
    }

    fun playWrongMove() {
        StudentAudioManager.play(SoundEffect.GAME_WRONG_MOVE, 0.9f)
    }

    fun playMatchFound() {
        StudentAudioManager.play(SoundEffect.MATCH_FOUND, 1.0f)
    }

    fun playMatchCompleted() {
        StudentAudioManager.play(SoundEffect.MATCH_COMPLETED, 1.0f)
    }

    fun playCountdownTick() {
        StudentAudioManager.play(SoundEffect.COUNTDOWN_TICK, 0.75f)
    }

    fun playGameOver() {
        StudentAudioManager.play(SoundEffect.GAME_OVER, 0.95f)
    }

    fun playNewRecord() {
        StudentAudioManager.play(SoundEffect.NEW_RECORD, 1.0f)
    }

    fun playComplete() {
        StudentAudioManager.playGameComplete()
    }

    fun playPointsEarned() {
        StudentAudioManager.play(SoundEffect.POINTS_EARNED, 0.95f)
    }
}
