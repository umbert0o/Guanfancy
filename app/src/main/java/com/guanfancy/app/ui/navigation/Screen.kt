package com.guanfancy.app.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Warning : Screen

    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object Dashboard : Screen

    @Serializable
    data object Calendar : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object GuanfacineInfo : Screen

    @Serializable
    data class Feedback(val intakeId: Long) : Screen
}
