package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.data.db.entity.PromotionTemplateEntity
import com.lemon.mcdevmanagermp.domain.promotion.PromotionTemplate
import com.lemon.mcdevmanagermp.domain.promotion.PromotionTemplateRepository

class PromotionTemplateRepositoryImpl : PromotionTemplateRepository {

    companion object {
        val INSTANCE by lazy { PromotionTemplateRepositoryImpl() }
    }

    private val dao by lazy { AppContext.database.promotionTemplateDao() }

    override suspend fun getAll(): List<PromotionTemplate> =
        dao.getAll().map { it.toTemplate() }

    override suspend fun save(template: PromotionTemplate) {
        // 同名模板直接覆盖（不提示）：先删同名再插入
        dao.deleteByName(template.name)
        dao.upsert(template.toEntity())
    }

    override suspend fun delete(id: Long) = dao.delete(id)

    private fun PromotionTemplateEntity.toTemplate() = PromotionTemplate(
        id = id,
        name = name,
        extra = extra,
        activity = activity,
        feature = feature,
        update = updateContent,
        promoImageUrl = promoImageUrl,
        createdAt = createdAt
    )

    private fun PromotionTemplate.toEntity() = PromotionTemplateEntity(
        id = id,
        name = name,
        extra = extra,
        activity = activity,
        feature = feature,
        updateContent = update,
        promoImageUrl = promoImageUrl,
        createdAt = createdAt
    )
}
