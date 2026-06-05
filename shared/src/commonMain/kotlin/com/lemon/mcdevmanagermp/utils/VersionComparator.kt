package com.lemon.mcdevmanagermp.utils

object VersionComparator {
    fun compare(local: String, remote: String): Int {
        val localClean = local.trimStart('v')
        val remoteClean = remote.trimStart('v')

        val localParts = localClean.split('.').map { it.toIntOrNull() ?: 0 }
        val remoteParts = remoteClean.split('.').map { it.toIntOrNull() ?: 0 }

        val maxLen = maxOf(localParts.size, remoteParts.size)
        for (i in 0 until maxLen) {
            val l = localParts.getOrElse(i) { 0 }
            val r = remoteParts.getOrElse(i) { 0 }
            if (r != l) return r - l
        }
        return 0
    }

    fun needsUpdate(localVersion: String, remoteTag: String): Boolean {
        return compare(localVersion, remoteTag) > 0
    }
}
