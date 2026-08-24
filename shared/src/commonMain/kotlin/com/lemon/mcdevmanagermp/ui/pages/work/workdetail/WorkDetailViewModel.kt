package com.lemon.mcdevmanagermp.ui.pages.work.workdetail

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.enums.PePriTypeEnum
import com.lemon.mcdevmanagermp.data.consts.enums.PriceRankEnum
import com.lemon.mcdevmanagermp.data.consts.enums.PriceTypeEnum
import com.lemon.mcdevmanagermp.data.dto.netease.work.WorkCreateChannel
import com.lemon.mcdevmanagermp.data.dto.netease.work.WorkCreateDTO
import com.lemon.mcdevmanagermp.data.dto.netease.work.WorkCreateRes
import com.lemon.mcdevmanagermp.data.dto.netease.work.WorkUpdateDlcInfoDTO
import com.lemon.mcdevmanagermp.data.repository.FileUploadRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsChannelData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsCommonTitleData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailChannel
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailDlcInfo
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailRes
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailSyncChannel
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailTag
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceDetailVideoInfo
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceRequirementData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.VIDEO_COVER_CHANNEL_ID
import com.lemon.mcdevmanagermp.data.vo.netease.resource.requiredPeImageChannels
import com.lemon.mcdevmanagermp.domain.upload.FileUploadRepository
import com.lemon.mcdevmanagermp.domain.upload.parseUploadUrl
import com.lemon.mcdevmanagermp.domain.work.WorkDetailUseCase
import com.lemon.mcdevmanagermp.domain.work.WorkSaveValidationInput
import com.lemon.mcdevmanagermp.domain.work.validateWorkSave
import com.lemon.mcdevmanagermp.platform.validateVideoFile
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.ui.components.ModSelectOption
import com.lemon.mcdevmanagermp.utils.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
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
            WorkDetailAction.InitNewWork -> initNewWork()
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
            is WorkDetailAction.ToggleSyncPc -> setState {
                val originallySynced = detail?.syncPcFlag == true
                val resolvedType = resolvePcResourceType(
                    syncPc = action.value,
                    originallySynced = originallySynced,
                    currentPcResourceType = pcResourceType,
                    peResourceType = peResourceType,
                    peOptions = peResourceTypeOptions,
                    pcOptions = pcResourceTypeOptions
                )
                copy(
                    syncPc = action.value,
                    pcResourceType = resolvedType,
                    pcResourceSubType = resolvePcResourceSubType(
                        syncPc = action.value,
                        originallySynced = originallySynced,
                        pcResourceType = resolvedType,
                        currentPcResourceSubType = pcResourceSubType,
                        peResourceType = peResourceType,
                        peResourceSubType = peResourceSubType,
                        peOptions = peResourceSubTypeOptions,
                        pcOptions = pcResourceSubTypeOptions
                    )
                )
            }

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
            is WorkDetailAction.UpdatePeModVersion -> setState { copy(peModVersion = action.value) }
            is WorkDetailAction.UploadPeZip -> uploadPeZip(action.file)
            WorkDetailAction.RemovePeResource -> clearPeResource()

            // —— 联机大厅 ——
            is WorkDetailAction.UpdateLobbyMinNum -> updateLobbyMinNum(action.value)
            is WorkDetailAction.UpdateLobbyMaxNum -> updateLobbyMaxNum(action.value)
            is WorkDetailAction.UpdateLobbyForceMaxNum -> updateLobbyForceMaxNum(action.value)
            is WorkDetailAction.ToggleLobbyTag -> toggleLobbyTag(action.id)
            is WorkDetailAction.ToggleLobbyAsymmetric -> setState {
                copy(
                    lobbyIsAsymmetric = action.value,
                    lobbyCamps = when {
                        !action.value -> emptyList()
                        lobbyCamps.size >= 2 -> lobbyCamps
                        else -> listOf("", "")
                    }
                )
            }
            is WorkDetailAction.UpdateLobbyCamp -> setState {
                if (action.index !in lobbyCamps.indices) this
                else copy(lobbyCamps = lobbyCamps.toMutableList().apply {
                    this[action.index] = action.value
                })
            }
            is WorkDetailAction.UpdateLobbyPlayerNum -> setState {
                copy(lobbyPlayerNum = action.value.coerceIn(1, 15))
            }
            is WorkDetailAction.ToggleLobbyNormalMode -> setState {
                copy(lobbyNormalMode = action.value)
            }
            is WorkDetailAction.UpdateLobbyReconnectTime -> setState {
                copy(lobbyReconnectTime = action.value.coerceIn(1, 99))
            }

            // —— PC 资源管理 ——
            is WorkDetailAction.UpdatePcResourceType -> setState {
                // 切换模组类别时清空具体类别：旧 subType 不属于新类别，避免脏数据
                copy(pcResourceType = action.id, pcResourceSubType = 0)
            }

            is WorkDetailAction.UpdatePcAvailableScope -> setState { copy(pcAvailableScope = action.id) }
            is WorkDetailAction.UpdatePcResourceSubType -> setState { copy(pcResourceSubType = action.id) }

            // —— 视频 ——
            is WorkDetailAction.UploadVideo -> uploadVideo(action.file)
            is WorkDetailAction.RemoveVideo -> removeVideo(action.index)
            is WorkDetailAction.UploadVideoCover -> uploadVideoCover(
                action.index,
                action.file,
                action.mimeType
            )

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

            // —— PE/PC 宣传图 ——
            is WorkDetailAction.SelectPeChannelImage -> uploadChannelImage(
                isPe = true,
                channelId = action.channelId,
                file = action.file,
                mimeType = action.mimeType
            )

            is WorkDetailAction.RemovePeChannelImage -> removeChannelImage(
                isPe = true,
                channelId = action.channelId
            )

            is WorkDetailAction.SelectPcChannelImage -> uploadChannelImage(
                isPe = false,
                channelId = action.channelId,
                file = action.file,
                mimeType = action.mimeType
            )

            is WorkDetailAction.RemovePcChannelImage -> removeChannelImage(
                isPe = false,
                channelId = action.channelId
            )

            WorkDetailAction.Save -> submit(alsoReview = false)
            WorkDetailAction.SaveAndReview -> submit(alsoReview = true)
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
                                joinShantou = d.isDomainServerItem == 1,
                                isOriginal = d.isOriginal,
                                tags = d.tags.map { it.name }.filter { it.isNotEmpty() },
                                // prerequisiteItems 为 List<JsonElement>，结构未知，本期留空由用户手动填写
                                prerequisite = "",
                                activityDesc = d.activityDesc,
                                isRelatedMod = d.dlcInfo.dlcSwitch,
                                relatedIsMaster = d.dlcInfo.dlcType != ResourceDetailDlcInfo.DlcType.SLAVE.type,
                                relatedItemId = d.relateItemId,
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
                                // —— PC 资源管理 ——
                                pcResourceType = if (d.syncPcFlag) d.syncItemInfo.priType else 0,
                                pcAvailableScope = d.syncItemInfo.availableScope,
                                pcResourceSubType = if (d.syncPcFlag) d.syncItemInfo.subType else 0,
                                // —— PE 资源管理 ——
                                peResourceType = d.priType,
                                peResourceSubType = d.subType,
                                peResourceModSecondType = d.modSecondType,
                                peModVersion = d.modVersion,
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
                                // —— 联机大厅 ——
                                lobbyMinNum = d.lobbyMinNum,
                                lobbyMaxNum = d.lobbyMaxNum,
                                lobbyForceMaxNum = d.lobbyForceMaxNum.takeIf { it > 0 } ?: 10,
                                lobbyTags = d.lobbyTags.mapNotNull {
                                    (it as? JsonPrimitive)?.intOrNull
                                },
                                isLobbyCompetitive = d.isLobbyCompetitive,
                                lobbyIsAsymmetric = d.lobbyCamps.isNotEmpty(),
                                lobbyCamps = d.lobbyCamps.mapNotNull {
                                    (it as? JsonPrimitive)?.contentOrNull
                                },
                                lobbyPlayerNum = d.lobbyPlayerNum,
                                lobbyNormalMode = d.lobbyNormalMode,
                                lobbyReconnectTime = d.lobbyReconnectTime,
                                // —— 视频 ——
                                videos = d.videoInfoList.map {
                                    VideoItem(
                                        cover = it.cover.ifEmpty {
                                            d.channel.firstOrNull { channel ->
                                                channel.channelId == VIDEO_COVER_CHANNEL_ID
                                            }?.channelUrl.orEmpty()
                                        },
                                        size = it.size.toLong(),
                                        url = it.url
                                    )
                                },
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
                    val pcTypes = consts.priType.comp
                    val peTypes = consts.priType.pe
                    val pcSubTypes = parsePriTypeSubTypeOptions(consts.subType.pc)
                    val peSubTypes = parsePriTypeSubTypeOptions(consts.subType.pe)
                    // 回显 PC 模组标签：syncItemInfo.tag(List<Int> id) 用 options 映射为 title
                    val tagTitles = (state.value.detail?.syncItemInfo?.tag ?: emptyList())
                        .mapNotNull { id -> options.firstOrNull { it.id == id }?.title }
                    setState {
                        val originallySynced = detail?.syncPcFlag == true
                        val resolvedType = resolvePcResourceType(
                            syncPc = syncPc,
                            originallySynced = originallySynced,
                            currentPcResourceType = pcResourceType,
                            peResourceType = peResourceType,
                            peOptions = peTypes,
                            pcOptions = pcTypes
                        )
                        copy(
                            pcTagOptions = options,
                            pcTags = tagTitles,
                            pcResourceTypeOptions = pcTypes,
                            pcAvailableScopeOptions = consts.availableScope,
                            pcResourceSubTypeOptions = pcSubTypes,
                            pcResourceType = resolvedType,
                            pcResourceSubType = resolvePcResourceSubType(
                                syncPc = syncPc,
                                originallySynced = originallySynced,
                                pcResourceType = resolvedType,
                                currentPcResourceSubType = pcResourceSubType,
                                peResourceType = peResourceType,
                                peResourceSubType = peResourceSubType,
                                peOptions = peSubTypes,
                                pcOptions = pcSubTypes
                            ),
                            peResourceTypeOptions = peTypes,
                            peRecommendTagOptions = consts.labelType,
                            peRecommendTagLimit = consts.itemTagLimit,
                            lobbyTagOptions = consts.lobbyTags,
                            lobbyTagLimit = consts.itemTagLimit,
                            lobbyCompetitiveTagId = consts.lobbyTags
                                .firstOrNull { it.title == "竞技模式" }?.id ?: -1,
                            pePriTypeFileTypes = parsePriTypeFileTypes(consts.subType.pe),
                            peResourceSubTypeOptions = peSubTypes,
                            peModSecondTypeOptions = consts.modSecondType.subTag,
                            peModVersionOptions = consts.modVersion,
                            // PE/PC 宣传图位：按 mc_consts.channel 定义生成全部槽位并回填已有图片
                            peImageSlots = buildChannelSlots(
                                raw = detail?.channel.orEmpty(),
                                defs = consts.channel.requiredPeImageChannels(),
                                channelIdOf = { it.channelId },
                                urlOf = { it.channelUrl }
                            ),
                            pcImageSlots = buildPcChannelSlots(
                                raw = detail?.syncItemInfo?.channel.orEmpty(),
                                defs = consts.channel.comp
                            )
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

    private fun updateLobbyMinNum(value: Int) {
        setState {
            if (value == 0) copy(lobbyMinNum = 0, lobbyMaxNum = 0)
            else {
                val normalized = value.coerceIn(2, lobbyForceMaxNum.coerceAtLeast(2))
                copy(
                    lobbyMinNum = normalized,
                    lobbyMaxNum = lobbyMaxNum.coerceAtLeast(normalized)
                )
            }
        }
    }

    private fun updateLobbyMaxNum(value: Int) {
        setState {
            if (value == 0) copy(lobbyMinNum = 0, lobbyMaxNum = 0)
            else {
                val normalized = value.coerceIn(2, lobbyForceMaxNum.coerceAtLeast(2))
                copy(
                    lobbyMinNum = if (lobbyMinNum == 0) 2 else lobbyMinNum.coerceAtMost(normalized),
                    lobbyMaxNum = normalized
                )
            }
        }
    }

    private fun updateLobbyForceMaxNum(value: Int) {
        setState {
            val normalized = value.coerceIn(2, 10)
            val maxNum = if (lobbyMaxNum == 0) 0 else lobbyMaxNum.coerceAtMost(normalized)
            copy(
                lobbyForceMaxNum = normalized,
                lobbyMaxNum = maxNum,
                lobbyMinNum = if (maxNum == 0) 0 else lobbyMinNum.coerceAtMost(maxNum)
            )
        }
    }

    private fun toggleLobbyTag(id: Int) {
        setState {
            val selected = id in lobbyTags
            when {
                id == lobbyCompetitiveTagId && selected -> copy(
                    lobbyTags = lobbyTags - id,
                    isLobbyCompetitive = false
                )
                id == lobbyCompetitiveTagId -> copy(
                    lobbyTags = listOf(id),
                    isLobbyCompetitive = true,
                    lobbyPlayerNum = lobbyPlayerNum.coerceIn(1, 15),
                    lobbyReconnectTime = lobbyReconnectTime.coerceIn(1, 99)
                )
                isLobbyCompetitive -> this
                selected -> copy(lobbyTags = lobbyTags - id)
                lobbyTagLimit > 0 && lobbyTags.size >= lobbyTagLimit -> this
                else -> copy(lobbyTags = lobbyTags + id)
            }
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
                fileType = "zip_package",
                fileName = file.name,
                file = file,
                mimeType = "application/zip",
                secure = "true"
            )
            when (result) {
                is NetworkState.Success -> {
                    val info = result.data
                    val url = parseUploadUrl(info?.body.orEmpty())
                    val resource = PeResourceFile(
                        name = file.name,
                        url = url,
                        addVersion = state.value.peAddVersion,
                        fileInfo = info
                    )
                    setState { copy(peResource = resource) }
                    if (url.isEmpty()) {
                        setState { copy(isUploadingPeZip = false) }
                        sendEffect(WorkDetailEffect.ShowToast("上传成功但未能解析资源地址"))
                        return@launch
                    }

                    val detail = state.value.detail
                    if (detail == null) {
                        setState { copy(isUploadingPeZip = false) }
                        return@launch
                    }
                    val updatedDetail = withUpdatedPeResource(detail, resource, resource.addVersion)
                    when (val update =
                        workDetailUseCase.updateWork(updatedDetail, isCheckApply = false)) {
                        is NetworkState.Success -> setState {
                            copy(detail = updatedDetail, isUploadingPeZip = false)
                        }

                        is NetworkState.Error -> {
                            setState { copy(isUploadingPeZip = false) }
                            handleError(
                                update,
                                onNeedReLogin = { WorkDetailEffect.NeedReLogin },
                                onShowToast = { WorkDetailEffect.ShowToast(it) }
                            )
                        }
                    }
                }

                is NetworkState.Error -> {
                    setState { copy(isUploadingPeZip = false) }
                    sendEffect(WorkDetailEffect.ShowToast(result.msg))
                }
            }
        }
    }

    private fun removeVideo(index: Int) {
        setState { copy(videos = videos.filterIndexed { i, _ -> i != index }) }
    }

    private fun uploadVideo(file: PlatformFile) {
        if (state.value.isUploadingVideo) return
        viewModelScope.launch {
            val size = try {
                file.size()
            } catch (_: Exception) {
                0L
            }
            // 格式校验：16:9 / 时长 ≤1:30 / H264 / ≤50MB（各平台实现，阻塞 IO）
            val validation = withContext(Dispatchers.IO) { validateVideoFile(file, size) }
            if (!validation.isValid) {
                sendEffect(
                    WorkDetailEffect.ShowToast(
                        validation.errorMessage ?: "视频文件不符合要求"
                    )
                )
                return@launch
            }
            setState { copy(isUploadingVideo = true) }
            val mimeType = try {
                file.mimeType()?.toString()
            } catch (_: Exception) {
                null
            } ?: "video/mp4"
            val result = fileUploadRepository.uploadFile(
                fileType = "video",
                fileName = file.name,
                file = file,
                mimeType = mimeType
            )
            when (result) {
                is NetworkState.Success -> {
                    val url = parseUploadUrl(result.data?.body.orEmpty())
                    setState {
                        copy(
                            isUploadingVideo = false,
                            videos = listOf(VideoItem(cover = "", size = size, url = url))
                        )
                    }
                    if (url.isEmpty()) sendEffect(WorkDetailEffect.ShowToast("上传成功但未能解析视频地址"))
                }

                is NetworkState.Error -> {
                    setState { copy(isUploadingVideo = false) }
                    sendEffect(WorkDetailEffect.ShowToast(result.msg))
                }
            }
        }
    }

    private fun uploadVideoCover(index: Int, file: PlatformFile, mimeType: String) {
        viewModelScope.launch {
            setState {
                copy(videos = videos.mapIndexed { i, v ->
                    if (i == index) v.copy(isUploadingCover = true) else v
                })
            }
            val result = fileUploadRepository.uploadFile(
                fileType = "image",
                fileName = file.name,
                file = file,
                mimeType = mimeType
            )
            when (result) {
                is NetworkState.Success -> {
                    val coverUrl = parseUploadUrl(result.data?.body.orEmpty())
                    setState {
                        copy(videos = videos.mapIndexed { i, v ->
                            if (i == index) v.copy(
                                cover = coverUrl,
                                isUploadingCover = false
                            ) else v
                        })
                    }
                    if (coverUrl.isEmpty()) sendEffect(WorkDetailEffect.ShowToast("封面上传失败"))
                }

                is NetworkState.Error -> {
                    setState {
                        copy(videos = videos.mapIndexed { i, v ->
                            if (i == index) v.copy(isUploadingCover = false) else v
                        })
                    }
                    sendEffect(WorkDetailEffect.ShowToast(result.msg))
                }
            }
        }
    }

    // ===== PE/PC 宣传图 =====

    /** 按 isPe 更新对应 slot 列表里指定 channelId 的项（setState 内调用）。 */
    private fun WorkDetailState.updateImageSlot(
        isPe: Boolean,
        channelId: Int,
        transform: (ChannelImageSlot) -> ChannelImageSlot
    ): WorkDetailState = if (isPe) {
        copy(peImageSlots = peImageSlots.map { if (it.channelId == channelId) transform(it) else it })
    } else {
        copy(pcImageSlots = pcImageSlots.map { if (it.channelId == channelId) transform(it) else it })
    }

    private fun removeChannelImage(isPe: Boolean, channelId: Int) {
        setState {
            updateImageSlot(isPe, channelId) {
                it.copy(
                    channelUrl = "",
                    isUploading = false
                )
            }
        }
    }

    /** 选图（裁剪后）立即上传到网易 FP，成功后更新对应 slot 的 channelUrl。 */
    private fun uploadChannelImage(
        isPe: Boolean,
        channelId: Int,
        file: PlatformFile,
        mimeType: String
    ) {
        viewModelScope.launch {
            setState { updateImageSlot(isPe, channelId) { it.copy(isUploading = true) } }
            val result = fileUploadRepository.uploadFile(
                fileType = "image",
                fileName = file.name,
                file = file,
                mimeType = mimeType
            )
            when (result) {
                is NetworkState.Success -> {
                    val info = result.data
                    val url = parseUploadUrl(info?.body.orEmpty())
                    setState {
                        updateImageSlot(isPe, channelId) {
                            it.copy(
                                channelUrl = url,
                                isUploading = false,
                                fileInfo = info
                            )
                        }
                    }
                    if (url.isEmpty()) sendEffect(WorkDetailEffect.ShowToast("上传成功但未能解析图片地址"))
                }

                is NetworkState.Error -> {
                    setState { updateImageSlot(isPe, channelId) { it.copy(isUploading = false) } }
                    sendEffect(WorkDetailEffect.ShowToast(result.msg))
                }
            }
        }
    }

    /** 按 mc_consts.channel 定义生成全部槽位，并回填详情中已有图片。 */
    private fun <T> buildChannelSlots(
        raw: List<T>,
        defs: List<MCConstsChannelData>,
        channelIdOf: (T) -> Int,
        urlOf: (T) -> String
    ): List<ChannelImageSlot> = defs.distinctBy { it.id }.map { def ->
        val channel = raw.firstOrNull { channelIdOf(it) == def.id }
        ChannelImageSlot(
            channelId = def.id,
            title = def.title,
            width = def.width,
            height = def.height,
            channelUrl = channel?.let(urlOf).orEmpty(),
            version = def.version
        )
    }

    // ===== 提交保存（更新） =====

    /** 新建模式：不加载详情，仅加载表单选项（默认标签 / mc_consts），进入空表单。 */
    private fun initNewWork() {
        setState { copy(isLoading = false, detail = null, itemId = "") }
        loadItemTags()
        loadPcTagOptions()
    }

    /**
     * 新建作品（pe/upload）。res/channel 用上传回执 FileInfoDTO。
     * [alsoReview]=true 时 is_check_apply=true（创建并发起提审）；成功后返回列表。
     */
    private fun createWork(alsoReview: Boolean) {
        viewModelScope.launch {
            setState {
                copy(
                    isSubmitting = true,
                    submittingMessage = if (alsoReview) "创建并提审中..." else "创建中..."
                )
            }
            val payload = buildWorkCreatePayload(state.value, isCheckApply = alsoReview)
            when (val result = workDetailUseCase.createWork(payload)) {
                is NetworkState.Success -> {
                    sendEffect(
                        WorkDetailEffect.ShowToast(if (alsoReview) "创建并提审成功" else "创建成功")
                    )
                    sendEffect(WorkDetailEffect.NavigateBack)
                }

                is NetworkState.Error -> handleError(
                    result,
                    onNeedReLogin = { WorkDetailEffect.NeedReLogin },
                    onShowToast = { WorkDetailEffect.ShowToast(it) }
                )
            }
            setState { copy(isSubmitting = false, submittingMessage = "") }
        }
    }

    /**
     * 保存作品信息（第1/2节，isCheckApply=false 纯保存）。
     * [alsoReview]=true 时保存成功后继续调第3节 apply_review 发起提审。
     * 全流程成功后返回列表；保存成功但提审失败则停留并提示（数据已落库，可重试）。
     */
    private fun submit(alsoReview: Boolean) {
        val s = state.value
        if (s.isSubmitting) return
        if (s.isUploadingPeZip) {
            sendEffect(WorkDetailEffect.ShowToast("资源文件上传中"))
            return
        }
        if (s.peImageSlots.any { it.isUploading }) {
            sendEffect(WorkDetailEffect.ShowToast("PE 图片上传中"))
            return
        }
        if (s.isUploadingVideo || s.videos.any { it.isUploadingCover }) {
            sendEffect(WorkDetailEffect.ShowToast("视频上传中"))
            return
        }
        validateWorkDetail(s)?.let {
            sendEffect(WorkDetailEffect.ShowToast(it))
            return
        }
        val detail = s.detail
        if (detail == null) {
            // 新建模式：走 pe/upload 创建接口
            createWork(alsoReview)
            return
        }
        if (detail.itemId.isEmpty()) {
            sendEffect(WorkDetailEffect.ShowToast("作品详情未加载"))
            return
        }
        viewModelScope.launch {
            setState {
                copy(
                    isSubmitting = true,
                    submittingMessage = if (alsoReview) "提交审核中..." else "保存中..."
                )
            }
            // 授权图：本地新选则先上传，否则沿用远端 URL
            val corpProofUrl = if (s.corpProofFile != null) {
                val file = s.corpProofFile
                val mimeType = runCatching { file.mimeType()?.toString() }.getOrNull() ?: "image/*"
                when (val r = fileUploadRepository.uploadFile(
                    fileType = "image",
                    fileName = file.name,
                    file = file,
                    mimeType = mimeType
                )) {
                    is NetworkState.Success -> parseUploadUrl(r.data?.body.orEmpty())
                    is NetworkState.Error -> {
                        setState { copy(isSubmitting = false, submittingMessage = "") }
                        sendEffect(WorkDetailEffect.ShowToast(r.msg))
                        return@launch
                    }
                }
            } else {
                s.corpProofImage
            }
            val payload = buildUpdatePayload(s, detail, corpProofUrl)
            when (val result = workDetailUseCase.updateWork(payload, isCheckApply = false)) {
                is NetworkState.Success -> if (alsoReview) {
                    // 保存成功 → 第3节发起提审
                    when (val review = workDetailUseCase.submitForReview(detail.itemId)) {
                        is NetworkState.Success -> {
                            sendEffect(WorkDetailEffect.ShowToast("提审成功"))
                            sendEffect(WorkDetailEffect.NavigateBack)
                        }

                        is NetworkState.Error -> sendEffect(
                            WorkDetailEffect.ShowToast("已保存，提审失败：${review.msg}")
                        )
                    }
                } else {
                    sendEffect(WorkDetailEffect.ShowToast("保存成功"))
                    sendEffect(WorkDetailEffect.NavigateBack)
                }

                is NetworkState.Error -> handleError(
                    result,
                    onNeedReLogin = { WorkDetailEffect.NeedReLogin },
                    onShowToast = { WorkDetailEffect.ShowToast(it) }
                )
            }
            setState { copy(isSubmitting = false, submittingMessage = "") }
        }
    }

}

