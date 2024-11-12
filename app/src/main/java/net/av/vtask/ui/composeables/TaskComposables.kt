package net.av.vtask.ui.composeables

import androidx.annotation.IntRange
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.av.vtask.IDataItemProvider
import net.av.vtask.IItemHierarchyManager
import net.av.vtask.R
import net.av.vtask.data.Task
import net.av.vtask.security.StandardSpacer
import net.av.vtask.ui.theme.VTaskTheme

@Composable
fun ColorSlider(
    label: String,
    @IntRange(0, 255) initialValue: Int,
    modifier: Modifier = Modifier,
    positiveProperty: Boolean = false,
    onValueChanged: (newValue: Int) -> Unit
) {
    Column(modifier = modifier.background(color = MaterialTheme.colorScheme.background)) {
        Text(text = label, color = MaterialTheme.colorScheme.onBackground)
        Slider(
            modifier = Modifier.padding(10.dp, 0.dp),
            value = initialValue.toFloat(),
            onValueChange = { newValue ->
                onValueChanged(newValue.toInt())
            },
            valueRange = 0f..255f,
        )

        Box(
            modifier = Modifier
                .width(40.dp)
                .height(10.dp)
                .align(Alignment.CenterHorizontally)
                .background(color = determineColor(initialValue, positiveProperty))
        )

    }
}

fun determineColor(value: Int, positiveProperty: Boolean): Color {
    val useValue = if (positiveProperty) 255 - value else value
    return Color(
        if (useValue <= 127) useValue * 2 else 255,
        if (useValue <= 128) 255 else 2 * (255 - useValue),
        0,
        255
    )
}

@Composable
fun TaskEditor(
    onSave: (task: Task, makeParentAwait: Boolean) -> Unit,
    task: Task = Task(stringResource(R.string.default_task_title), "", id = "")
) {
    val rememberedTask = remember {
        task
    }

    val title = rememberSaveable {
        mutableStateOf(rememberedTask.title)
    }
    val content = rememberSaveable {
        mutableStateOf(rememberedTask.content)
    }

    val status = rememberSaveable {
        mutableStateOf(rememberedTask.status)
    }

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(10.dp),
    ) {
        Text(text = "Title", color = MaterialTheme.colorScheme.onBackground)
        TextField(
            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
            value = title.value,
            onValueChange = {
                title.value = it
                rememberedTask.title = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    3.dp, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.large
                ),
            trailingIcon = {
                IconButton(onClick = { title.value = "" }) {
                    Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear title")
                }
            },
            shape = MaterialTheme.shapes.large,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        StandardSpacer()

        val expanded = remember {
            mutableStateOf(false)
        }
        Row(modifier = Modifier
            .clickable {
                expanded.value = !expanded.value
            }
            .border(
                width = 1.dp, color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "expandStatusList",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = status.value.name,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(10.dp, 0.dp)
            )
        }
        if (expanded.value) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Task.Status.entries.forEach {
                    Box(modifier = Modifier
                        .clickable {
                            status.value = it
                            rememberedTask.status = it
                            expanded.value = false
                        }
                        .background(color = if (it == status.value) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer)
                        .fillMaxWidth()) {
                        Text(text = it.name)
                    }
                }
            }
        }
        val makeParentAwait = rememberSaveable {
            mutableStateOf(false)
        }
        if (status.value == Task.Status.Pending) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = makeParentAwait.value,
                    onCheckedChange = { makeParentAwait.value = it })
                Text(text = "Make parent await", color = MaterialTheme.colorScheme.onBackground)
            }
        }
        StandardSpacer()
        Text(text = "Content:", color = MaterialTheme.colorScheme.onBackground)
        TextField(modifier = Modifier
            .padding(10.dp)
            .weight(1f)
            .fillMaxWidth()
            .border(3.dp, MaterialTheme.colorScheme.primaryContainer),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            value = content.value,
            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Justify),
            onValueChange = {
                content.value = it
                rememberedTask.content = it
            })
        StandardSpacer()
        Button(modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            onClick = {
                onSave(rememberedTask, makeParentAwait.value)
            }) {
            Text(text = "Save", color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Preview
@Composable
fun TaskCreationPreview() {
    VTaskTheme {
        TaskCreatorScreen(parentId = "") {}
    }
}

@Composable
fun TaskCreatorScreen(parentId: String, onExit: () -> Unit) {
    TaskEditor(onSave = { task, makeParentAwait ->
        val id = IItemHierarchyManager.current.create(task, parentId)
        if (makeParentAwait) {
            IItemHierarchyManager.current.makeParentAwait(id)
        }
        onExit()
    })
}

@Composable
fun TaskEditorScreen(id: String, onExit: () -> Unit) {
    TaskEditor(task = IDataItemProvider.current.get(id) as Task, onSave = { task, makeParentAwait ->
        IItemHierarchyManager.current.edit(task, id)
        if (makeParentAwait) {
            IItemHierarchyManager.current.makeParentAwait(id)
        }
        onExit()
    })
}

@Composable
fun TaskViewer(
    id: String,
    onNavigateTo: (id: String) -> Unit,
    onEditPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val task = IDataItemProvider.current.get(id)

    if (task != null) {
        task as Task
        StandardSpacer()
        Column(
            modifier = modifier
                .background(color = MaterialTheme.colorScheme.background)
                .wrapContentHeight()
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.large
                    )
                    .padding(10.dp, 0.dp)
            ) {
                Text(text = task.status.name, color = MaterialTheme.colorScheme.onBackground)
            }
            StandardSpacer()
            IconButton(onClick = { onEditPressed() }, colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit task",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            StandardSpacer()
            Text(
                text = task.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(10.dp)
            )
            ListViewer(idList = task.children, onNavigateTo = onNavigateTo)
        }
    } else {
        Text(text = "Item unavailable", color = Color.Red)
    }
}

@Composable
fun PropertyColorDisplay(
    label: String,
    @IntRange(0, 255) value: Int,
    modifier: Modifier = Modifier,
    positiveProperty: Boolean
) {
    Column(modifier = modifier.padding(5.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onBackground)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .height(20.dp)
                .width(20.dp)
                .background(color = determineColor(value, positiveProperty))
                .align(Alignment.CenterHorizontally)
        )
    }
}