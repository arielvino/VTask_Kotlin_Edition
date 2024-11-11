package net.av.vtask.ui.composeables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.av.vtask.data.Collection
import net.av.vtask.data.IDataItem
import net.av.vtask.IDataItemProvider
import net.av.vtask.IItemHierarchyManager
import net.av.vtask.data.IItemWithChildren
import net.av.vtask.data.Note
import net.av.vtask.data.Task
import kotlin.concurrent.thread

@Composable
fun CollectionViewer(
    id: String, onNavigateTo: (id: String) -> Unit, modifier: Modifier = Modifier
) {
    val collection = IDataItemProvider.current.get(id) as Collection
    Column(modifier = modifier) {
        ListViewer(idList = collection.children, onNavigateTo = onNavigateTo)
    }
}

@Composable
fun ListViewer(
    idList: MutableList<String>, onNavigateTo: (id: String) -> Unit, modifier: Modifier = Modifier
) {
    val rememberList = remember {
        mutableStateListOf(elements = idList.toTypedArray())
    }
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        for (id in rememberList) {
            item {
                ListItem(id = id, onNavigateInside = {
                    onNavigateTo(id)
                }, onDelete = {
                    IItemHierarchyManager.current.delete(id)
                    rememberList.remove(id)
                })
            }
        }
    }
}


@Composable
fun ListItem(id: String, onNavigateInside: () -> Unit, onDelete: (id: String) -> Unit) {
    val item: IDataItem? = IDataItemProvider.current.get(id)
    if (item != null) {
        Column {
            val approveDelete = remember {
                mutableStateOf(false)
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onNavigateInside()
                }
                .background(color = MaterialTheme.colorScheme.secondaryContainer)
                .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        modifier = Modifier
                            .padding(5.dp)
                            .border(
                                1.5f.dp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                shape = CircleShape
                            )
                            .padding(7.5f.dp),
                        imageVector = when (item) {
                            is Task -> Icons.Filled.Task
                            is Collection -> Icons.Filled.Folder
                            is Note -> Icons.AutoMirrored.Filled.Note
                        },
                        contentDescription = "itemType",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = item.title, style = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Start
                        ), modifier = Modifier.padding(10.dp)
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            approveDelete.value = true
                            thread {
                                Thread.sleep(2000)
                                approveDelete.value = false
                            }
                        }, modifier = Modifier
                            .wrapContentHeight()
                            .background(
                                color = MaterialTheme.colorScheme.background, shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Filled.DeleteForever,
                            contentDescription = "PasswordVisibility",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            if (approveDelete.value) {
                OkCancelButton(onOk = { onDelete(id) }, onCancel = { approveDelete.value = false })
            }
        }
    } else {
        Text(text = "Unavailable", color = Color.Red)
    }

}

@Composable
fun Viewer(
    id: String,
    onNavigateTo: (id: String) -> Unit,
    onCreateTaskPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(10.dp)
    ) {
        val item = IDataItemProvider.current.get(id)
        if (item == null) {
            Text(
                text = "Item unavailable",
                style = MaterialTheme.typography.displaySmall.copy(color = Color.Red)
            )
        } else {
            Text(
                text = item.title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displaySmall
            )
            Box(modifier = Modifier.weight(1f)) {
                if (item is Task) {
                    TaskViewer(id = id, onNavigateTo)
                }
                if (item is Collection) {
                    CollectionViewer(id = id, onNavigateTo = onNavigateTo)
                }
            }
            if (item is IItemWithChildren) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onCreateTaskPressed() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = "Add task",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                ShortCut(
                    modifier = Modifier.weight(1f), onClick = {
                        onNavigateTo(IDataItemProvider.rootId[IDataItemProvider.IndependentId.Root]!!)
                    }, icon = Icons.Filled.AccountTree, contentDescription = "folders"
                )
                ShortCut(
                    modifier = Modifier.weight(1f), onClick = {
                        onNavigateTo(IDataItemProvider.rootId[IDataItemProvider.IndependentId.PendingTasks]!!)
                    }, icon = Icons.Filled.Task, contentDescription = "tasks"
                )
            }
        }
    }
}

@Composable
fun ShortCut(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String
) {
    Box(modifier = modifier
        .background(color = MaterialTheme.colorScheme.secondaryContainer)
        .clickable {
            onClick()
        }) {
        Icon(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(15.dp),
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primaryContainer
        )
    }
}
