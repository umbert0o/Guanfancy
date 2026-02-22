package com.guanfancy.app.domain.model

enum class MedicationType(val displayName: String, val description: String) {
    INTUNIV(
        displayName = "Intuniv",
        description = "Extended release - Max blood presence at ~6 hours"
    ),
    TENEX(
        displayName = "Tenex",
        description = "Immediate release - Max blood presence at ~3 hours"
    );

    fun getFoodZoneConfig(): FoodZoneConfig = when (this) {
        INTUNIV -> FoodZoneConfig.INTUNIV_DEFAULT
        TENEX -> FoodZoneConfig.TENEX_DEFAULT
    }

    companion object {
        val DEFAULT = INTUNIV

        fun fromString(value: String?): MedicationType = when (value) {
            TENEX.name -> TENEX
            else -> INTUNIV
        }
    }
}
