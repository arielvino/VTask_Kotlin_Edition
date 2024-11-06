package net.av.vtask.security

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.av.vtask.MainActionButton
import kotlin.concurrent.thread

@Composable
fun UserAuthenticationScreen(
    modifier: Modifier = Modifier, userName: String = "User1", navigateToHome: () -> Unit
) {
    IUserProvider.current.init()
    val userHandler = UserLogic(userName)

    val userState = remember {
        mutableStateOf(userHandler.determineUserState())
    }

    val refreshUserState = {
        userState.value = userHandler.determineUserState()
    }

    Box(modifier = modifier) {
        when (userState.value) {
            is NoUser -> CreateUser(userName) {
                userHandler.createUser(it)
                refreshUserState()
            }

            is NeedPassword -> LoginWithPassword(userName = userName,
                userState = userState.value as NeedPassword,
                tryLogin = { password: String, PIN: String? ->
                    val key = userHandler.unlockWithPassword(password)
                    if (key == null) {
                        refreshUserState()
                    } else {
                        CryptoFactory.key = key
                        if (PIN != null) {
                            userHandler.setupPINLock(PIN, key)
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            navigateToHome()
                        }
                    }
                })

            is NeedPIN -> LoginWithPIN(userName = userName,
                userState = userState.value as NeedPIN,
                tryLogin = {
                    val key = userHandler.unlockWithPIN(it)
                    if (key == null) {
                        refreshUserState()
                    } else {
                        CryptoFactory.key = key
                        navigateToHome()
                    }
                })

        }
    }
}

@Composable
fun CreateUser(
    userName: String, createUser: (password: String) -> Unit
) {
    LoginFrame {
        val password = rememberSaveable {
            mutableStateOf("")
        }
        val confirmPassword = rememberSaveable {
            mutableStateOf("")
        }
        val showMessage = rememberSaveable {
            mutableStateOf(false)
        }

        val wait = remember {
            mutableStateOf(false)
        }

        Text(
            text = "Password:",
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.fillMaxWidth()
        )
        PasswordInput(value = password.value, onValueChanged = {
            password.value = it
            showMessage.value = true
        }, validation = { passwordMessage(password.value).isEmpty() })
        if (showMessage.value && passwordMessage(password.value).isNotEmpty()) {
            Text(
                text = passwordMessage(password.value),
                color = Color.Red,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = "Confirm password:",
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.fillMaxWidth()
        )
        PasswordInput(value = confirmPassword.value,
            onValueChanged = { confirmPassword.value = it },
            validation = { confirmPassword.value.contentEquals(password.value) })
        Spacer(modifier = Modifier.height(15.dp))
        MainActionButton(
            onClick = {
                if (passwordMessage(
                        password.value
                    ).contentEquals("") && password.value.contentEquals(
                        confirmPassword.value
                    )
                ) {
                    wait.value = true
                    thread { createUser(password.value) }
                }
            }, text = "Create", wait = wait.value
        )
    }
}

fun passwordMessage(password: String): String {
    //todo: more checks
    return if (password.isEmpty()) "Fill password" else ""
}

@Composable
fun LoginWithPassword(
    userName: String, userState: NeedPassword, tryLogin: (password: String, PIN: String?) -> Unit
) {
    LoginFrame {
        val password = remember {
            mutableStateOf("")
        }

        val usePIN = rememberSaveable {
            mutableStateOf(false)
        }

        val PIN = remember {
            mutableStateOf("")
        }

        val wait = remember {
            mutableStateOf(false)
        }

        LaunchedEffect(key1 = userState) {
            wait.value = false
        }

        when (userState) {
            is PINExpired -> {
                Text(text = "Your pinLock has been expired.", color = Color.Red)
            }

            is PINInvalid -> {
                Text(text = "There was an error with your pinLock.", color = Color.Red)
            }

            is TooManyPINAttempts -> {
                Text(text = "Too many attempts. Please enter your password.", color = Color.Red)
            }

            is NeedPasswordWithAttemptsLeft -> {
                Text(text = "Remaining attempts: ${userState.attemptsLeft}.", color = Color.Red)
            }
        }

        Text(text = "Password", modifier = Modifier.fillMaxWidth())
        PasswordInput(value = password.value,
            onValueChanged = { password.value = it },
            validation = { it.isNotEmpty() })
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = usePIN.value, onCheckedChange = { usePIN.value = it })
            Text(
                text = "Use PIN code for future login"
            )
        }
        if (usePIN.value) {
            PasswordInput(value = PIN.value,
                onValueChanged = { PIN.value = it },
                validation = { validatePIN(it).isEmpty() })
            Text(text = validatePIN(PIN.value), color = Color.Red)
        }
        Spacer(modifier = Modifier.height(15.dp))
        MainActionButton(
            onClick = {
                if (password.value.isNotEmpty()) {
                    wait.value = true
                    thread {
                        tryLogin(password.value, if (usePIN.value) PIN.value else null)
                    }
                }
            }, text = "Login", wait = wait.value
        )
    }
}

@Composable
fun LoginWithPIN(
    userName: String, userState: NeedPIN, tryLogin: (PIN: String) -> Unit
) {
    val PIN = remember {
        mutableStateOf("")
    }

    LoginFrame {
        Text(text = "Enter your PIN:", Modifier.fillMaxWidth())
        PasswordInput(value = PIN.value,
            onValueChanged = { PIN.value = it },
            validation = { it.isNotEmpty() })
        if (userState is NeedPINWithAttemptsLeft) {
            Text(text = "Remaining attempts: ${userState.attemptsLeft}", color = Color.Red)
        }
        Spacer(modifier = Modifier.height(15.dp))
        MainActionButton(onClick = {
            if (PIN.value.isNotEmpty()) {
                tryLogin(PIN.value)
            }
        }, text = "Login")
    }
}

@Composable
fun LoginFrame(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.secondaryContainer)
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

fun validatePIN(PIN: String): String {
    return if (PIN.length < 4) {
        "PIN length must be at least 4."
    } else {
        ""
    }
}

@Composable
fun PasswordInput(
    value: String,
    onValueChanged: (newValue: String) -> Unit,
    modifier: Modifier = Modifier,
    validation: (password: String) -> Boolean
) {
    val showPassword = remember {
        mutableStateOf(false)
    }

    TextField(
        value = value,
        onValueChange = onValueChanged,
        modifier = modifier.loginInputFieldModifier(validation(value)),
        visualTransformation = if (showPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val (icon, iconColor) = if (showPassword.value) {
                Pair(
                    Icons.Filled.Visibility, MaterialTheme.colorScheme.primaryContainer
                )
            } else {
                Pair(
                    Icons.Filled.VisibilityOff, Color(0xff666666)
                )
            }

            IconButton(onClick = { showPassword.value = !showPassword.value }) {
                Icon(
                    icon, contentDescription = "PasswordVisibility", tint = iconColor
                )
            }
        },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun Modifier.loginInputFieldModifier(valid: Boolean): Modifier {
    return this.then(
        Modifier
            .border(
                width = 3.dp, color = if (valid) Color.Green else Color.Red, shape = CircleShape
            )
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.secondaryContainer)
            .clip(CircleShape)
    )
}