/** PC 图片按 mc_consts.channel.comp 生成全部槽位，而不是只展示已有 channel。 */
internal fun buildPcChannelSlots(
    raw: List<ResourceDetailSyncChannel>,
    defs: List<MCConstsChannelData>
): List<ChannelImageSlot> = defs.distinctBy { it.id }.map { def ->
    val channel = raw.firstOrNull { it.channelId == def.id }
    ChannelImageSlot(
        channelId = def.id,
        title = def.title,
        width = def.width,
        height = def.height,
        channelUrl = channel?.channelUrl.orEmpty(),
        version = def.version
    )
}

/** 立即持久化新资源时仅替换 res，避免提交表单中其他未保存字段。 */
internal fun withUpdatedPeResource(
    detail: ResourceDetailVO,
    resource: PeResourceFile,
    addVersion: Boolean
): ResourceDetailVO = detail.copy(
    res = listOf(
        ResourceDetailRes(
            addVersion = addVersion,
            resName = resource.name,
            resUrl = resource.url,
            mcVersion = resource.mcVersion,
            fileInfo = resource.fileInfo
        )
    )
)

// ponytail: PE/PC ID 空间不同，只接受已确认的标题关系，最终 ID 始终取运行时 mc_consts。
private val PE_TO_PC_RESOURCE_TYPE_TITLES = mapOf(
    "地图" to setOf("地图组件", "地图模组"),
    "add_ons" to setOf("功能组件", "功能模组"),
    "材质光影" to setOf("视觉组件", "视觉模组"),
    "皮肤" to setOf("形象组件", "形象模组"),
    "联机大厅" to setOf("联机大厅"),
    "礼包" to setOf("礼包")
)

