package net.av.vtask.security

import net.av.vtask.App
import java.io.File
import java.time.LocalDateTime

interface IUserProvider {
    companion object {
        val current: IUserProvider = object : IUserProvider {
            val usersDirectoryPath = "${App.appContext.filesDir.absolutePath}/users"
            override fun init() {
                File(usersDirectoryPath).mkdir()
            }

            override fun createUser(userName: String, userData: User) {
                val userDirectory = File("$usersDirectoryPath/$userName")
                if (!userDirectory.exists()) {
                    userDirectory.mkdirs()
                    editUser(userName, userData)
                }
            }

            override fun editUser(userName: String, userData: User) {
                val userDirectory = File("$usersDirectoryPath/$userName")
                if (userDirectory.exists()) {
                    File(userDirectory.absolutePath + "/passwordSalt").writeBytes(userData.passwordSalt!!)
                    File(userDirectory.absolutePath + "/test").writeText(userData.test!!)
                    File(userDirectory.absolutePath + "/cipherTest").writeBytes(userData.cipherTest!!)
                    File(userDirectory.absolutePath + "/passwordKeyBuffer").writeBytes(userData.passwordKeyBuffer!!)
                    File(userDirectory.absolutePath + "/attemptsLeftTillCooldown").writeText(
                        userData.attemptsLeftTillCooldown!!.toString()
                    )
                    userData.cooldown?.let {
                        File(userDirectory.absolutePath + "/cooldown").writeText(
                            it.toString()
                        )
                    } ?: File(userDirectory.absolutePath + "/cooldown").delete()
                    userData.lockKeyBuffer?.let {
                        File(userDirectory.absolutePath + "/lockKeyBuffer").writeBytes(
                            it
                        )
                    } ?: File(userDirectory.absolutePath + "/lockKeyBuffer").delete()
                    userData.lockAttemptsLeft?.let {
                        File(userDirectory.absolutePath + "/lockAttemptsLeft").writeText(
                            it.toString()
                        )
                    } ?: File(userDirectory.absolutePath + "/lockAttemptsLeft").delete()
                    userData.lockExpiration?.let {
                        File(userDirectory.absolutePath + "/lockExpiration").writeText(
                            it.toString()
                        )
                    } ?: File(userDirectory.absolutePath + "/lockExpiration").delete()
                }
            }

            override fun getUser(userName: String): User? {
                val userDirectory = File("$usersDirectoryPath/$userName")
                if (userDirectory.exists()) {
                    val passwordSalt = try {
                        userDirectory.resolve("passwordSalt").readBytes()
                    } catch (e: Exception) {
                        null
                    }
                    val test = try {
                        userDirectory.resolve("test").readText()
                    } catch (e: Exception) {
                        null
                    }
                    val cipherTest = try {
                        userDirectory.resolve("cipherTest").readBytes()
                    } catch (e: Exception) {
                        null
                    }
                    val passwordKeyBuffer = try {
                        userDirectory.resolve("passwordKeyBuffer").readBytes()
                    } catch (e: Exception) {
                        null
                    }
                    val attemptsLeftTillCooldown = try {
                        userDirectory.resolve("attemptsLeftTillCooldown").readText().toInt()
                    } catch (e: Exception) {
                        null
                    }
                    val cooldown = try {
                        LocalDateTime.parse(userDirectory.resolve("cooldown").readText())
                    } catch (e: Exception) {
                        null
                    }
                    val lockKeyBuffer = try {
                        userDirectory.resolve("lockKeyBuffer").readBytes()
                    } catch (e: Exception) {
                        null
                    }
                    val lockAttemptsLeft = try {
                        userDirectory.resolve("lockAttemptsLeft").readText().toInt()
                    } catch (e: Exception) {
                        null
                    }
                    val lockExpiration = try {
                        LocalDateTime.parse(userDirectory.resolve("lockExpiration").readText())
                    } catch (e: Exception) {
                        null
                    }

                    return User(
                        passwordSalt = passwordSalt,
                        test = test,
                        cipherTest = cipherTest,
                        passwordKeyBuffer = passwordKeyBuffer,
                        attemptsLeftTillCooldown = attemptsLeftTillCooldown,
                        cooldown = cooldown,
                        lockKeyBuffer = lockKeyBuffer,
                        lockAttemptsLeft = lockAttemptsLeft,
                        lockExpiration = lockExpiration
                    )
                }
                return null
            }

            override fun getUsers(): List<String> {
                val users = mutableListOf<String>()
                for (userDirectory in File(usersDirectoryPath).listFiles()!!) {
                    if (userDirectory.isDirectory) {
                        users.add(userDirectory.name)
                    }
                }
                return users.toList()
            }

            override fun deleteUser(userName: String) {
                File("$usersDirectoryPath/$userName").delete()
            }
        }
    }

    fun init()
    fun createUser(userName: String, userData: User)
    fun editUser(userName: String, userData: User)
    fun getUser(userName: String): User?
    fun getUsers(): List<String>
    fun deleteUser(userName: String)
}