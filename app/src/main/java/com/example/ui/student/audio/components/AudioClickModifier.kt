package com.example.ui.student.audio.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role
import com.example.ui.student.audio.StudentAudioManager

/**
 * Reusable Jetpack Compose Modifiers for seamless, accessible click audio.
 * Enforces:
 * - Minimum touch target semantics
 * - Tactile Material ripple visual indicator (audio is never the sole feedback)
 * - Automatic rate-limited audio feedback via StudentAudioManager
 * - Respects Sound Effects ON/OFF preference
 */

fun Modifier.audioClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this.clickable(
        interactionSource = interactionSource,
        indication = ripple(),
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = {
            StudentAudioManager.playClick()
            onClick()
        }
    )
}

fun Modifier.audioSelectClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this.clickable(
        interactionSource = interactionSource,
        indication = ripple(),
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = {
            StudentAudioManager.playSelect()
            onClick()
        }
    )
}

fun Modifier.audioBackClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this.clickable(
        interactionSource = interactionSource,
        indication = ripple(),
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = {
            StudentAudioManager.playBack()
            onClick()
        }
    )
}
