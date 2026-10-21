package com.lemon.mcdevmanagermp.utils

import com.lemon.mcdevmanagermp.platform.getPasswordKeyDirectory
import com.lemon.mcdevmanagermp.utils.PasswordCrypto.CIPHER_PREFIX
import dev.whyoleg.cryptography.BinarySize.Companion.bits
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.RSA
import dev.whyoleg.cryptography.algorithms.SHA256
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use
import kotlin.concurrent.Volatile
import kotlin.io.encoding.Base64

/**
 * 记住密码的本地加解密。
 *
 * 数据库只保存 RSA-OAEP(SHA-256) 密文，私钥存放在各平台的应用私有目录中，
 * 与数据库文件分离，数据库被单独读取时无法还原出明文密码。
 *
 * 密文统一以 [CIPHER_PREFIX] 开头，用于和历史版本保存的明文区分：
 * 历史明文仍可正常读出，并在下次保存账号时替换为密文。
 */
object PasswordCrypto {

    private const val CIPHER_PREFIX = "RSA1:"
    private const val KEY_FILE_NAME = "password_key.dat"
    private const val KEY_SIZE_BITS = 2048

    private val mutex = Mutex()

    @Volatile
    private var cachedKey: KeyPairKeys? = null

    private class KeyPairKeys(
        val publicKey: RSA.OAEP.PublicKey,
        val privateKey: RSA.OAEP.PrivateKey
    )

    /** 是否为加密后的存储值（历史明文返回 false）。 */
    fun isEncrypted(stored: String): Boolean = stored.startsWith(CIPHER_PREFIX)

    /**
     * 加密密码。空字符串原样返回，未勾选记住密码时不落库。
     */
    suspend fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        val keys = loadKeys()
        val cipherText = keys.publicKey.encryptor().encrypt(plainText.encodeToByteArray())
        return CIPHER_PREFIX + Base64.encode(cipherText)
    }

    /**
     * 解密密码。
     *
     * @return 明文；存储值为空、或私钥丢失导致无法解密时返回 null。
     *         历史明文存储值原样返回。
     */
    suspend fun decrypt(stored: String): String? {
        if (stored.isEmpty()) return null
        if (!isEncrypted(stored)) return stored
        return runCatching {
            val keys = loadKeys()
            val cipherText = Base64.decode(stored.removePrefix(CIPHER_PREFIX))
            keys.privateKey.decryptor().decrypt(cipherText).decodeToString()
        }.getOrNull()
    }

    private suspend fun loadKeys(): KeyPairKeys {
        cachedKey?.let { return it }
        return mutex.withLock {
            cachedKey ?: createOrRestoreKeys().also { cachedKey = it }
        }
    }

    @OptIn(DelicateCryptographyApi::class)
    private suspend fun createOrRestoreKeys(): KeyPairKeys {
        val rsa = CryptographyProvider.Default.get(RSA.OAEP)
        readStoredKey()?.let { stored ->
            runCatching {
                val privateKey = rsa.privateKeyDecoder(SHA256)
                    .decodeFromByteArray(RSA.PrivateKey.Format.DER.PKCS1, Base64.decode(stored))
                return KeyPairKeys(privateKey.getPublicKey(), privateKey)
            }
        }
        val keyPair = rsa.keyPairGenerator(KEY_SIZE_BITS.bits).generateKey()
        writeStoredKey(
            Base64.encode(keyPair.privateKey.encodeToByteArray(RSA.PrivateKey.Format.DER.PKCS1))
        )
        return KeyPairKeys(keyPair.publicKey, keyPair.privateKey)
    }

    private fun keyPath() = getPasswordKeyDirectory().toPath() / KEY_FILE_NAME

    private fun readStoredKey(): String? = runCatching {
        val path = keyPath()
        if (!FileSystem.SYSTEM.exists(path)) return null
        FileSystem.SYSTEM.source(path).buffer().use { it.readUtf8() }.trim().ifEmpty { null }
    }.getOrNull()

    private fun writeStoredKey(value: String) {
        val path = keyPath()
        FileSystem.SYSTEM.createDirectories(path.parent!!)
        FileSystem.SYSTEM.sink(path).buffer().use { it.writeUtf8(value) }
    }
}
