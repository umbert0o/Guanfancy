package com.guanfancy.app.domain.model

data class FoodZoneConfig(
    val greenHoursBefore: Int,
    val yellowHoursBefore: Int,
    val redHoursAfter: Int,
    val yellowHoursAfter: Int
) {
    companion object {
        val INTUNIV_DEFAULT = FoodZoneConfig(
            greenHoursBefore = 5,
            yellowHoursBefore = 3,
            redHoursAfter = 3,
            yellowHoursAfter = 5
        )

        val TENEX_DEFAULT = FoodZoneConfig(
            greenHoursBefore = 5,
            yellowHoursBefore = 3,
            redHoursAfter = 1,
            yellowHoursAfter = 2
        )

        val DEFAULT = INTUNIV_DEFAULT
    }
}
