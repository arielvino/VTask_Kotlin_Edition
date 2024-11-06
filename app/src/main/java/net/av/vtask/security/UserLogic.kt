package net.av.vtask.security

import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Base64

class UserLogic(private val userName: String) {
    companion object {
        const val PASSWORD_ATTEMPTS_TILL_COOLDOWN = 10
        const val SECONDS_DURATION_OF_PASSWORD_COOLDOWN = 30
        const val LOCK_ATTEMPTS_TILL_DELETED = 10
        const val DAYS_TILL_LOCK_EXPIRATION = 30
    }

    fun createUser(password: String) {
        val buffer = ByteArray(32)
        val salt = ByteArray(32)
        val testBytes = ByteArray(64)
        SecureRandom().nextBytes(buffer)
        SecureRandom().nextBytes(salt)
        SecureRandom().nextBytes(testBytes)
        val test = Base64.getEncoder().encodeToString(testBytes)

        val derivedBytes = CryptoFactory.pbkdf2(password, salt)
        val key = CryptoFactory.xor256(derivedBytes, buffer)
        val cipherTest = CryptoFactory.aesGcmEncrypt(key, test)

        IUserProvider.current.createUser(
            userName, User(
                salt, test, cipherTest, buffer, PASSWORD_ATTEMPTS_TILL_COOLDOWN
            )
        )
    }

    fun setupPINLock(PIN: String, key: ByteArray) {
        val hashedPIN = CryptoFactory.sha256(PIN.toByteArray())
        val buffer = CryptoFactory.xor256(key, hashedPIN)

        val userData = IUserProvider.current.getUser(userName)!!
        userData.lockKeyBuffer = buffer
        userData.lockAttemptsLeft = LOCK_ATTEMPTS_TILL_DELETED
        userData.lockExpiration = LocalDateTime.now().plusDays(DAYS_TILL_LOCK_EXPIRATION.toLong())
        IUserProvider.current.editUser(userName, userData)
    }

    fun deletePIN() {
        val userData = IUserProvider.current.getUser(userName)!!
        userData.lockKeyBuffer = null
        userData.lockAttemptsLeft = null
        userData.lockExpiration = null
        IUserProvider.current.editUser(userName, userData)
    }

    fun unlockWithPIN(PIN: String): ByteArray? {
        val userData = IUserProvider.current.getUser(userName)!!
        if (userData.lockKeyBuffer != null && userData.lockAttemptsLeft != null && userData.lockExpiration != null) {
            if (userData.lockAttemptsLeft!! > 0 && userData.lockExpiration!!.isAfter(LocalDateTime.now())) {
                val key = CryptoFactory.xor256(
                    userData.lockKeyBuffer!!, CryptoFactory.sha256(PIN.toByteArray())
                )
                try {
                    if (userData.test.contentEquals(
                            CryptoFactory.aesGcmDecryptAsString(
                                key, userData.cipherTest!!
                            )
                        )
                    ) {
                        setupPINLock(PIN, key)
                        return key
                    } else throw Exception()
                } catch (e: Exception) {
                    userData.lockAttemptsLeft = userData.lockAttemptsLeft!! - 1
                    IUserProvider.current.editUser(userName, userData)
                    if (userData.lockAttemptsLeft!! < 1) {
                        userData.lockKeyBuffer = ByteArray(32)
                        IUserProvider.current.editUser(userName, userData)
                    }
                    return null
                }
            }
            deletePIN()
        }
        return null
    }

    fun unlockWithPassword(password: String): ByteArray? {
        val userData = IUserProvider.current.getUser(userName)!!
        if (userData.attemptsLeftTillCooldown!! > 0) {
            if (userData.cooldown?.isAfter(LocalDateTime.now()) == true) {
                return null
            }
            val derivedBytes = CryptoFactory.pbkdf2(password, userData.passwordSalt!!)
            val key = CryptoFactory.xor256(derivedBytes, userData.passwordKeyBuffer!!)
            try {
                if (userData.test!!.contentEquals(
                        CryptoFactory.aesGcmDecryptAsString(
                            key, userData.cipherTest!!
                        )
                    )
                ) {
                    userData.attemptsLeftTillCooldown = PASSWORD_ATTEMPTS_TILL_COOLDOWN
                    IUserProvider.current.editUser(userName, userData)
                    return key
                } else throw Exception()
            } catch (e: Exception) {
                userData.attemptsLeftTillCooldown = userData.attemptsLeftTillCooldown!! - 1
                IUserProvider.current.editUser(userName, userData)
                if (userData.attemptsLeftTillCooldown!! < 1) {
                    userData.cooldown = LocalDateTime.now()
                        .plusSeconds(SECONDS_DURATION_OF_PASSWORD_COOLDOWN.toLong())
                    userData.attemptsLeftTillCooldown = PASSWORD_ATTEMPTS_TILL_COOLDOWN
                    IUserProvider.current.editUser(userName, userData)
                }
            }
        }
        return null
    }

    fun determineUserState(): UserState {
        val userData = IUserProvider.current.getUser(userName)
        if (userData == null) {
            return NoUser()
        } else {
            if (userData.passwordSalt == null || userData.test == null || userData.cipherTest == null || userData.passwordKeyBuffer == null || userData.attemptsLeftTillCooldown == null) {
                return ErrorWithUser("Some property was null.")
            } else {
                val deleteLock = {
                    userData.lockKeyBuffer = null
                    userData.lockAttemptsLeft = null
                    userData.lockExpiration = null
                    IUserProvider.current.editUser(userName, userData)
                }

                //all lock data exist:
                if (userData.lockKeyBuffer != null && userData.lockAttemptsLeft != null && userData.lockExpiration != null) {
                    if (userData.lockExpiration!!.isBefore(LocalDateTime.now())) {
                        deleteLock()
                        return PINExpired()
                    }
                    if (userData.lockAttemptsLeft!! < 1) {
                        deleteLock()
                        return TooManyPINAttempts()
                    }
                    return if (userData.lockAttemptsLeft in 1.until(LOCK_ATTEMPTS_TILL_DELETED)) {
                        NeedPINWithAttemptsLeft(userData.lockAttemptsLeft!!)
                    } else {
                        NeedPIN()
                    }
                } else {
                    //partial lock data exist - invalid:
                    if (userData.lockKeyBuffer != null || userData.lockAttemptsLeft != null || userData.lockExpiration != null) {
                        deleteLock()
                        return PINInvalid()
                    }
                    //lock data not exist:
                    else {
                        if (userData.cooldown != null) {
                            if (userData.cooldown!!.isAfter(LocalDateTime.now())) {
                                userData.attemptsLeftTillCooldown = 0
                                IUserProvider.current.editUser(userName, userData)
                                return PasswordCoolDown(
                                    ChronoUnit.SECONDS.between(
                                        LocalDateTime.now(), userData.cooldown
                                    ).toInt()
                                )
                            } else {
                                userData.cooldown = null
                                userData.attemptsLeftTillCooldown = PASSWORD_ATTEMPTS_TILL_COOLDOWN
                                IUserProvider.current.editUser(userName, userData)
                            }
                        }
                        return if (userData.attemptsLeftTillCooldown == PASSWORD_ATTEMPTS_TILL_COOLDOWN) {
                            NeedPassword()
                        } else {
                            NeedPasswordWithAttemptsLeft(userData.attemptsLeftTillCooldown!!)
                        }
                    }
                }
            }
        }
    }
}