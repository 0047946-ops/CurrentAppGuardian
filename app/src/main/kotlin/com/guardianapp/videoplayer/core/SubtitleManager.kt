package com.guardianapp.videoplayer.core

import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SubtitleState(
    val isSubtitleEnabled: Boolean = false,
    val subtitleLanguage: String = "en",
    val subtitleSize: Float = 14f
)

@UnstableApi
class SubtitleManager(private val player: ExoPlayer, private val trackSelector: DefaultTrackSelector) {
    private val _subtitleState = MutableStateFlow(SubtitleState())
    val subtitleState: StateFlow<SubtitleState> = _subtitleState.asStateFlow()

    fun enableSubtitles(language: String = "en") {
        _subtitleState.value = SubtitleState(
            isSubtitleEnabled = true,
            subtitleLanguage = language
        )
        applySubtitleSettings()
    }

    fun disableSubtitles() {
        _subtitleState.value = SubtitleState(isSubtitleEnabled = false)
        applySubtitleSettings()
    }

    fun setSubtitleSize(size: Float) {
        _subtitleState.value = _subtitleState.value.copy(subtitleSize = size)
        applySubtitleSettings()
    }

    fun setSubtitleLanguage(language: String) {
        _subtitleState.value = _subtitleState.value.copy(subtitleLanguage = language)
        applySubtitleSettings()
    }

    private fun applySubtitleSettings() {
        val state = _subtitleState.value
        if (state.isSubtitleEnabled) {
            trackSelector.setParameters(
                trackSelector.buildUponParameters()
                    .setPreferredTextLanguage(state.subtitleLanguage)
                    .setPreferredTextRoleFlags(C.ROLE_FLAG_CAPTION)
                    .build()
            )
        } else {
            trackSelector.setParameters(
                trackSelector.buildUponParameters()
                    .clearSelectionOverrides()
                    .build()
            )
        }
    }

    fun getAvailableLanguages(): List<String> {
        return listOf("en", "zh", "ja", "ko", "es", "fr", "de", "pt", "ru", "ar")
    }
}
