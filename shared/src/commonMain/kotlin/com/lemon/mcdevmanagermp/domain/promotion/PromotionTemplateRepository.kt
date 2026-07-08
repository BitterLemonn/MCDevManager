package com.lemon.mcdevmanagermp.domain.promotion

interface PromotionTemplateRepository {
    suspend fun getAll(): List<PromotionTemplate>
    suspend fun save(template: PromotionTemplate)
    suspend fun delete(id: Long)
}
