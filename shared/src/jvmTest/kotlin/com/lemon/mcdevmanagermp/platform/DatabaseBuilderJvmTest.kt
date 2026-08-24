package com.lemon.mcdevmanagermp.platform

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DatabaseBuilderJvmTest {

    @Test
    fun `hot reload stores data below the stable Gradle build root`() {
        val buildRoot = File("/project")

        assertEquals(
            buildRoot.absoluteFile,
            resolveHotReloadApplicationDirectory(
                isActive = "true",
                buildRoot = buildRoot.path
            )
        )
    }

    @Test
    fun `normal launch ignores the Gradle build root`() {
        assertNull(
            resolveHotReloadApplicationDirectory(
                isActive = "false",
                buildRoot = "/project"
            )
        )
    }

    @Test
    fun `hot reload restores the newest database from the generated classpath`() {
        val tempDirectory = Files.createTempDirectory("hot-run-").toFile()
        val runDirectory = tempDirectory.resolve("run")
        val targetDirectory = tempDirectory.resolve("stable-data")
        val oldDataDirectory = runDirectory.resolve("classpath/libs/shared/old/.data")
        val newDataDirectory = runDirectory.resolve("classpath/libs/shared/new/.data")
        oldDataDirectory.mkdirs()
        newDataDirectory.mkdirs()
        oldDataDirectory.resolve("mc_dev_manager.db").apply {
            writeText("old")
            setLastModified(1L)
        }
        newDataDirectory.resolve("mc_dev_manager.db").apply {
            writeText("new")
            setLastModified(2L)
        }
        newDataDirectory.resolve("mc_dev_manager.db-wal").writeText("wal")

        try {
            migrateLegacyHotReloadDatabase(targetDirectory, runDirectory)

            assertEquals("new", targetDirectory.resolve("mc_dev_manager.db").readText())
            assertEquals("wal", targetDirectory.resolve("mc_dev_manager.db-wal").readText())
        } finally {
            tempDirectory.deleteRecursively()
        }
    }

    @Test
    fun `portable distribution keeps data next to the application when writable`() {
        val applicationDirectory = File("/portable/app")
        val userDataDirectory = File("/user-data/MCDevManager")

        val directory = selectJvmDatabaseDirectory(
            applicationDirectory = applicationDirectory,
            userDataDirectory = userDataDirectory,
            prepareDirectory = { it == applicationDirectory.resolve(".data") }
        )

        assertEquals(applicationDirectory.resolve(".data").absoluteFile, directory)
    }

    @Test
    fun `installed distribution falls back to user data when application directory is read-only`() {
        val applicationDirectory = File("/program-files/MCDevManager/app")
        val userDataDirectory = File("/user-data/MCDevManager")

        val directory = selectJvmDatabaseDirectory(
            applicationDirectory = applicationDirectory,
            userDataDirectory = userDataDirectory,
            prepareDirectory = { it == userDataDirectory }
        )

        assertEquals(userDataDirectory.absoluteFile, directory)
    }

    @Test
    fun `windows stores data below LocalAppData`() {
        val directory = resolveJvmAppDataDirectory(
            osName = "Windows 11",
            userHome = "/users/test",
            environment = mapOf("LOCALAPPDATA" to "/local-app-data")
        )

        assertEquals(File("/local-app-data/MCDevManager").absoluteFile, directory)
    }

    @Test
    fun `windows falls back to the user profile`() {
        val directory = resolveJvmAppDataDirectory(
            osName = "Windows 11",
            userHome = "/users/test",
            environment = emptyMap()
        )

        assertEquals(File("/users/test/AppData/Local/MCDevManager").absoluteFile, directory)
    }

    @Test
    fun `macOS stores data below Application Support`() {
        val directory = resolveJvmAppDataDirectory(
            osName = "Mac OS X",
            userHome = "/users/test",
            environment = emptyMap()
        )

        assertEquals(
            File("/users/test/Library/Application Support/MCDevManager").absoluteFile,
            directory
        )
    }

    @Test
    fun `linux honors XDG data home`() {
        val directory = resolveJvmAppDataDirectory(
            osName = "Linux",
            userHome = "/home/test",
            environment = mapOf("XDG_DATA_HOME" to "/xdg-data")
        )

        assertEquals(File("/xdg-data/MCDevManager").absoluteFile, directory)
    }

    @Test
    fun `linux falls back to the user data directory`() {
        val directory = resolveJvmAppDataDirectory(
            osName = "Linux",
            userHome = "/home/test",
            environment = emptyMap()
        )

        assertEquals(File("/home/test/.local/share/MCDevManager").absoluteFile, directory)
    }

    @Test
    fun saveAccountWithRememberPasswordStoresCredentials() {
        val fakeAccountRepo = object : com.lemon.mcdevmanagermp.domain.account.AccountRepository {
            val accounts = mutableListOf<com.lemon.mcdevmanagermp.domain.account.Account>()
            override suspend fun getAllAccounts() = accounts
            override suspend fun getLastUsedAccount() = accounts.lastOrNull()
            override suspend fun getAccountByNickname(nickname: String) = accounts.firstOrNull { it.nickname == nickname }
            override suspend fun upsertAccount(account: com.lemon.mcdevmanagermp.domain.account.Account) {
                val idx = accounts.indexOfFirst { it.nickname == account.nickname }
                if (idx >= 0) accounts[idx] = account else accounts.add(account)
            }
            override suspend fun deleteAccount(id: Long) { accounts.removeAll { it.id == id } }
            override suspend fun updateNicknameById(id: Long, nickname: String) {}
        }
        val fakeUserRepo = object : com.lemon.mcdevmanagermp.domain.user.UserRepository {
            override suspend fun getUserInfo(): com.lemon.mcdevmanagermp.data.common.NetworkState<com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO> {
                return com.lemon.mcdevmanagermp.data.common.NetworkState.Success(
                    com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO(
                        exp = 0,
                        level = 1,
                        nickname = "TestUser",
                        headImg = "http://example.com/head.png",
                        income = "0",
                        onSaleItemCount = 0,
                        curMonthIncentiveFund = 0.0,
                        unExtractIncome = "0",
                        prerequisiteSwitch = false
                    )
                )
            }
            override suspend fun getOverview(): com.lemon.mcdevmanagermp.data.common.NetworkState<com.lemon.mcdevmanagermp.data.vo.netease.user.OverviewVO> = error("unused")
            override suspend fun getLevelInfo(): com.lemon.mcdevmanagermp.data.common.NetworkState<com.lemon.mcdevmanagermp.data.vo.netease.user.LevelInfoVO> = error("unused")
        }
        val fakeCookieRepo = object : com.lemon.mcdevmanagermp.domain.account.CookieRepository {
            override fun getAllCookiesMap(): Map<String, String> = mapOf("token" to "abc")
            override fun addCookie(key: String, value: String) {}
            override fun clearCookies() {}
        }

        val useCase = com.lemon.mcdevmanagermp.domain.account.SaveAccountUseCase(
            accountRepository = fakeAccountRepo,
            userRepository = fakeUserRepo,
            cookieRepository = fakeCookieRepo
        )

        kotlinx.coroutines.runBlocking {
            // Remember password = true
            useCase(email = "test@example.com", password = "secretPassword", rememberPassword = true)
            val saved1 = fakeAccountRepo.getAccountByNickname("TestUser")
            kotlin.test.assertNotNull(saved1)
            kotlin.test.assertEquals("test@example.com", saved1.email)
            kotlin.test.assertEquals("secretPassword", saved1.password)
            kotlin.test.assertTrue(saved1.rememberPassword)

            // Remember password = false
            useCase(email = "test@example.com", password = "secretPassword", rememberPassword = false)
            val saved2 = fakeAccountRepo.getAccountByNickname("TestUser")
            kotlin.test.assertNotNull(saved2)
            kotlin.test.assertEquals("", saved2.password)
            kotlin.test.assertFalse(saved2.rememberPassword)
        }
    }

    @Test
    fun profitMonthComparison() {
        val Jan2026 = com.lemon.mcdevmanagermp.domain.main.ProfitMonth(2026, 1)
        val Dec2025 = com.lemon.mcdevmanagermp.domain.main.ProfitMonth(2025, 12)
        val Feb2026 = com.lemon.mcdevmanagermp.domain.main.ProfitMonth(2026, 2)

        kotlin.test.assertTrue(Dec2025 < Jan2026)
        kotlin.test.assertFalse(Feb2026 < Jan2026)
        kotlin.test.assertFalse(Jan2026 < Jan2026)
    }
}
