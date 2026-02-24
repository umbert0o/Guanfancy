package com.guanfancy.app.domain.service

import com.guanfancy.app.domain.model.FoodZone
import kotlinx.coroutines.flow.Flow

interface FoodZoneService {
    fun getCurrentZone(): Flow<FoodZone>
}