internal fun resolvePcResourceType(
    syncPc: Boolean,
    originallySynced: Boolean,
    currentPcResourceType: Int,
    peResourceType: Int,
    peOptions: List<MCConstsCommonTitleData>,
    pcOptions: List<MCConstsCommonTitleData>
): Int {
    if (!syncPc || originallySynced) return currentPcResourceType
    if (pcOptions.count { it.id == currentPcResourceType } == 1) return currentPcResourceType
    val peTitle = peOptions.singleOrNull { it.id == peResourceType }?.title ?: return 0
    val targetTitles = PE_TO_PC_RESOURCE_TYPE_TITLES[peTitle] ?: return 0
    return pcOptions.singleOrNull { it.title in targetTitles }?.id ?: 0
}

internal fun resolvePcResourceSubType(
    syncPc: Boolean,
    originallySynced: Boolean,
    pcResourceType: Int,
    currentPcResourceSubType: Int,
    peResourceType: Int,
    peResourceSubType: Int,
    peOptions: Map<Int, List<MCConstsCommonTitleData>>,
    pcOptions: Map<Int, List<MCConstsCommonTitleData>>
): Int {
    if (!syncPc || originallySynced) return currentPcResourceSubType
    val targetOptions = pcOptions[pcResourceType].orEmpty()
    if (targetOptions.count { it.id == currentPcResourceSubType } == 1) {
        return currentPcResourceSubType
    }
    val peTitle = peOptions[peResourceType]
        ?.singleOrNull { it.id == peResourceSubType }
        ?.title
        ?: return 0
    return targetOptions.singleOrNull { it.title == peTitle }?.id ?: 0
}

