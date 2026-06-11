package com.lemon.mcdevmanagermp.ui.pages.work.activity.participate

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.dto.netease.activity.JoinActivityDTO
import com.lemon.mcdevmanagermp.data.repository.ActivityRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.FileUploadRepositoryImpl
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ReviewActivityItemVO
import com.lemon.mcdevmanagermp.domain.activity.ActivityUseCase
import com.lemon.mcdevmanagermp.domain.upload.FileUploadUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class ActivityParticipateViewModel :
    BaseViewModel<ActivityParticipateState, ActivityParticipateAction, ActivityParticipateEffect>(
        ActivityParticipateState()
    ) {
    companion object {
        private const val MAX_IMAGE_COUNT = 3
        private const val MAX_VIDEO_SIZE = 50 * 1024 * 1024L // 50MB
    }

    private val activityUseCase = ActivityUseCase(
        activityRepository = ActivityRepositoryImpl.INSTANCE
    )
    private val fileUploadUseCase = FileUploadUseCase(
        fileUploadRepository = FileUploadRepositoryImpl.INSTANCE
    )

    override fun dispatch(action: ActivityParticipateAction) {
        when (action) {
            is ActivityParticipateAction.LoadData -> loadActivity(action.activity)
            is ActivityParticipateAction.SelectModule -> selectModule(action.moduleId)
            is ActivityParticipateAction.SelectCandidate -> selectCandidate(action.itemId)
            is ActivityParticipateAction.UpdateApplyIntro -> updateApplyIntro(action.intro)
            is ActivityParticipateAction.AddImages -> addImages(action.images)
            is ActivityParticipateAction.RemoveImage -> removeImage(action.index)
            is ActivityParticipateAction.AddVideo -> addVideo(action.video)
            is ActivityParticipateAction.RemoveVideo -> removeVideo()
            is ActivityParticipateAction.ValidationError -> handleValidationError(action.message)
            is ActivityParticipateAction.Submit -> submitParticipation()
        }
    }

    private fun loadActivity(activity: ReviewActivityItemVO) {
        val firstModuleId = activity.modules.firstOrNull()?.moduleId
        setState { copy(activity = activity, selectedModuleId = firstModuleId) }

        if (firstModuleId != null) {
            loadCandidates(activity.id, firstModuleId)
        }
    }

    private fun selectModule(moduleId: Int) {
        val activity = state.value.activity ?: return
        setState {
            copy(
                selectedModuleId = moduleId,
                candidates = emptyList(),
                selectedCandidateId = null
            )
        }
        loadCandidates(activity.id, moduleId)
    }

    private fun loadCandidates(activityId: String, moduleId: Int) {
        if (state.value.isLoading) return
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val (candidates, error) = activityUseCase.loadModuleCandidates(activityId, moduleId)
            setState { copy(isLoading = false, candidates = candidates) }
            if (error != null) {
                sendEffect(ActivityParticipateEffect.ShowToast("加载候选作品失败: $error"))
            }
        }
    }

    private fun selectCandidate(itemId: String) {
        setState { copy(selectedCandidateId = itemId) }
    }

    private fun updateApplyIntro(intro: String) {
        setState { copy(applyIntro = intro) }
    }

    private fun addImages(images: List<SelectedImage>) {
        val currentCount = state.value.selectedImages.size
        val remaining = MAX_IMAGE_COUNT - currentCount
        if (remaining <= 0) {
            sendEffect(ActivityParticipateEffect.ShowToast("最多只能选择 $MAX_IMAGE_COUNT 张图片"))
            return
        }
        val toAdd = images.take(remaining)
        setState { copy(selectedImages = selectedImages + toAdd) }
    }

    private fun removeImage(index: Int) {
        val current = state.value.selectedImages
        if (index in current.indices) {
            setState { copy(selectedImages = current.filterIndexed { i, _ -> i != index }) }
        }
    }

    private fun addVideo(video: SelectedVideo) {
        if (video.size > MAX_VIDEO_SIZE) {
            sendEffect(ActivityParticipateEffect.ShowToast("视频文件大小不能超过 50MB"))
            return
        }
        if (video.bytes.size > MAX_VIDEO_SIZE) {
            sendEffect(ActivityParticipateEffect.ShowToast("视频文件大小不能超过 50MB"))
            return
        }
        setState { copy(selectedVideo = video) }
    }

    private fun removeVideo() {
        setState { copy(selectedVideo = null) }
    }

    private fun handleValidationError(message: String) {
        sendEffect(ActivityParticipateEffect.ShowToast(message))
    }

    private fun submitParticipation() {
        val currentState = state.value
        val activity = currentState.activity ?: return
        val moduleId = currentState.selectedModuleId ?: return
        val candidateId = currentState.selectedCandidateId

        if (candidateId == null) {
            sendEffect(ActivityParticipateEffect.ShowToast("请选择要参与的作品"))
            return
        }

        if (currentState.isSubmitting || currentState.isUploading) return

        viewModelScope.launch {
            setState { copy(isUploading = true, uploadProgress = "准备上传文件...") }

            // 1. 上传图片
            val imageUrls = mutableListOf<String>()
            if (currentState.selectedImages.isNotEmpty()) {
                setState {
                    copy(uploadProgress = "正在上传图片 (0/${currentState.selectedImages.size})...")
                }
                val (urls, imageErrors) = fileUploadUseCase.uploadImages(
                    files = currentState.selectedImages.map { it.name to it.bytes }
                )
                imageUrls.addAll(urls)
                if (imageErrors.isNotEmpty()) {
                    setState { copy(isUploading = false, uploadProgress = "") }
                    sendEffect(ActivityParticipateEffect.ShowToast(imageErrors.first()))
                    return@launch
                }
            }

            // 2. 上传视频
            val videoUrls = mutableListOf<String>()
            val video = currentState.selectedVideo
            if (video != null) {
                setState { copy(uploadProgress = "正在上传视频...") }
                val (url, error) = fileUploadUseCase.uploadVideo(video.name, video.bytes)
                if (error != null) {
                    setState { copy(isUploading = false, uploadProgress = "") }
                    sendEffect(ActivityParticipateEffect.ShowToast("视频上传失败: $error"))
                    return@launch
                }
                if (url != null) {
                    videoUrls.add(url)
                }
            }

            // 3. 提交参与
            setState {
                copy(
                    isUploading = false,
                    isSubmitting = true,
                    uploadProgress = "正在提交参与..."
                )
            }
            val error = activityUseCase.joinActivity(
                activityId = activity.id,
                moduleId = moduleId,
                content = JoinActivityDTO(
                    itemId = candidateId,
                    applyIntro = currentState.applyIntro,
                    imageList = imageUrls,
                    videoInfoList = videoUrls
                )
            )
            setState { copy(isSubmitting = false, uploadProgress = "") }

            if (error != null) {
                sendEffect(ActivityParticipateEffect.ShowToast("参与失败: $error"))
            } else {
                sendEffect(ActivityParticipateEffect.ShowToast("参与成功！"))
                sendEffect(ActivityParticipateEffect.ParticipateSuccess)
            }
        }
    }
}
