package com.lemon.mcdevmanagermp.ui.pages.work.activity.participate

import com.lemon.mcdevmanagermp.data.vo.netease.activity.CandidatesItemVO
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ReviewActivityItemVO
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

data class ActivityParticipateState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val activity: ReviewActivityItemVO? = null,
    val selectedModuleId: Int? = null,
    val candidates: List<CandidatesItemVO> = emptyList(),
    val selectedCandidateId: String? = null,
    val applyIntro: String = "",
) : IUiState

sealed interface ActivityParticipateAction : IUiAction {
    data class LoadData(val activity: ReviewActivityItemVO) : ActivityParticipateAction
    data class SelectModule(val moduleId: Int) : ActivityParticipateAction
    data class SelectCandidate(val itemId: String) : ActivityParticipateAction
    data class UpdateApplyIntro(val intro: String) : ActivityParticipateAction
    data object Submit : ActivityParticipateAction
}

sealed interface ActivityParticipateEffect : IUiEffect {
    data class ShowToast(val message: String) : ActivityParticipateEffect
    data object ParticipateSuccess : ActivityParticipateEffect
}
