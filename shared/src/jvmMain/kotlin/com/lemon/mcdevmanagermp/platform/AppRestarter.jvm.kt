package com.lemon.mcdevmanagermp.platform

import java.io.File

actual fun restartApp() {
    try {
        val jarPath =
            object {}::class.java.protectionDomain?.codeSource?.location?.toURI()?.path ?: return
        val appDir = File(jarPath).parentFile ?: return
        // jpackage 打包结构：JAR 在 app/ 子目录，发行版根目录是上一级
        val distRoot = appDir.parentFile

        if (distRoot != null) {
            val isWindows =
                System.getProperty("os.name", "").lowercase().contains("win")

            // Windows: 检查是否有待安装的更新（.patch_temp 由 installUpdate 解压准备）
            if (isWindows) {
                val tempDir = File(distRoot, ".patch_temp")
                if (tempDir.exists() && tempDir.isDirectory) {
                    val launcher = findLauncher(distRoot, true)
                    if (launcher != null) {
                        val updateManager = AppUpdateManager()
                        val extractedRoot = updateManager.detectExtractedRoot(tempDir)
                        val script = createWindowsPatchScript(
                            distRoot, extractedRoot, launcher.absolutePath,
                            ProcessHandle.current().pid()
                        )
                        // 启动辅助脚本：等待当前进程退出 → 复制文件 → 启动应用
                        ProcessBuilder("cmd", "/c", script.absolutePath)
                            .directory(distRoot)
                            .start()
                        kotlin.system.exitProcess(0)
                        return
                    }
                }
            }

            // 正常重启（无待安装更新，或非 Windows 平台）
            val launcher = findLauncher(distRoot, isWindows)
            if (launcher != null) {
                ProcessBuilder(launcher.absolutePath)
                    .directory(distRoot)
                    .start()
                kotlin.system.exitProcess(0)
                return
            }

            // 回退：使用打包的 JRE 启动 JAR
            val javaName = if (isWindows) "java.exe" else "java"
            val bundledJava = File(distRoot, "runtime/bin/$javaName")
            if (bundledJava.exists()) {
                ProcessBuilder(bundledJava.absolutePath, "-jar", jarPath)
                    .directory(distRoot)
                    .start()
                kotlin.system.exitProcess(0)
                return
            }
        }

        // 最后兜底：系统 java
        Runtime.getRuntime().exec(arrayOf("java", "-jar", jarPath))
    } catch (_: Exception) {
        // 如果无法启动新进程，直接退出让用户手动重启
    }
    kotlin.system.exitProcess(0)
}

/**
 * 查找原生启动器
 * - Windows: jpackage 结构下 .exe 直接在发行版根目录
 * - Linux/macOS: 根目录下可执行文件
 */
private fun findLauncher(distRoot: File, isWindows: Boolean): File? {
    return if (isWindows) {
        distRoot.listFiles()?.firstOrNull { it.isFile && it.name.endsWith(".exe") }
    } else {
        distRoot.listFiles()?.firstOrNull {
            it.isFile && it.canExecute() && !it.name.contains("java")
        }
    }
}

/**
 * 创建 Windows 辅助脚本，在当前进程退出后完成文件复制和应用重启。
 *
 * 流程：
 * 1. 循环等待当前进程 PID 退出
 * 2. 使用 robocopy 覆盖文件（排除 .data/logs/downloads/.patch_temp）
 * 3. 清理临时目录
 * 4. 启动应用
 * 5. 删除脚本自身
 */
private fun createWindowsPatchScript(
    distRoot: File,
    sourceDir: File,
    launcherPath: String,
    currentPid: Long
): File {
    val script = File(distRoot, ".patch.bat")
    val tempDirPath = File(distRoot, ".patch_temp").absolutePath
    script.writeText(
        """
        @echo off
        chcp 65001 >nul 2>&1
        :wait_loop
        tasklist /fi "PID eq $currentPid" 2>nul | find "$currentPid" >nul
        if %ERRORLEVEL%==0 (
            timeout /t 1 /nobreak >nul
            goto wait_loop
        )
        timeout /t 1 /nobreak >nul
        robocopy "${sourceDir.absolutePath}" "${distRoot.absolutePath}" /E /XD .data logs downloads .patch_temp /NFL /NDL /NJH /NJS /NC /NS >nul
        rd /s /q "$tempDirPath" 2>nul
        start "" "$launcherPath"
        (goto) 2>nul & del "%~f0"
        """.trimIndent()
    )
    return script
}
