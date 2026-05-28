package com.lemon.mcdevmanagermp.data.dto.netease.login

import com.lemon.mcdevmanagermp.data.vo.netease.login.BaseLoginVO
import com.lemon.mcdevmanagermp.data.vo.netease.login.CapIdVO
import com.lemon.mcdevmanagermp.data.vo.netease.login.PowerVO
import com.lemon.mcdevmanagermp.data.vo.netease.login.TicketVO
import com.lemon.mcdevmanagermp.utils.encrpy.dataJsonToString


interface NeteaseLoginResult {
    val ret: Int
    fun extractData(): String? = null
}

class TicketResult(private val vo: TicketVO) : NeteaseLoginResult {
    override val ret: Int get() = vo.ret
    override fun extractData(): String = vo.tk
}

class BaseLoginResult(private val vo: BaseLoginVO) : NeteaseLoginResult {
    override val ret: Int get() = vo.ret
}

class PowerResult(private val vo: PowerVO) : NeteaseLoginResult {
    override val ret: Int get() = vo.ret
    override fun extractData(): String = dataJsonToString(vo.pVInfo)
}

class CapIdResult(private val vo: CapIdVO) : NeteaseLoginResult {
    override val ret: Int get() = vo.ret
    override fun extractData(): String = vo.capId
}