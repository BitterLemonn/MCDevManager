package com.lemon.mcdevmanagermp.ui.pages.work.workdetail

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.enums.PriceRankEnum
import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum
import com.lemon.mcdevmanagermp.data.repository.FileUploadRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsCommonTitleData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailDlcInfo
import com.lemon.mcdevmanagermp.domain.upload.FileUploadRepository
import com.lemon.mcdevmanagermp.domain.work.WorkDetailUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.ui.components.ModSelectOption
import com.lemon.mcdevmanagermp.utils.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
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

    private val fileUploadRepository: FileUploadRepository = FileUploadRepositoryImpl.INSTANCE

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
            is WorkDetailAction.UpdatePcIntro -> setState { copy(pcBrief = action.value) }
            is WorkDetailAction.UpdatePeDetail -> setState { copy(peDetail = action.value) }
            is WorkDetailAction.UpdatePeUpdateSummary -> setState { copy(peUpdateSummary = action.value) }
            is WorkDetailAction.UpdatePcDetail -> setState { copy(pcDetail = action.value) }

            // —— PE 资源管理 ——
            is WorkDetailAction.UpdatePeResourceType -> setState {
                // 切换资源类别时清空具体类别与次级分类：旧值不属于新类别，避免脏数据
                copy(peResourceType = action.id, peResourceSubType = 0, peResourceModSecondType = 0)
            }

            is WorkDetailAction.UpdatePeResourceSubType -> setState { copy(peResourceSubType = action.id) }
            is WorkDetailAction.UpdatePeResourceModSecondType -> setState {
                copy(
                    peResourceModSecondType = action.id
                )
            }

            is WorkDetailAction.TogglePeRecommendTag -> togglePeRecommendTag(action.id)
            is WorkDetailAction.TogglePePlayPlan -> setState { copy(peAddPlayPlan = action.value) }
            is WorkDetailAction.TogglePeMountCall -> setState { copy(peMountCallEnabled = action.value) }
            is WorkDetailAction.TogglePeAddVersion -> setState { copy(peAddVersion = action.value) }
            is WorkDetailAction.UploadPeZip -> uploadPeZip(action.file)
            WorkDetailAction.RemovePeResource -> clearPeResource()

            // —— 上架设置（弱下架） ——
            is WorkDetailAction.TogglePeWeakOffline -> setState { copy(peWeakOffline = action.value) }
            is WorkDetailAction.UpdatePeWeakOfflineReason -> setState { copy(peWeakOfflineReason = action.value) }
            is WorkDetailAction.TogglePcWeakOffline -> setState { copy(pcWeakOffline = action.value) }
            is WorkDetailAction.UpdatePcWeakOfflineReason -> setState { copy(pcWeakOfflineReason = action.value) }

            // —— 模组搜索选择 ——
            is WorkDetailAction.SearchRelatedMods -> searchRelatedMods(action.query)
            is WorkDetailAction.SelectRelatedMod -> selectRelatedMod(action.option)
            WorkDetailAction.ClearRelatedMod -> clearRelatedMod()
            is WorkDetailAction.SearchPcPrereqMods -> searchPcPrereqMods(action.query)
            is WorkDetailAction.SelectPcPrereqMod -> selectPcPrereqMod(action.option)
            is WorkDetailAction.RemovePcPrereqMod -> removePcPrereqMod(action.option)

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
                                // —— PC 前置模组 ——
                                pcHasPrerequisite = d.syncItemInfo.requirement.isNotEmpty(),
                                pcPrerequisites = d.syncItemInfo.requirement.map {
                                    ModSelectOption(it.itemId, it.itemName)
                                },
                                // —— PE 详情信息 ——
                                peDetail = d.info,
                                peUpdateSummary = d.updateSummary,
                                // —— PC 详细信息 ——
                                pcDetail = d.syncItemInfo.info,
                                // —— PE 资源管理 ——
                                peResourceType = d.priType,
                                peResourceSubType = d.subType,
                                peResourceModSecondType = d.modSecondType,
                                peRecommendTags = d.labelTypeList,
                                peAddPlayPlan = d.peIsAddPlayPlan,
                                peMountCallEnabled = d.mountCallEnabled,
                                peResource = d.res.firstOrNull()?.let { res ->
                                    PeResourceFile(
                                        name = res.resName,
                                        url = res.resUrl,
                                        mcVersion = res.mcVersion,
                                        size = res.resInfo.resSize.toLong(),
                                        addVersion = res.addVersion
                                    )
                                },
                                peAddVersion = d.res.firstOrNull()?.addVersion ?: false,
                                // —— 上架设置（弱下架） ——
                                peWeakOffline = d.weakOffline,
                                peWeakOfflineReason = d.weakOfflineReason,
                                pcWeakOffline = d.syncItemInfo.weakOffline,
                                pcWeakOfflineReason = d.syncItemInfo.weakOfflineReason,
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

    /** 加载 PC 模组可选标签（mc_consts.tag.comp）并回显 PC 模组标签（syncItemInfo.tag 的 id → title）；失败静默。 */
    private fun loadPcTagOptions() {
        viewModelScope.launch {
            when (val result = workDetailUseCase.getMCConsts()) {
                is NetworkState.Success -> result.data?.let { consts ->
                    val options = consts.tag.comp
                    // 回显 PC 模组标签：syncItemInfo.tag(List<Int> id) 用 options 映射为 title
                    val tagTitles = (state.value.detail?.syncItemInfo?.tag ?: emptyList())
                        .mapNotNull { id -> options.firstOrNull { it.id == id }?.title }
                    setState {
                        copy(
                            pcTagOptions = options,
                            pcTags = tagTitles,
                            peResourceTypeOptions = consts.priType.pe,
                            peRecommendTagOptions = consts.labelType,
                            peRecommendTagLimit = consts.itemTagLimit,
                            pePriTypeFileTypes = parsePriTypeFileTypes(consts.subType.pe),
                            peResourceSubTypeOptions = parsePriTypeSubTypeOptions(consts.subType.pe),
                            peModSecondTypeOptions = consts.modSecondType.subTag
                        )
                    }
                }

                is NetworkState.Error -> Logger.d("PC标签选项: 加载失败 ${result.msg}")
            }
        }
    }

    // ===== 模组搜索选择（关联模组 pe / PC 前置 comp） =====

    /** 关联模组搜索（pe，mcStatus=1）；空 query 清空结果不请求。 */
    private fun searchRelatedMods(query: String) {
        val q = query.trim()
        if (q.isEmpty()) {
            setState { copy(relatedSearchResults = emptyList(), isSearchingRelated = false) }
            return
        }
        viewModelScope.launch {
            setState { copy(isSearchingRelated = true) }
            when (val result = workDetailUseCase.getResourceList("pe", q, 1)) {
                is NetworkState.Success -> setState {
                    copy(
                        isSearchingRelated = false,
                        relatedSearchResults = (result.data ?: emptyList())
                            .map { ModSelectOption(it.itemId, it.itemName) }
                    )
                }

                is NetworkState.Error -> setState { copy(isSearchingRelated = false) }
            }
        }
    }

    private fun selectRelatedMod(option: ModSelectOption) {
        setState {
            copy(
                relatedItemId = option.id,
                relatedItemName = option.name,
                relatedSearchResults = emptyList(),
                isSearchingRelated = false
            )
        }
    }

    private fun clearRelatedMod() {
        setState { copy(relatedItemId = "", relatedItemName = "") }
    }

    /** PC 前置模组搜索（comp requirements 接口，按名称搜索可作前置的模组）；空 query 清空结果不请求。 */
    private fun searchPcPrereqMods(query: String) {
        val q = query.trim()
        if (q.isEmpty()) {
            setState { copy(pcPrereqSearchResults = emptyList(), isSearchingPcPrereq = false) }
            return
        }
        viewModelScope.launch {
            setState { copy(isSearchingPcPrereq = true) }
            when (val result = workDetailUseCase.getCompRequirements(q)) {
                is NetworkState.Success -> setState {
                    copy(
                        isSearchingPcPrereq = false,
                        pcPrereqSearchResults = (result.data ?: emptyList())
                            .map { ModSelectOption(it.itemId, it.itemName) }
                    )
                }

                is NetworkState.Error -> setState { copy(isSearchingPcPrereq = false) }
            }
        }
    }

    private fun selectPcPrereqMod(option: ModSelectOption) {
        setState {
            // 多选：去重追加
            if (pcPrerequisites.any { it.id == option.id }) this
            else copy(pcPrerequisites = pcPrerequisites + option)
        }
    }

    private fun removePcPrereqMod(option: ModSelectOption) {
        setState {
            copy(pcPrerequisites = pcPrerequisites.filterNot { it.id == option.id })
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

    // ===== PE 资源管理 =====

    private fun togglePeRecommendTag(id: Int) {
        val current = state.value
        val limit = current.peRecommendTagLimit
        // 合计上限：玩法+主题共享 item_tag_limit；已达上限且为新增时拒绝并提示（limit<=0 视为不限制）
        val willAdd = id !in current.peRecommendTags
        if (willAdd && limit > 0 && current.peRecommendTags.size >= limit) {
            sendEffect(WorkDetailEffect.ShowToast("推荐标签最多选 $limit 个"))
            return
        }
        setState {
            copy(
                peRecommendTags = if (id in peRecommendTags) {
                    peRecommendTags.filterNot { it == id }
                } else {
                    peRecommendTags + id
                }
            )
        }
    }

    private fun clearPeResource() {
        setState { copy(peResource = null) }
    }

    private fun uploadPeZip(file: PlatformFile) {
        if (state.value.isUploadingPeZip) return
        // 选定资源类别后，按其 sub_type.file_type 限定上传文件类型（consts 无该类别分组/未声明 file_type 时不限）
        val accepted = state.value.pePriTypeFileTypes[state.value.peResourceType]
        val fileType = inferFileType(file.name)
        if (fileType != null && !accepted.isNullOrEmpty() && fileType !in accepted) {
            sendEffect(WorkDetailEffect.ShowToast("该资源类别仅支持 ${accepted.joinToString("/")} 文件"))
            return
        }
        viewModelScope.launch {
            setState { copy(isUploadingPeZip = true) }
            // ponytail: fileType 待按网易 FP 模组文件类型实测微调（现有接口仅用过 image/video）
            val result = fileUploadRepository.uploadFile(
                fileType = "mod",
                fileName = file.name,
                file = file,
                mimeType = "application/zip"
            )
            when (result) {
                is NetworkState.Success -> {
                    val url = parseUploadUrl(result.data?.body.orEmpty())
                    setState {
                        copy(
                            isUploadingPeZip = false,
                            peResource = PeResourceFile(
                                name = file.name,
                                url = url,
                                addVersion = peAddVersion
                            )
                        )
                    }
                    if (url.isEmpty()) {
                        sendEffect(WorkDetailEffect.ShowToast("上传成功但未能解析资源地址"))
                    }
                }

                is NetworkState.Error -> {
                    setState { copy(isUploadingPeZip = false) }
                    sendEffect(WorkDetailEffect.ShowToast(result.msg))
                }
            }
        }
    }

    /** best-effort 解析网易 FP 上传响应（textarea 内 JSON）中的资源地址；结构需实测，用正则取常见字段。 */
    private fun parseUploadUrl(body: String): String {
        if (body.isEmpty()) return ""
        return Regex(""""url"\s*:\s*"([^"]+)"""").find(body)?.groupValues?.get(1)
            ?: Regex(""""filename"\s*:\s*"([^"]+)"""").find(body)?.groupValues?.get(1)
            ?: ""
    }
}

/** 解析 mc_consts.sub_type.pe → {pri_type id → 接受的 file_type 集合}；空集表示该类别子类型未声明 file_type，视为不限。 */
internal fun parsePriTypeFileTypes(peSubType: JsonObject): Map<Int, Set<String>> {
    val result = mutableMapOf<Int, MutableSet<String>>()
    for ((priTypeKey, childrenEl) in peSubType) {
        val priType = priTypeKey.toIntOrNull() ?: continue
        val accepted = mutableSetOf<String>()
        childrenEl.jsonArray.forEach { el ->
            val ft = el.jsonObject["file_type"]?.jsonPrimitive?.content
            if (!ft.isNullOrEmpty()) accepted.add(ft)
        }
        result[priType] = accepted
    }
    return result
}

/** 解析 mc_consts.sub_type.pe → {pri_type id → 具体类别选项(id+title)}；仅保留有子类别的 priType。 */
internal fun parsePriTypeSubTypeOptions(peSubType: JsonObject): Map<Int, List<MCConstsCommonTitleData>> {
    val result = mutableMapOf<Int, List<MCConstsCommonTitleData>>()
    for ((priTypeKey, childrenEl) in peSubType) {
        val priType = priTypeKey.toIntOrNull() ?: continue
        val options = childrenEl.jsonArray.mapNotNull { el ->
            val obj = el.jsonObject
            val id = obj["id"]?.jsonPrimitive?.intOrNull ?: return@mapNotNull null
            MCConstsCommonTitleData(id, obj["title"]?.jsonPrimitive?.content ?: "")
        }
        if (options.isNotEmpty()) result[priType] = options
    }
    return result
}

/** 由文件名扩展名推断网易 file_type：.png→png，.zip/.mcworld/.mcpack/.mcaddon→zip，其余 null（未知，不过滤）。 */
internal fun inferFileType(fileName: String): String? {
    val lower = fileName.lowercase()
    return when {
        lower.endsWith(".png") -> "png"
        lower.endsWith(".zip") || lower.endsWith(".mcworld") ||
                lower.endsWith(".mcpack") || lower.endsWith(".mcaddon") -> "zip"

        else -> null
    }
}

/** 应用时区（与 utils/extension/FunExt 的 timeZoneCN 保持一致：Asia/Shanghai）。 */
private val APP_ZONE: TimeZone = TimeZone.of("Asia/Shanghai")

/** 当前本地日期（Asia/Shanghai 时区）。 */
private fun todayLocalDate(): LocalDate {
    val nowMillis = Clock.System.now().toEpochMilliseconds()
    return Instant.fromEpochMilliseconds(nowMillis).toLocalDateTime(APP_ZONE).date
}
