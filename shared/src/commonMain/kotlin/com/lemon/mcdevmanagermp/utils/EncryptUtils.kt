package com.lemon.mcdevmanagermp.utils

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.RSA
import dev.whyoleg.cryptography.algorithms.SHA256
import okio.ByteString.Companion.toByteString
import kotlin.io.encoding.Base64

@OptIn(DelicateCryptographyApi::class)
suspend fun rsaEncrypt(input: String, publicKeyStr: String): String {
    val rsa = CryptographyProvider.Default.get(RSA.PKCS1)

    val keyBytes = Base64.decode(publicKeyStr)
    val publicKey = rsa.publicKeyDecoder(SHA256)
        .decodeFromByteArray(RSA.PublicKey.Format.DER, keyBytes)

    val ciphertext: ByteArray = publicKey.encryptor()
        .encrypt(input.encodeToByteArray())

    return ciphertext.toByteString().base64()
}
