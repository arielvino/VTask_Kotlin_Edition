package net.av.vtask.security

interface IUserProvider {
    companion object {
        val current: IUserProvider = FilesUserProvider
    }

    fun init()
    fun createUser(userName: String, userData: User)
    fun editUser(userName: String, userData: User)
    fun getUser(userName: String): User?
    fun getUsers(): List<String>
    fun deleteUser(userName: String)
}