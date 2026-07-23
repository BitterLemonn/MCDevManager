package com.lemon.mcdevmanagermp.domain.user

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.user.LevelInfoVO

class GetLevelInfoUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): NetworkState<LevelInfoVO> = userRepository.getLevelInfo()
}
