package com.lemon.mcdevmanagermp.ui.pages.work.activity.participate

import com.lemon.mcdevmanagermp.data.vo.netease.activity.CandidatesItemVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ReviewActivityItemVO
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path

data class ActivityParticipateState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isUploading: Boolean = false,
    val activity: ReviewActivityItemVO? = null,
    val selectedModuleId: Int? = null,
    val candidates: List<CandidatesItemVO> = emptyList(),
    val selectedCandidateId: String? = null,
    val applyIntro: String = "",
    val selectedImages: List<SelectedImage> = emptyList(),
    val selectedVideo: SelectedVideo? = null,
    val uploadProgress: String = "",
) : IUiState

/**
 * 选中的图片数据
 * 延迟加载：选中时只保存 PlatformFile 引用，上传时才读取文件内容
 */
data class SelectedImage(
    val name: String,
    val file: PlatformFile,
    val mimeType: String = "image/jpeg",
) {
    /** 图片预览 URI */
    val uri: String get() = file.path

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SelectedImage) return false
        return name == other.name && file == other.file
    }

    override fun hashCode(): Int = 31 * name.hashCode() + file.hashCode()
}

/**
 * 选中的视频数据
 * 延迟加载：选中时只保存 PlatformFile 引用，上传时才读取文件内容
 */
data class SelectedVideo(
    val name: String,
    val file: PlatformFile,
    val size: Long,
    val mimeType: String = "video/mp4",
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SelectedVideo) return false
        return name == other.name && file == other.file
    }

    override fun hashCode(): Int = 31 * name.hashCode() + file.hashCode()
}

sealed interface ActivityParticipateAction : IUiAction {
    data class LoadData(val activity: ReviewActivityItemVO) : ActivityParticipateAction
    data class SelectModule(val moduleId: Int) : ActivityParticipateAction
    data class SelectCandidate(val itemId: String) : ActivityParticipateAction
    data class UpdateApplyIntro(val intro: String) : ActivityParticipateAction
    data class AddImages(val images: List<SelectedImage>) : ActivityParticipateAction
    data class RemoveImage(val index: Int) : ActivityParticipateAction
    data class AddVideo(val video: SelectedVideo) : ActivityParticipateAction
    data object RemoveVideo : ActivityParticipateAction
    data class ValidationError(val message: String) : ActivityParticipateAction
    data object Submit : ActivityParticipateAction
}

sealed interface ActivityParticipateEffect : IUiEffect {
    data class ShowToast(val message: String) : ActivityParticipateEffect
    data object ParticipateSuccess : ActivityParticipateEffect
}
