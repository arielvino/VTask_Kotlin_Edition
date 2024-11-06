package net.av.vtask.security

import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import kotlin.experimental.xor

object CryptoFactory {
    var key: ByteArray? = null

    private const val KEY_LENGTH = 32 // 256-bit key

    fun aesGcmEncrypt(key: ByteArray, plaintext: ByteArray): ByteArray {
        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), spec)
        val ciphertext = cipher.doFinal(plaintext)
        return iv + ciphertext
    }

    fun aesGcmEncrypt(key: ByteArray, plaintext: String): ByteArray {
        return aesGcmEncrypt(key, plaintext.toByteArray())
    }

    fun aesGcmDecrypt(key: ByteArray, ciphertext: ByteArray): ByteArray {
        val iv = ciphertext.copyOfRange(0, 12)
        val encryptedData = ciphertext.copyOfRange(12, ciphertext.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), spec)
        return cipher.doFinal(encryptedData)
    }

    fun aesGcmDecryptAsString(key: ByteArray, ciphertext: ByteArray): String {
        return String(aesGcmDecrypt(key, ciphertext))
    }

    fun pbkdf2(
        password: String,
        salt: ByteArray,
        iterations: Int = 600000,
        keyLength: Int = 256
    ): ByteArray {
        try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
            val key = factory.generateSecret(spec)
            return key.encoded
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalArgumentException("PBKDF2WithHmacSHA256 algorithm not available", e)
        } catch (e: InvalidKeySpecException) {
            throw IllegalArgumentException("Invalid key specification", e)
        }
    }

    fun sha256(input: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(input)
    }

    fun xor256(buffer1: ByteArray, buffer2: ByteArray): ByteArray {
        require(buffer1.size == KEY_LENGTH && buffer2.size == KEY_LENGTH) { "Buffers must be 256-bit (32 bytes) long" }
        return ByteArray(KEY_LENGTH) { buffer1[it] xor buffer2[it] }
    }
}
