package com.lemon.mcdevmanagermp.domain.resource

import com.lemon.mcdevmanagermp.data.common.ResponseData
import com.lemon.mcdevmanagermp.data.vo.netease.resource.MCConstsVO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * `mc_consts` 进程内缓存。
 *
 * 取代此前随包分发的 `consts.json` 快照 —— 快照会随服务端新增类别/版本而滞后
 * （实测已缺 `mod_version 3.9`、`mc_version 1.21.10`）。改为首次请求时拉取并缓存，
 * 由 cookie 生命周期驱动失效：切换账号 / 退出登录会清空 cookie，此时清空缓存以
 * 避免把上一账号的常量表带给新账号。
 */
object MCConstsCache {
    private val OK_STATUSES = setOf("200", "201", "ok", "OK", "Ok")

    private val mutex = Mutex()
    private var cached: MCConstsVO? = null

    /**
     * 命中缓存时合成成功响应（与接口成功态一致，供 `UnifiedExceptionHandler` 走同一通路），
     * 否则调用 [fetch] 并在成功后缓存。失败响应不入缓存，保证下次仍会重试。
     */
    suspend fun loadOrFetch(
        fetch: suspend () -> ResponseData<MCConstsVO>
    ): ResponseData<MCConstsVO> {
        mutex.withLock { cached?.let { return ResponseData("ok", it) } }
        // 锁外请求：拉取期间不阻塞读取；并发下可能重复请求一次，可接受。
        val fresh = fetch()
        if (fresh.status in OK_STATUSES && fresh.data != null) {
            mutex.withLock { cached = fresh.data }
        }
        return fresh
    }

    fun clear() {
        cached = null
    }
}