internal fun validateWorkDetail(state: WorkDetailState): String? {
    validateWorkSave(
        WorkSaveValidationInput(
            itemName = state.itemName,
            tags = state.tags,
            priceType = state.priceType,
            priceRank = state.priceRank.type,
            price = state.emeraldPrice,
            detailHtml = state.peDetail,
            peResourceType = state.peResourceType,
            recommendTagIds = state.peRecommendTags,
            gameplayTagIds = state.peRecommendTagOptions.gameplayTag.mapTo(mutableSetOf()) { it.id },
            themeTagIds = state.peRecommendTagOptions.themeTag.mapTo(mutableSetOf()) { it.id },
            modVersion = state.peModVersion,
            hasResource = state.peResource != null,
            channelsLoaded = state.peImageSlots.isNotEmpty(),
            requiredChannelIds = state.peImageSlots.mapTo(mutableSetOf()) { it.channelId },
            availableChannelIds = state.peImageSlots
                .filter { it.channelUrl.isNotBlank() }
                .mapTo(mutableSetOf()) { it.channelId },
            hasVideo = state.videos.any { it.url.isNotBlank() },
            syncPc = state.syncPc,
            originallySyncedToPc = state.detail?.syncPcFlag == true,
            pcResourceType = state.pcResourceType,
            validPcResourceTypeIds = state.pcResourceTypeOptions.mapTo(mutableSetOf()) { it.id },
            pcResourceSubType = state.pcResourceSubType,
            validPcResourceSubTypeIds = state.pcResourceSubTypeOptions[state.pcResourceType]
                .orEmpty()
                .mapTo(mutableSetOf()) { it.id },
        )
    )?.let { return it }
    return validateLobbySettings(state)
}

