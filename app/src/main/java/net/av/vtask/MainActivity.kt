package net.av.vtask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.av.vtask.security.UserAuthenticationScreen
import net.av.vtask.ui.composeables.Viewer
import net.av.vtask.ui.composeables.TaskCreatorScreen
import net.av.vtask.ui.theme.VTaskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    VTaskTheme {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "launch", //"display/${IDataItemProvider.rootId[IDataItemProvider.RootGroups.Tasks]}"
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            composable(route = "launch") {
                UserAuthenticationScreen(navigateToHome = { navController.navigate("display/${IDataItemProvider.rootId[IDataItemProvider.RootGroups.Tasks]!!}") })
            }
            composable(route = "display/{id}") { backStackEntry ->
                IDataItemProvider.current.init()

                val id = backStackEntry.arguments?.getString("id")
                    ?: IDataItemProvider.rootId[IDataItemProvider.RootGroups.Tasks]!!
                Viewer(id = id, onNavigateTo = {
                    navController.navigate("display/$it")
                }, onCreateTaskPressed = {
                    navController.navigate("create/$id")
                })
            }
            composable(route = "create/{parentId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("parentId")
                    ?: IDataItemProvider.rootId[IDataItemProvider.RootGroups.Tasks]!!
                TaskCreatorScreen(parentId = id, onExit = {
                    navController.popBackStack()
                })
            }
        }
    }
}

@Composable
fun MainActionButton(
    modifier: Modifier = Modifier, text: String, onClick: () -> Unit, wait: Boolean = false
) {
    Button(
        enabled = !wait,
        onClick = {
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        if (wait) {
                WaitAnimation()
        } else {
            Text(text = text, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
fun WaitAnimation(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier.width(28.dp).aspectRatio(1f),
        color = MaterialTheme.colorScheme.primaryContainer,
        trackColor = MaterialTheme.colorScheme.secondaryContainer,
    )
}