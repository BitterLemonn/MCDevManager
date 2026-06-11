package com.lemon.mcdevmanagermp.ui.pages.work.activity.participate

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.dto.netease.activity.JoinActivityDTO
import com.lemon.mcdevmanagermp.data.repository.ActivityRepositoryImpl
import com.lemon.mcdevmanagermp.data.vo.netease.activity.ReviewActivityItemVO
import com.lemon.mcdevmanagermp.domain.activity.ActivityUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class ActivityParticipateViewModel :
    BaseViewModel<ActivityParticipateState, ActivityParticipateAction, ActivityParticipateEffect>(
        ActivityParticipateState()
    ) {
    private val activityUseCase = ActivityUseCase(
        activityRepository = ActivityRepositoryImpl.INSTANCE
    )

    override fun dispatch(action: ActivityParticipateAction) {
        when (action) {
            is ActivityParticipateAction.LoadData -> loadActivity(action.activity)
            is ActivityParticipateAction.SelectModule -> selectModule(action.moduleId)
            is ActivityParticipateAction.SelectCandidate -> selectCandidate(action.itemId)
            is ActivityParticipateAction.UpdateApplyIntro -> updateApplyIntro(action.intro)
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

    private fun submitParticipation() {
        val currentState = state.value
        val activity = currentState.activity ?: return
        val moduleId = currentState.selectedModuleId ?: return
        val candidateId = currentState.selectedCandidateId

        if (candidateId == null) {
            sendEffect(ActivityParticipateEffect.ShowToast("请选择要参与的作品"))
            return
        }

        if (currentState.isSubmitting) return

        viewModelScope.launch {
            setState { copy(isSubmitting = true) }
            val error = activityUseCase.joinActivity(
                activityId = activity.id,
                moduleId = moduleId,
                content = JoinActivityDTO(
                    itemId = candidateId,
                    applyIntro = currentState.applyIntro
                )
            )
            setState { copy(isSubmitting = false) }

            if (error != null) {
                sendEffect(ActivityParticipateEffect.ShowToast("参与失败: $error"))
            } else {
                sendEffect(ActivityParticipateEffect.ShowToast("参与成功！"))
                sendEffect(ActivityParticipateEffect.ParticipateSuccess)
            }
        }
    }
}