internal fun validateLobbySettings(state: WorkDetailState): String? {
    if (state.peResourceType != PePriTypeEnum.LOBBY.value.toInt()) return null
    if (state.lobbyTags.isEmpty()) return "请至少选择一个联机大厅专区分类"
    if (state.lobbyTagLimit > 0 && state.lobbyTags.size > state.lobbyTagLimit) {
        return "联机大厅专区分类最多选择 ${state.lobbyTagLimit} 个"
    }
    if (state.lobbyForceMaxNum !in 2..10) return "房间限制人数应为 2–10"
    val suggestedNumValid =
        state.lobbyMinNum == 0 && state.lobbyMaxNum == 0 ||
                state.lobbyMinNum in 2..state.lobbyForceMaxNum &&
                state.lobbyMaxNum in 2..state.lobbyForceMaxNum &&
                state.lobbyMinNum <= state.lobbyMaxNum
    if (!suggestedNumValid) return "建议游戏人数应同时为 0，或保持 2 ≤ 最小人数 ≤ 最大人数 ≤ 房间限制人数"
    if (!state.isLobbyCompetitive) return null
    if (state.lobbyPlayerNum !in 1..15) return "游戏开始人数应为 1–15"
    if (state.lobbyReconnectTime !in 1..99) return "逃跑时间应为 1–99 分钟"
    if (state.lobbyIsAsymmetric &&
        (state.lobbyCamps.size < 2 || state.lobbyCamps.any { it.isBlank() })
    ) {
        return "请填写至少两个阵营名称"
    }
    return null
}

