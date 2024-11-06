package net.av.vtask.ui.composeables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.av.vtask.Collection
import net.av.vtask.IDataItem
import net.av.vtask.IDataItemProvider
import net.av.vtask.IItemWithChildren
import net.av.vtask.Task

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
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        for (id in idList) {
            item {
                DisplayItemInList(id = id,
                    onNavigateInside = {
                        onNavigateTo(id)
                    })
            }
        }
    }
}


@Composable
fun DisplayItemInList(id: String, onNavigateInside: () -> Unit) {
    val item: IDataItem? = IDataItemProvider.current.get(id)
    if (item != null) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onNavigateInside()
            }
            .background(color = MaterialTheme.colorScheme.secondaryContainer)
            .padding(5.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Start
                )
            )
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
        modifier = modifier
            .padding(10.dp)
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
        }
    }
}