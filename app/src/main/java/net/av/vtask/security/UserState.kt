package net.av.vtask.security

abstract class UserState

class NoUser : UserState()
open class NeedPassword : UserState()
class NeedPasswordWithAttemptsLeft(val attemptsLeft: Int) : NeedPassword()
class PINExpired : NeedPassword()
class PINInvalid : NeedPassword()
class TooManyPINAttempts : NeedPassword()
open class NeedPIN : UserState()
class NeedPINWithAttemptsLeft(val attemptsLeft: Int) : NeedPIN()
class PasswordCoolDown(val remainingSeconds: Int) : UserState()
class ErrorWithUser(val message: String = "") : UserState()