package net.av.vtask.security

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.av.vtask.App
import net.av.vtask.ui.composeables.MainActionButton
import net.av.vtask.ui.composeables.OkCancelButton
import net.av.vtask.R
import kotlin.concurrent.thread

fun userNameValidation(userName: String): String {
    return if (userName.isEmpty()) {
        "Fill user name."
    } else {
        ""
    }
}

@Composable
fun UserCreation(onCancel: () -> Unit, onSaved: (userName: String) -> Unit) {
    BackHandler {
        onCancel()
    }
    val newUserName = remember {
        mutableStateOf("")
    }
    Column {
        LoginInput(
            value = newUserName.value,
            onValueChanged = { newUserName.value = it },
            validation = { userNameValidation(newUserName.value).isEmpty() },
            secret = false,
            label = stringResource(R.string.user_name)
        )
        StandardSpacer()
        OkCancelButton(
            onOk = { onSaved(newUserName.value) },
            onCancel = onCancel,
            okButtonEnabled = userNameValidation(newUserName.value).isEmpty(),
            modifier = Modifier.padding(10.dp)
        )
    }
}

@Composable
fun UserSelectionScreen(navigateToHome: () -> Unit, modifier: Modifier = Modifier) {
    IUserProvider.current.init()

    val user = remember {
        mutableStateOf(App.userName)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user.value == null) {
            LoginGreeting(text = stringResource(id = R.string.welcome))
            StandardSpacer()

            val creationMode = remember {
                mutableStateOf(false)
            }
            if (creationMode.value) {
                UserCreation(onCancel = { creationMode.value = false }, onSaved = {
                    user.value = it
                    creationMode.value = false
                })
            } else {
                Column {
                    UsersList(onSelected = { user.value = it })
                    StandardSpacer()
                    Text(text = stringResource(R.string.create_user),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier
                            .clickable { creationMode.value = true }
                            .align(Alignment.CenterHorizontally))
                }
            }
        } else {
            UserAuthenticationScreen(
                userName = user.value!!, switchUser = {
                    App.userName = null
                    user.value = null
                }, navigateToHome = navigateToHome
            )
            BackHandler {
                user.value = null
            }
        }
    }
}

@Composable
fun UsersList(modifier: Modifier = Modifier, onSelected: (String) -> Unit) {
    val users = remember {
        mutableStateListOf(
            elements = IUserProvider.current.getUsers().toTypedArray()
        )
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        users.forEach {
            Box(modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                )
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                )
                .fillMaxWidth()
                .clickable { onSelected(it) }) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(10.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun UserAuthenticationScreen(
    modifier: Modifier = Modifier,
    userName: String,
    navigateToHome: () -> Unit,
    switchUser: () -> Unit
) {
    LaunchedEffect(key1 = userName) {
        App.userName = userName
    }
    val userHandler = UserLogic(userName)


    val userState = remember {
        mutableStateOf(userHandler.determineUserState())
    }

    val refreshUserState = {
        userState.value = userHandler.determineUserState()
    }

    Column(modifier = modifier) {
        LoginGreeting(text = stringResource(id = R.string.welcome_user, userName))
        StandardSpacer()
        when (userState.value) {
            is NoUser -> CreateUser {
                userHandler.createUser(it)
                refreshUserState()
            }

            is NeedPassword -> LoginWithPassword(userState = userState.value as NeedPassword,
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

            is NeedPIN -> LoginWithPIN(userState = userState.value as NeedPIN, tryLogin = {
                val key = userHandler.unlockWithPIN(it)
                if (key == null) {
                    refreshUserState()
                } else {
                    CryptoFactory.key = key
                    navigateToHome()
                }
            })

        }
        StandardSpacer()
        Text(text = stringResource(R.string.switch_user),
            style = MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primaryContainer,
                textDecoration = TextDecoration.Underline
            ),
            modifier = Modifier
                .clickable { switchUser() }
                .align(Alignment.CenterHorizontally))
    }
}

@Composable
fun CreateUser(
    createUser: (password: String) -> Unit
) {
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

    Column {
        LoginInput(
            value = password.value,
            onValueChanged = {
                password.value = it
                showMessage.value = true
            },
            validation = { passwordMessage(password.value).isEmpty() },
            label = stringResource(id = R.string.password)
        )
        if (showMessage.value && passwordMessage(password.value).isNotEmpty()) {
            Text(
                text = passwordMessage(password.value),
                color = Color.Red,
                modifier = Modifier.fillMaxWidth()
            )
        }
        StandardSpacer()
        LoginInput(
            value = confirmPassword.value,
            onValueChanged = { confirmPassword.value = it },
            validation = { confirmPassword.value.contentEquals(password.value) },
            label = stringResource(id = R.string.confirm_password)
        )
        StandardSpacer()
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
            }, text = stringResource(R.string.create), wait = wait.value
        )
    }
}

