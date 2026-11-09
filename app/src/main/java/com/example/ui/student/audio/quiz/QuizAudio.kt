package com.example.ui.student.audio.quiz

import com.example.core.audio.SoundEffect
import com.example.ui.student.audio.StudentAudioManager

/**
 * Quiz Audio domain helper for the TL Brain Test Student User Panel.
 * Manages question pacing, option selection, correct/wrong feedback, and victory scoring audio.
 */
object QuizAudio {

    fun playStart() {
        StudentAudioManager.onGameEntered() // Pause background music during active quiz questions
        StudentAudioManager.play(SoundEffect.QUIZ_START, 1.0f)
    }

    fun playQuestionAppear() {
        StudentAudioManager.play(SoundEffect.QUESTION_APPEAR, 0.85f)
    }

    fun playOptionSelect() {
        StudentAudioManager.playClick()
    }

    fun playCorrect() {
        StudentAudioManager.play(SoundEffect.CORRECT, 1.0f)
    }

    fun playWrong() {
        StudentAudioManager.play(SoundEffect.WRONG, 0.9f)
    }

    fun playTimerWarn() {
        StudentAudioManager.play(SoundEffect.TIMER_WARN, 0.8f)
    }

    fun playTimerEnd() {
        StudentAudioManager.play(SoundEffect.TIMER_END, 0.9f)
    }

    fun playNextQuestion() {
        StudentAudioManager.play(SoundEffect.NEXT_QUESTION, 0.85f)
    }

    fun playQuizComplete() {
        StudentAudioManager.play(SoundEffect.QUIZ_COMPLETE, 1.0f)
    }

    fun playHighScore() {
        StudentAudioManager.play(SoundEffect.HIGH_SCORE, 1.0f)
    }

    fun playXpEarned() {
        StudentAudioManager.play(SoundEffect.XP_EARNED, 0.95f)
    }

    fun onQuizExited() {
        StudentAudioManager.onGameExited() // Restore home music if user enabled it
    }
}