/** 由编辑态构造新建请求体（pe/upload）。res/channel 仅含已上传（有 fileInfo）的项。 */
internal fun buildWorkCreatePayload(s: WorkDetailState, isCheckApply: Boolean): WorkCreateDTO =
    WorkCreateDTO(
        itemName = s.itemName,
        itemVersion = bumpVersion(s.itemVersion),
        labelTypeList = s.peRecommendTags,
        priType = s.peResourceType,
        subType = s.peResourceSubType,
        modSecondType = s.peResourceModSecondType,
        modVersion = s.peModVersion,
        info = s.peDetail,
        lobbyMinNum = if (s.peResourceType == PePriTypeEnum.LOBBY.value.toInt()) s.lobbyMinNum else 0,
        lobbyMaxNum = if (s.peResourceType == PePriTypeEnum.LOBBY.value.toInt()) s.lobbyMaxNum else 0,
        lobbyForceMaxNum = if (s.peResourceType == PePriTypeEnum.LOBBY.value.toInt()) s.lobbyForceMaxNum else 10,
        lobbyTags = if (s.peResourceType == PePriTypeEnum.LOBBY.value.toInt()) {
            s.lobbyTags.map(::JsonPrimitive)
        } else emptyList(),
        isLobbyCompetitive = s.peResourceType == PePriTypeEnum.LOBBY.value.toInt() && s.isLobbyCompetitive,
        isAsymmetric = s.peResourceType == PePriTypeEnum.LOBBY.value.toInt() &&
                s.isLobbyCompetitive && s.lobbyIsAsymmetric,
        lobbyCamps = if (s.peResourceType == PePriTypeEnum.LOBBY.value.toInt() &&
            s.isLobbyCompetitive && s.lobbyIsAsymmetric
        ) s.lobbyCamps.map { JsonPrimitive(it.trim()) } else emptyList(),
        lobbyPlayerNum = if (s.peResourceType == PePriTypeEnum.LOBBY.value.toInt() && s.isLobbyCompetitive) {
            s.lobbyPlayerNum
        } else 0,
        lobbyNormalMode = s.peResourceType == PePriTypeEnum.LOBBY.value.toInt() &&
                s.isLobbyCompetitive && s.lobbyNormalMode,
        lobbyReconnectTime = if (s.peResourceType == PePriTypeEnum.LOBBY.value.toInt() &&
            s.isLobbyCompetitive
        ) s.lobbyReconnectTime else 0,
        updateSummary = s.peUpdateSummary,
        tag = s.tags.map { ResourceDetailTag(name = it) },
        isOriginal = s.isOriginal,
        isDomainServerItem = if (s.joinShantou) 1 else 0,
        priceType = priceTypeString(s.priceType, "free"),
        priceRank = s.priceRank.type,
        price = when (s.priceType) {
            PriceTypeEnum.EMERALD -> s.emeraldPrice
            PriceTypeEnum.DIAMOND -> if (s.priceRank.diamondPrice > 0) s.priceRank.diamondPrice else 0
            else -> 0
        },
        res = s.peResource?.fileInfo?.let { info ->
            listOf(
                WorkCreateRes(
                    resUrl = info,
                    resName = s.peResource.name,
                    addVersion = s.peAddVersion
                )
            )
        } ?: emptyList(),
        channel = s.peImageSlots.mapNotNull { slot ->
            slot.fileInfo?.let { WorkCreateChannel(channelId = slot.channelId, channelUrl = it) }
        },
        videoInfoList = s.videos.map { video ->
            buildJsonObject {
                put("cover", video.cover)
                put("size", video.size)
                put("url", video.url)
            }
        },
        syncPcFlag = s.syncPc,
        dlcInfo = WorkUpdateDlcInfoDTO(
            dlcSwitch = s.isRelatedMod,
            dlcType = when {
                !s.isRelatedMod -> "off"
                s.relatedIsMaster -> ResourceDetailDlcInfo.DlcType.MASTER.type
                else -> ResourceDetailDlcInfo.DlcType.SLAVE.type
            }
        ),
        isCheckApply = isCheckApply
    )