fun passwordMessage(password: String): String {
    //todo: more checks
    return if (password.isEmpty()) App.appContext.getString(R.string.fill_password) else ""
}

@Composable
fun LoginWithPassword(
    userState: NeedPassword, tryLogin: (password: String, PIN: String?) -> Unit
) {
    Column {
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
                Text(
                    text = stringResource(R.string.your_pinlock_has_been_expired), color = Color.Red
                )
            }

            is PINInvalid -> {
                Text(
                    text = stringResource(R.string.there_was_an_error_with_your_pinlock),
                    color = Color.Red
                )
            }

            is TooManyPINAttempts -> {
                Text(
                    text = stringResource(R.string.too_many_attempts_please_enter_your_password),
                    color = Color.Red
                )
            }

            is NeedPasswordWithAttemptsLeft -> {
                Text(
                    text = stringResource(R.string.remaining_attempts, userState.attemptsLeft),
                    color = Color.Red
                )
            }
        }

        LoginInput(
            value = password.value,
            onValueChanged = { password.value = it },
            validation = { it.isNotEmpty() },
            label = stringResource(id = R.string.password)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = usePIN.value, onCheckedChange = { usePIN.value = it })
            Text(
                text = stringResource(R.string.use_pin_code_for_future_login)
            )
        }
        if (usePIN.value) {
            LoginInput(
                value = PIN.value,
                onValueChanged = { PIN.value = it },
                validation = { validatePIN(it).isEmpty() },
                label = stringResource(id = R.string.enter_your_pin)
            )
            Text(text = validatePIN(PIN.value), color = Color.Red)
        }
        StandardSpacer()
        MainActionButton(
            onClick = {
                if (password.value.isNotEmpty()) {
                    wait.value = true
                    thread {
                        tryLogin(password.value, if (usePIN.value) PIN.value else null)
                    }
                }
            }, text = stringResource(R.string.login), wait = wait.value
        )
    }
}

@Composable
fun LoginWithPIN(
    userState: NeedPIN, tryLogin: (PIN: String) -> Unit
) {
    val PIN = remember {
        mutableStateOf("")
    }

    Column {
        LoginInput(
            value = PIN.value,
            onValueChanged = { PIN.value = it },
            validation = { it.isNotEmpty() },
            label = stringResource(R.string.enter_your_pin)
        )
        if (userState is NeedPINWithAttemptsLeft) {
            Text(
                text = stringResource(id = R.string.remaining_attempts, userState.attemptsLeft),
                color = Color.Red
            )
        }
        StandardSpacer()
        MainActionButton(onClick = {
            if (PIN.value.isNotEmpty()) {
                tryLogin(PIN.value)
            }
        }, text = stringResource(id = R.string.login))
    }
}

fun validatePIN(PIN: String): String {
    return if (PIN.length < 4) {
        App.appContext.getString(R.string.pin_length_must_be_at_least_4)
    } else {
        ""
    }
}

@Composable
fun LoginInput(
    value: String,
    label: String,
    onValueChanged: (newValue: String) -> Unit,
    modifier: Modifier = Modifier,
    validation: (password: String) -> Boolean,
    secret: Boolean = true
) {
    val showPassword = remember {
        mutableStateOf(!secret)
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        TextField(value = value,
            onValueChange = onValueChanged,
            modifier = Modifier
                .border(
                    width = 3.dp,
                    color = if (validation(value)) Color.Green else Color.Red,
                    shape = CircleShape
                )
                .wrapContentHeight()
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape
                )
                .clip(CircleShape),
            visualTransformation = if (showPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = if (secret) {
                {
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
                }
            } else {
                null
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ))
    }
}

@Composable
fun StandardSpacer() {
    Spacer(modifier = Modifier.height(15.dp))
}

@Composable
fun LoginGreeting(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        style = MaterialTheme.typography.displayMedium.copy(
            textMotion = TextMotion.Animated, fontStyle = FontStyle.Italic
        ),
        modifier = modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}