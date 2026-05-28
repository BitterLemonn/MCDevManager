package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.api.ApiFactory.provideKtorfit
import com.lemon.mcdevmanagermp.data.consts.GITHUB_RESTFUL_LINK
import com.lemon.mcdevmanagermp.data.vo.github.LatestReleaseVO
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import kotlin.getValue

interface GithubUpdateApi {

    @GET("/repos/{author}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("author") author: String = "BitterLemonn",
        @Path("repo") repo: String = "MCDevManager"
    ): LatestReleaseVO

    companion object {
        val INSTANCE by lazy {
            provideKtorfit(GITHUB_RESTFUL_LINK).createGithubUpdateApi()
        }
    }
}