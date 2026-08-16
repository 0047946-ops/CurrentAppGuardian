package com.currentguardian.testing

data class TestEventTemplate(
    val id: String,
    val displayName: String,
    val description: String,
    val enabled: Boolean = true
)
