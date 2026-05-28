package com.lemon.mcdevmanagermp.platform

import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher

actual fun rsaEncrypt(input: String, publicKeyStr: String): String {
    val keyBytes = Base64.getDecoder().decode(publicKeyStr)
    val keySpec = X509EncodedKeySpec(keyBytes)
    val publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec)

    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)

    val encrypted = cipher.doFinal(input.toByteArray(Charsets.UTF_8))
    return Base64.getEncoder().withoutPadding().encodeToString(encrypted)
}
