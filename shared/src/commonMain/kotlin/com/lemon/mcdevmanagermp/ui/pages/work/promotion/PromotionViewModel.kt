package com.lemon.mcdevmanagermp.ui.pages.work.promotion

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.repository.FileUploadRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.PromotionRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.PromotionTemplateRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.ResourceRepositoryImpl
import com.lemon.mcdevmanagermp.data.vo.netease.promotion.UserApplyItemVO
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.domain.promotion.PromotionTemplate
import com.lemon.mcdevmanagermp.domain.promotion.PromotionUseCase
import com.lemon.mcdevmanagermp.domain.resource.GetResourceListUseCase
import com.lemon.mcdevmanagermp.domain.upload.FileUploadUseCase
import com.lemon.mcdevmanagermp.domain.work.WorkDetailUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.ui.components.ModSelectOption
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Clock

class PromotionViewModel :
    BaseViewModel<PromotionState, PromotionAction, PromotionEffect>(PromotionState()) {

    private val promotionUseCase = PromotionUseCase(PromotionRepositoryImpl.INSTANCE)
    private val resourceListUseCase = GetResourceListUseCase(ResourceRepositoryImpl.INSTANCE)
    private val fileUploadUseCase = FileUploadUseCase(FileUploadRepositoryImpl.INSTANCE)
    private val workDetailUseCase = WorkDetailUseCase(ResourceRepositoryImpl.INSTANCE)
    private val templateRepo = PromotionTemplateRepositoryImpl.INSTANCE

    private var fullItems: List<ResourceData> = emptyList()

    override fun dispatch(action: PromotionAction) {
        when (action) {
            is PromotionAction.LoadData -> loadData()
            is PromotionAction.Refresh -> loadData()
            is PromotionAction.SearchItems -> searchItems(action.query)
            is PromotionAction.SelectItem -> setState { copy(selectedItem = action.item) }
            is PromotionAction.RemoveItem -> setState { copy(selectedItem = null) }
            is PromotionAction.SelectDate -> setState { copy(selectedStartTime = action.timestamp) }
            is PromotionAction.SelectPromoImage -> setState {
                copy(
                    promoImage = action.file,
                    promoImageUrl = ""
                )
            }

            is PromotionAction.RemovePromoImage -> setState {
                copy(
                    promoImage = null,
                    promoImageUrl = ""
                )
            }

            is PromotionAction.UpdateExtra -> setState { copy(extra = action.value) }
            is PromotionAction.UpdateActivity -> setState { copy(activity = action.value) }
            is PromotionAction.UpdateFeature -> setState { copy(feature = action.value) }
            is PromotionAction.UpdateUpdate -> setState { copy(update = action.value) }
            is PromotionAction.SyncFromPe -> syncFromPe()
            is PromotionAction.LoadTemplates -> loadTemplates()
            is PromotionAction.LoadUserApply -> loadUserApply()
            is PromotionAction.LoadMoreUserApply ->
                loadUserApply(start = state.value.userApplies.size)

            is PromotionAction.SaveTemplate -> saveTemplate(action.name)
            is PromotionAction.ApplyTemplate -> applyTemplate(action.id)
            is PromotionAction.DeleteTemplate -> deleteTemplate(action.id)
            is PromotionAction.DismissSaveTemplateDialog -> setState { copy(showSaveTemplateDialog = false) }
            is PromotionAction.ShowTemplatePicker -> {
                loadTemplates()
                setState { copy(showTemplatePicker = true) }
            }

            is PromotionAction.DismissTemplatePicker -> setState { copy(showTemplatePicker = false) }
            is PromotionAction.StartModifyApply -> startModify(action.item)
            is PromotionAction.SelectTab -> setState { copy(selectedTab = action.tab) }
            is PromotionAction.CancelModify -> cancelModify()
            is PromotionAction.Submit -> submit()
        }
    }

    private fun loadData() {
        if (state.value.isLoading) return
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val canApply = promotionUseCase.loadCanApply()
            val items = when (val r = resourceListUseCase("pe")) {
                is NetworkState.Success -> r.data ?: emptyList()
                is NetworkState.Error -> emptyList()
            }
            fullItems = items
            setState {
                copy(
                    isLoading = false,
                    permit = canApply.permit,
                    reason = canApply.reason,
                    recentRecords = canApply.recentRecords,
                    searchResults = items.take(20).map { ModSelectOption(it.itemId, it.itemName) }
                )
            }
            if (canApply.error != null) {
                sendEffect(PromotionEffect.ShowToast("加载可申请状态失败: ${canApply.error}"))
            }
            loadTemplates()
            loadUserApply()
        }
    }

    /** start=0 替换列表（首次/刷新/提交后），start>0 追加（加载更多）。 */
    private fun loadUserApply(start: Int = 0) {
        val loadMore = start > 0
        if (loadMore && state.value.isLoadingMoreUserApply) return
        viewModelScope.launch {
            if (loadMore) setState { copy(isLoadingMoreUserApply = true) }
            when (val r = promotionUseCase.loadUserApply(start = start)) {
                is NetworkState.Success -> setState {
                    copy(
                        userApplies = if (loadMore) {
                            userApplies + (r.data?.applications ?: emptyList())
                        } else {
                            r.data?.applications ?: emptyList()
                        },
                        userApplyCount = r.data?.count ?: userApplyCount,
                        isLoadingMoreUserApply = false
                    )
                }

                is NetworkState.Error -> {
                    if (loadMore) setState { copy(isLoadingMoreUserApply = false) }
                }
            }
        }
    }

    private fun searchItems(query: String) {
        val filtered = if (query.isBlank()) fullItems
        else fullItems.filter { it.itemName.contains(query, ignoreCase = true) }
        setState {
            copy(searchResults = filtered.take(30).map { ModSelectOption(it.itemId, it.itemName) })
        }
    }

    /**
     * 一键同步：拉取所选作品的 PE 详细信息（ResourceDetailVO.info），灌入 4 个描述框；
     * syncEchoKey +1 触发各 RichDetailForm 回显新 HTML。
     */
    private fun syncFromPe() {
        val current = state.value
        val item = current.selectedItem ?: run {
            sendEffect(PromotionEffect.ShowToast("请先选择参与的作品"))
            return
        }
        if (current.isSyncing) return
        viewModelScope.launch {
            setState { copy(isSyncing = true) }
            when (val r = workDetailUseCase.getResourceDetail(item.id)) {
                is NetworkState.Error -> {
                    setState { copy(isSyncing = false) }
                    sendEffect(PromotionEffect.ShowToast("加载 PE 详情失败: ${r.msg}"))
                }

                is NetworkState.Success -> {
                    val info = r.data?.info ?: ""
                    setState {
                        copy(
                            isSyncing = false,
                            activity = info,
                            feature = info,
                            update = info,
                            syncEchoKey = syncEchoKey + 1
                        )
                    }
                    sendEffect(
                        PromotionEffect.ShowToast(
                            if (info.isEmpty()) "该作品暂无 PE 详细信息" else "已同步 PE 详细信息"
                        )
                    )
                }
            }
        }
    }

    private fun loadTemplates() {
        viewModelScope.launch {
            val list = templateRepo.getAll()
            setState { copy(templates = list) }
        }
    }

    /** 把当前 4 个描述字段 + 宣传图链接存为模板（提交成功后由保存对话框触发）。 */
    private fun saveTemplate(name: String) {
        val current = state.value
        viewModelScope.launch {
            templateRepo.save(
                PromotionTemplate(
                    name = name,
                    extra = current.extra,
                    activity = current.activity,
                    feature = current.feature,
                    update = current.update,
                    promoImageUrl = current.promoImageUrl,
                    createdAt = Clock.System.now().toEpochMilliseconds()
                )
            )
            setState { copy(showSaveTemplateDialog = false) }
            loadTemplates()
            sendEffect(PromotionEffect.ShowToast("模板已保存"))
        }
    }

    /** 一键套用模板：4 个描述字段 + 宣传图链接覆盖 + syncEchoKey+1 触发 RichDetailForm 回显。 */
    private fun applyTemplate(id: Long) {
        val template = state.value.templates.firstOrNull { it.id == id } ?: return
        setState {
            copy(
                extra = template.extra,
                activity = template.activity,
                feature = template.feature,
                update = template.update,
                promoImageUrl = template.promoImageUrl,
                syncEchoKey = syncEchoKey + 1,
                showTemplatePicker = false
            )
        }
        sendEffect(PromotionEffect.ShowToast("已应用模板"))
    }

    private fun deleteTemplate(id: Long) {
        viewModelScope.launch {
            templateRepo.delete(id)
            loadTemplates()
            sendEffect(PromotionEffect.ShowToast("模板已删除"))
        }
    }

    /** 进入修改模式：把申请记录回填到表单，editingApplyId 标记修改目标。 */
    private fun startModify(item: UserApplyItemVO) {
        setState {
            copy(
                editingApplicationId = item.applicationId,
                selectedItem = ModSelectOption(item.itemId, item.itemName.ifBlank { item.itemId }),
                selectedStartTime = item.startTime.toLong(),
                selectedTab = PromotionTab.APPLY,
                promoImage = null,
                promoImageUrl = item.peChannel,
                extra = item.extra,
                activity = item.activity,
                feature = item.feature,
                update = item.update,
                syncEchoKey = syncEchoKey + 1
            )
        }
        sendEffect(PromotionEffect.ShowToast("已加载申请内容，修改后点击保存"))
    }

    /** 退出修改模式，清空表单回到新建申请。 */
    private fun cancelModify() {
        setState {
            copy(
                editingApplicationId = null,
                selectedItem = null,
                selectedStartTime = null,
                promoImage = null,
                promoImageUrl = "",
                extra = "",
                activity = "",
                feature = "",
                update = "",
                syncEchoKey = syncEchoKey + 1
            )
        }
    }

    private fun submit() {
        val current = state.value
        if (current.isSubmitting || current.isUploading) return
        val item = current.selectedItem
        val startTime = current.selectedStartTime
        val promoImage = current.promoImage
        if (item == null) {
            sendEffect(PromotionEffect.ShowToast("请选择参与的作品"))
            return
        }
        if (startTime == null) {
            sendEffect(PromotionEffect.ShowToast("请选择开始日期"))
            return
        }
        if (promoImage == null && current.promoImageUrl.isBlank()) {
            sendEffect(PromotionEffect.ShowToast("请上传宣传图"))
            return
        }

        viewModelScope.launch {
            // promoImage 非空→上传新图；否则沿用模板带的 promoImageUrl（套用模板时回填，免重传）
            val url = if (promoImage != null) {
                setState { copy(isUploading = true) }
                val uploadMime = if (promoImage.name.endsWith(
                        ".png",
                        ignoreCase = true
                    )
                ) "image/png" else "image/jpeg"
                val uploadResult = fileUploadUseCase.uploadImage(
                    fileName = promoImage.name,
                    file = promoImage,
                    mimeType = uploadMime
                )
                setState { copy(isUploading = false) }
                val uploadedBody = (uploadResult as? NetworkState.Success)?.data?.body
                if (uploadedBody.isNullOrEmpty()) {
                    val err = (uploadResult as? NetworkState.Error)?.msg ?: "上传失败"
                    sendEffect(PromotionEffect.ShowToast("宣传图上传失败: $err"))
                    return@launch
                }
                // 从body里面获取url
                val jObject = Json.decodeFromString<JsonObject>(uploadedBody)
                jObject["url"]?.jsonPrimitive?.contentOrNull ?: run {
                    sendEffect(PromotionEffect.ShowToast("宣传图上传失败: url解析失败"))
                    return@launch
                }
            } else {
                current.promoImageUrl
            }

            // 2. 提交申请或修改（peChannel 用上传后的 URL 或既有 URL）
            setState { copy(isSubmitting = true, promoImageUrl = url) }
            val isModify = current.editingApplicationId != null
            val error = if (isModify) {
                promotionUseCase.modifyApplyPromotion(
                    applicationId = current.editingApplicationId,
                    itemId = item.id,
                    startTimeSeconds = startTime,
                    peChannel = url,
                    extra = current.extra,
                    activity = current.activity,
                    feature = current.feature,
                    update = current.update
                )
            } else {
                promotionUseCase.applyPromotion(
                    itemId = item.id,
                    startTimeSeconds = startTime,
                    peChannel = url,
                    extra = current.extra,
                    activity = current.activity,
                    feature = current.feature,
                    update = current.update
                )
            }
            setState { copy(isSubmitting = false) }
            if (error != null) {
                sendEffect(PromotionEffect.ShowToast("${if (isModify) "修改" else "申请"}失败: $error"))
            } else {
                sendEffect(PromotionEffect.ShowToast(if (isModify) "修改已保存" else "申请已提交"))
                setState {
                    copy(
                        showSaveTemplateDialog = true,
                        saveTemplateName = item.name,
                        editingApplicationId = null
                    )
                }
                loadData()
            }
        }
    }
}
