package com.lemon.mcdevmanagermp.ui.pages.work.workdetail

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.enums.PriceRankEnum
import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailDlcInfo
import com.lemon.mcdevmanagermp.domain.work.WorkDetailUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.utils.Logger
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.time.Clock
import kotlin.time.Instant

class WorkDetailViewModel :
    BaseViewModel<WorkDetailState, WorkDetailAction, WorkDetailEffect>(WorkDetailState()) {

    private val workDetailUseCase = WorkDetailUseCase(
        resourceRepository = ResourceRepositoryImpl.INSTANCE
    )

    override fun dispatch(action: WorkDetailAction) {
        when (action) {
            is WorkDetailAction.LoadDetail -> loadDetail(action.itemId)
            is WorkDetailAction.UpdateItemName -> setState { copy(itemName = action.value) }
            is WorkDetailAction.ToggleJoinShantou -> setState { copy(joinShantou = action.value) }
            is WorkDetailAction.ToggleOriginal -> setState { copy(isOriginal = action.value) }
            is WorkDetailAction.AddTag -> addTag(action.name)
            is WorkDetailAction.RemoveTag -> removeTag(action.index)
            is WorkDetailAction.UpdatePrerequisite -> setState { copy(prerequisite = action.value) }
            is WorkDetailAction.UpdateActivityDesc -> setState { copy(activityDesc = action.value) }
            is WorkDetailAction.ToggleRelatedMod -> setState { copy(isRelatedMod = action.value) }
            is WorkDetailAction.ToggleRelatedPackType -> setState { copy(relatedIsMaster = action.value) }
            is WorkDetailAction.UpdateRelatedSearch -> setState { copy(relatedSearchKey = action.value) }
            is WorkDetailAction.ToggleSyncPc -> setState { copy(syncPc = action.value) }

            // —— 授权信息 ——
            is WorkDetailAction.SelectCorpProof -> setState { copy(corpProofFile = action.file) }
            WorkDetailAction.RemoveCorpProof ->
                setState { copy(corpProofFile = null, corpProofImage = "") }

            // —— PC 基本信息 ——
            is WorkDetailAction.TogglePcIncludeMap -> setState { copy(pcIncludeMap = action.value) }
            is WorkDetailAction.AddPcTag -> addPcTag(action.name)
            is WorkDetailAction.RemovePcTag -> removePcTag(action.index)
            is WorkDetailAction.TogglePcPrerequisite -> setState { copy(pcHasPrerequisite = action.value) }
            is WorkDetailAction.UpdatePcPrerequisiteIid -> setState { copy(pcPrerequisiteIid = action.value) }
            is WorkDetailAction.UpdatePcIntro -> setState { copy(pcBrief = action.value) }

            // —— 定价 ——
            is WorkDetailAction.ChangePriceType -> changePriceType(action.type)
            is WorkDetailAction.ChangePriceRank -> changePriceRank(action.rank)
            is WorkDetailAction.UpdateEmeraldPrice -> setState { copy(emeraldPrice = action.value) }

            // —— 折扣 ——
            WorkDetailAction.AddDiscount -> addDiscount()
            is WorkDetailAction.RemoveDiscount -> removeDiscount(action.index)
            is WorkDetailAction.UpdateDiscountPercent -> updateDiscountPercent(
                action.index,
                action.percent
            )

            is WorkDetailAction.UpdateDiscountBegin -> updateDiscountBegin(
                action.index,
                action.date
            )

            is WorkDetailAction.UpdateDiscountEnd -> updateDiscountEnd(action.index, action.date)

            WorkDetailAction.Submit -> sendEffect(WorkDetailEffect.ShowToast("更新接口暂未接入"))
        }
    }

    private fun loadDetail(itemId: String) {
        if (state.value.isLoading) return
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            when (val result = workDetailUseCase.getResourceDetail(itemId)) {
                is NetworkState.Success -> {
                    val d = result.data
                    if (d != null) {
                        val priceType = PriceTypeEnum.fromStringType(d.priceType)
                        setState {
                            copy(
                                isLoading = false,
                                detail = d,
                                itemId = d.itemId,
                                normalNumber = d.normalNumber,
                                itemVersion = d.itemVersion,
                                itemName = d.itemName,
                                isOriginal = d.isOriginal,
                                tags = d.tags.map { it.name }.filter { it.isNotEmpty() },
                                // prerequisiteItems 为 List<JsonElement>，结构未知，本期留空由用户手动填写
                                prerequisite = "",
                                activityDesc = d.activityDesc,
                                isRelatedMod = d.dlcInfo.dlcSwitch,
                                relatedIsMaster = d.dlcInfo.dlcType == ResourceDetailDlcInfo.DlcType.MASTER.type,
                                syncPc = d.syncPcFlag,
                                // —— 授权信息 ——
                                corpProofImage = d.corpProofImage,
                                // —— PC 基本信息 ——
                                pcIncludeMap = d.syncItemInfo.includeMap,
                                pcBrief = d.syncItemInfo.brief,
                                pcPrerequisiteIid = d.relateItemId,
                                // —— 定价 ——
                                priceType = priceType,
                                priceRank = PriceRankEnum.fromIntType(d.priceRank),
                                emeraldPrice = if (priceType == PriceTypeEnum.EMERALD) d.price else 0,
                                discounts = parseDiscounts(d.discount)
                            )
                        }
                        // 加载默认标签（失败不影响详情展示）
                        loadItemTags()
                        // 加载 PC 模组可选标签（mc_consts.tag.comp；失败不影响详情展示）
                        loadPcTagOptions()
                    } else {
                        setState { copy(isLoading = false) }
                        sendEffect(WorkDetailEffect.ShowToast("获取详情失败"))
                    }
                }

                is NetworkState.Error -> {
                    setState { copy(isLoading = false) }
                    handleError(
                        result,
                        onNeedReLogin = { WorkDetailEffect.NeedReLogin },
                        onShowToast = { WorkDetailEffect.ShowToast(it) }
                    )
                }
            }
        }
    }

    /** 加载默认模组标签（搜索建议）；失败静默处理，不影响详情。 */
    private fun loadItemTags() {
        viewModelScope.launch {
            when (val result = workDetailUseCase.getItemTag()) {
                is NetworkState.Success -> result.data?.let { tagVo ->
                    setState { copy(availableTags = tagVo.tagList) }
                }

                is NetworkState.Error -> Unit
            }
        }
    }

    /** 加载 PC 模组可选标签（mc_consts.tag.comp）；失败静默处理，不影响详情。 */
    private fun loadPcTagOptions() {
        viewModelScope.launch {
            when (val result = workDetailUseCase.getMCConsts()) {
                is NetworkState.Success -> result.data?.let { consts ->
                    val options = consts.tag.comp
                    setState { copy(pcTagOptions = options) }
                }

                is NetworkState.Error -> Logger.d("PC标签选项: 加载失败 ${result.msg}")
            }
        }
    }

    // ===== 定价联动 =====

    // 是否允许编辑定价
    private fun canEditDiscount(type: PriceTypeEnum, rank: PriceRankEnum): Boolean {
        return type == PriceTypeEnum.DIAMOND && rank.type >= 1
    }

    // 切换定价类型
    private fun changePriceType(type: PriceTypeEnum) {
        setState {
            val newRank = when (type) {
                PriceTypeEnum.DIAMOND ->
                    if (priceRank.type in 0..6) priceRank else PriceRankEnum.DIAMOND_TIER_ONE

                PriceTypeEnum.EMERALD -> PriceRankEnum.EMERALD_TIER
                PriceTypeEnum.FREE -> PriceRankEnum.FREE_TIER
                PriceTypeEnum.UNKNOWN -> PriceRankEnum.UNKNOWN
            }
            val keepDiscounts = canEditDiscount(type, newRank)
            copy(
                priceType = type,
                priceRank = newRank,
                discounts = if (keepDiscounts) discounts else emptyList()
            )
        }
    }

    // 非钻石定价清空折扣
    private fun changePriceRank(rank: PriceRankEnum) {
        setState {
            val keepDiscounts = canEditDiscount(priceType, rank)
            copy(
                priceRank = rank,
                discounts = if (keepDiscounts) discounts else emptyList()
            )
        }
    }

    // 折扣增删改
    private fun addDiscount() {
        setState {
            if (!canEditDiscount(priceType, priceRank)) this
            else {
                val today = todayLocalDate()
                val weekLater = today.plus(7, DateTimeUnit.DAY)
                copy(
                    discounts = discounts + DiscountConfig(
                        percent = 90,
                        beginDate = today,
                        endDate = weekLater
                    )
                )
            }
        }
    }

    private fun removeDiscount(index: Int) {
        setState {
            if (index !in discounts.indices) this
            else copy(discounts = discounts.toMutableList().apply { removeAt(index) })
        }
    }

    private fun updateDiscountPercent(index: Int, percent: Int) {
        setState {
            if (index !in discounts.indices) this
            else copy(discounts = discounts.toMutableList().apply {
                this[index] = this[index].copy(percent = percent.coerceIn(60, 99))
            })
        }
    }

    private fun updateDiscountBegin(index: Int, date: LocalDate) {
        setState {
            if (index !in discounts.indices) this
            else copy(discounts = discounts.toMutableList().apply {
                this[index] = this[index].copy(beginDate = date)
            })
        }
    }

    private fun updateDiscountEnd(index: Int, date: LocalDate) {
        setState {
            if (index !in discounts.indices) this
            else copy(discounts = discounts.toMutableList().apply {
                this[index] = this[index].copy(endDate = date)
            })
        }
    }

    // ===== 折扣解析 =====

    /** 解析后端 discount 数组（List<JsonElement>，每项含 begin_at/end_at(Unix 秒)/discount）为 UI 模型。 */
    private fun parseDiscounts(elements: List<JsonElement>): List<DiscountConfig> {
        if (elements.isEmpty()) return emptyList()
        return elements.mapNotNull { el ->
            runCatching {
                val obj = el.jsonObject
                val beginSec = obj["begin_at"]?.jsonPrimitive?.longOrNull ?: return@runCatching null
                val endSec = obj["end_at"]?.jsonPrimitive?.longOrNull ?: return@runCatching null
                val percent = obj["discount"]?.jsonPrimitive?.intOrNull ?: 100
                val begin = Instant.fromEpochSeconds(beginSec).toLocalDateTime(APP_ZONE).date
                val end = Instant.fromEpochSeconds(endSec).toLocalDateTime(APP_ZONE).date
                DiscountConfig(percent = percent.coerceIn(60, 99), beginDate = begin, endDate = end)
            }.getOrNull()
        }
    }

    private fun addTag(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        setState {
            if (tags.contains(trimmed)) this else copy(tags = tags + trimmed)
        }
    }

    private fun removeTag(index: Int) {
        setState {
            if (index !in tags.indices) this
            else copy(tags = tags.toMutableList().apply { removeAt(index) })
        }
    }

    private fun addPcTag(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        setState {
            if (pcTags.contains(trimmed)) this else copy(pcTags = pcTags + trimmed)
        }
    }

    private fun removePcTag(index: Int) {
        setState {
            if (index !in pcTags.indices) this
            else copy(pcTags = pcTags.toMutableList().apply { removeAt(index) })
        }
    }
}

/** 应用时区（与 utils/extension/FunExt 的 timeZoneCN 保持一致：Asia/Shanghai）。 */
private val APP_ZONE: TimeZone = TimeZone.of("Asia/Shanghai")

/** 当前本地日期（Asia/Shanghai 时区）。 */
private fun todayLocalDate(): LocalDate {
    val nowMillis = Clock.System.now().toEpochMilliseconds()
    return Instant.fromEpochMilliseconds(nowMillis).toLocalDateTime(APP_ZONE).date
}