/** 将编辑后的 state 合并回原始详情，构造 update 请求体。 */
internal fun buildUpdatePayload(
    s: WorkDetailState,
    d: ResourceDetailVO,
    corpProofUrl: String
): ResourceDetailVO {
    val updateLobby = s.peResourceType == PePriTypeEnum.LOBBY.value.toInt()
    return d.copy(
        itemName = s.itemName,
    itemVersion = bumpVersion(d.itemVersion),
    isDomainServerItem = if (s.joinShantou) 1 else 0,
    isOriginal = s.isOriginal,
    tags = s.tags.map { name ->
        d.tags.firstOrNull { it.name == name } ?: ResourceDetailTag(name = name)
    },
    corpProofImage = corpProofUrl,
    activityDesc = s.activityDesc,
    info = s.peDetail,
    lobbyMinNum = if (updateLobby) s.lobbyMinNum else d.lobbyMinNum,
    lobbyMaxNum = if (updateLobby) s.lobbyMaxNum else d.lobbyMaxNum,
    lobbyForceMaxNum = if (updateLobby) s.lobbyForceMaxNum else d.lobbyForceMaxNum,
    lobbyTags = if (updateLobby) s.lobbyTags.map(::JsonPrimitive) else d.lobbyTags,
    isLobbyCompetitive = if (updateLobby) s.isLobbyCompetitive else d.isLobbyCompetitive,
    isAsymmetric = if (updateLobby) {
        s.isLobbyCompetitive && s.lobbyIsAsymmetric
    } else d.isAsymmetric,
    lobbyCamps = if (updateLobby) {
        if (s.isLobbyCompetitive && s.lobbyIsAsymmetric) {
            s.lobbyCamps.map { JsonPrimitive(it.trim()) }
        } else emptyList()
    } else d.lobbyCamps,
    lobbyPlayerNum = if (updateLobby && s.isLobbyCompetitive) s.lobbyPlayerNum
        else if (updateLobby) 0 else d.lobbyPlayerNum,
    lobbyNormalMode = if (updateLobby) s.isLobbyCompetitive && s.lobbyNormalMode
        else d.lobbyNormalMode,
    lobbyReconnectTime = if (updateLobby && s.isLobbyCompetitive) s.lobbyReconnectTime
        else if (updateLobby) 0 else d.lobbyReconnectTime,
    updateSummary = s.peUpdateSummary,
    priType = s.peResourceType,
    subType = s.peResourceSubType,
    modSecondType = s.peResourceModSecondType,
    modVersion = s.peModVersion,
    labelTypeList = s.peRecommendTags,
    peIsAddPlayPlan = false,
    mountCallEnabled = s.peMountCallEnabled,
    weakOffline = s.peWeakOffline,
    weakOfflineReason = s.peWeakOfflineReason,
    syncPcFlag = s.syncPc,
    priceType = priceTypeString(s.priceType, d.priceType),
    priceRank = s.priceRank.type,
    price = when (s.priceType) {
        PriceTypeEnum.EMERALD -> s.emeraldPrice
        PriceTypeEnum.DIAMOND -> if (s.priceRank.diamondPrice > 0) s.priceRank.diamondPrice else d.price
        PriceTypeEnum.FREE -> 0
        else -> d.price
    },
    discount = buildDiscountJson(s.discounts, d.discount),
    res = buildResList(s.peResource, d.res, s.peAddVersion),
    videoInfoList = s.videos.map {
        ResourceDetailVideoInfo(cover = it.cover, size = it.size.toInt(), url = it.url)
    },
    channel = if (s.peImageSlots.isEmpty()) d.channel else s.peImageSlots.map { slot ->
        ResourceDetailChannel(
            channelId = slot.channelId,
            channelUrl = slot.channelUrl,
            version = d.channel.firstOrNull { it.channelId == slot.channelId }?.version ?: 0,
            fileInfo = slot.fileInfo
        )
    },
    syncItemInfo = d.syncItemInfo.copy(
        brief = s.pcBrief,
        info = s.pcDetail,
        includeMap = s.pcIncludeMap,
        priType = s.pcResourceType,
        subType = s.pcResourceSubType,
        availableScope = s.pcAvailableScope,
        weakOffline = s.pcWeakOffline,
        weakOfflineReason = s.pcWeakOfflineReason,
        tag = if (s.pcTagOptions.isEmpty()) d.syncItemInfo.tag else {
            s.pcTags.mapNotNull { t -> s.pcTagOptions.firstOrNull { it.title == t }?.id }
        },
        channel = if (s.pcImageSlots.isEmpty()) {
            d.syncItemInfo.channel
        } else {
            val editableIds = s.pcImageSlots.mapTo(mutableSetOf()) { it.channelId }
            d.syncItemInfo.channel.filter { it.channelId !in editableIds } +
                    s.pcImageSlots.filter { it.channelUrl.isNotEmpty() }.map { slot ->
                        ResourceDetailSyncChannel(
                            channelId = slot.channelId,
                            channelUrl = slot.channelUrl,
                            version = slot.version,
                            fileInfo = slot.fileInfo
                        )
                    }
        },
        requirement = if (s.pcHasPrerequisite) {
            s.pcPrerequisites.map { ResourceRequirementData(itemId = it.id, itemName = it.name) }
        } else {
            emptyList()
        }
    ),
    dlcInfo = d.dlcInfo.copy(
        dlcSwitch = s.isRelatedMod,
        dlcType = when {
            !s.isRelatedMod -> "off"
            s.relatedIsMaster -> ResourceDetailDlcInfo.DlcType.MASTER.type
            else -> ResourceDetailDlcInfo.DlcType.SLAVE.type
        }
    ),
        relateItemId = if (s.isRelatedMod) s.relatedItemId else ""
    )
}

