package net.av.vtask.security

import java.time.LocalDateTime

class User(
    var passwordSalt: ByteArray?,
    var test: String?,
    var cipherTest: ByteArray?,
    var passwordKeyBuffer: ByteArray?,
    var attemptsLeftTillCooldown: Int?,
    var cooldown: LocalDateTime? = null,
    var lockKeyBuffer: ByteArray? = null,
    var lockAttemptsLeft: Int? = null,
    var lockExpiration: LocalDateTime? = null
)