/** 折扣：用户未配置时保留原值，避免误清空；已配置则按 Unix 秒重建（vip_discount 暂同 discount）。 */
private fun buildDiscountJson(
    discounts: List<DiscountConfig>,
    origin: List<JsonElement>
): List<JsonElement> {
    if (discounts.isEmpty()) return origin
    return discounts.map { cfg ->
        buildJsonObject {
            put("begin_at", cfg.beginDate.atStartOfDayIn(APP_ZONE).epochSeconds)
            put(
                "end_at",
                cfg.endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(APP_ZONE).epochSeconds - 1
            )
            put("discount", cfg.percent)
            put("vip_discount", cfg.percent)
            put("is_activity_discount", 0)
        }
    }
}

/** PE 资源：空值或仍为详情回显资源时保留完整原数据，仅新上传时重建。 */
private fun buildResList(
    pe: PeResourceFile?,
    origin: List<ResourceDetailRes>,
    addVersion: Boolean
): List<ResourceDetailRes> {
    if (pe == null) return origin
    origin.firstOrNull()?.let {
        if (pe.name == it.resName && pe.url == it.resUrl) {
            return listOf(it.copy(addVersion = addVersion)) + origin.drop(1)
        }
    }
    return listOf(
        ResourceDetailRes(
            addVersion = addVersion,
            resName = pe.name,
            resUrl = pe.url,
            mcVersion = pe.mcVersion,
            fileInfo = pe.fileInfo
        )
    )
}

/**
 * 版本号 +0.1（minor 满 9 进位：0.9→1.0、1.9→2.0）；空串视为新作品首版 1.0。
 * 仅识别 "major.minor" 十进制形式，minor>9 进位。
 */
private fun bumpVersion(version: String): String {
    if (version.isEmpty()) return "0.1"
    val parts = version.split(".")
    val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val newMinor = minor + 1
    return if (newMinor > 9) "${major + 1}.0" else "$major.$newMinor"
}

private fun priceTypeString(t: PriceTypeEnum, fallback: String): String = when (t) {
    PriceTypeEnum.DIAMOND -> "diamond"
    PriceTypeEnum.EMERALD -> "point"
    PriceTypeEnum.FREE -> "free"
    PriceTypeEnum.UNKNOWN -> fallback
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
