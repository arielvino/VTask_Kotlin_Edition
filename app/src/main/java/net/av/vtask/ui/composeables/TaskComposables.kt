package net.av.vtask.ui.composeables

import androidx.annotation.IntRange
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.av.vtask.IDataItemProvider
import net.av.vtask.IItemHierarchyManager
import net.av.vtask.Task

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
    onSave: (Task) -> Unit, task: Task = Task("New task 1", "", 0, 0, 0, 0)
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

    val urgency = rememberSaveable {
        mutableIntStateOf(rememberedTask.urgency)
    }
    val familiarity = rememberSaveable {
        mutableIntStateOf(rememberedTask.familiarity)
    }
    val motivation = rememberSaveable {
        mutableIntStateOf(rememberedTask.motivation)
    }
    val deterrence = rememberSaveable {
        mutableIntStateOf(rememberedTask.deterrence)
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
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(5.dp))
        ColorSlider(label = "Urgency", initialValue = urgency.intValue) {
            urgency.intValue = it
            rememberedTask.urgency = it
        }
        Spacer(modifier = Modifier.height(5.dp))
        ColorSlider(
            label = "Familiarity", initialValue = familiarity.intValue, positiveProperty = true
        ) {
            familiarity.intValue = it
            rememberedTask.familiarity = it
        }
        Spacer(modifier = Modifier.height(5.dp))
        ColorSlider(
            label = "Motivation", initialValue = motivation.intValue, positiveProperty = true
        ) {
            motivation.intValue = it
            rememberedTask.motivation = it
        }
        Spacer(modifier = Modifier.height(5.dp))
        ColorSlider(label = "Deterrence", initialValue = deterrence.intValue) {
            deterrence.intValue = it
            rememberedTask.deterrence = it
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(text = "Content:", color = MaterialTheme.colorScheme.onBackground)
        TextField(modifier = Modifier
            .padding(10.dp)
            .weight(1f)
            .fillMaxWidth(),
            value = content.value,
            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Justify),
            onValueChange = {
                content.value = it
                rememberedTask.content = it
            })
        Spacer(modifier = Modifier.height(5.dp))
        Button(modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            onClick = {
                onSave(rememberedTask)
            }) {
            Text(text = "Save", color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
fun TaskCreatorScreen(parentId: String, onExit: () -> Unit) {
    TaskEditor(onSave = {
        IItemHierarchyManager.current.create(it, parentId)
        onExit()
    })
}

@Composable
fun TaskEditorScreen(id: String, onExit: () -> Unit) {
    TaskEditor(task = IDataItemProvider.current.get(id) as Task, onSave = {
        IDataItemProvider.current.edit(id, it)
        onExit()
    })
}

@Composable
fun TaskViewer(
    id: String,
    onNavigateTo: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val task = IDataItemProvider.current.get(id)

    if (task != null) {
        task as Task
        Column(
            modifier = modifier
                .background(color = MaterialTheme.colorScheme.background)
                .wrapContentHeight()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                PropertyColorDisplay(label = "Urgency", value = task.urgency, positiveProperty = false)
                PropertyColorDisplay(label = "Familiarity", value = task.familiarity, positiveProperty = true)
                PropertyColorDisplay(label = "Motivation", value = task.motivation, positiveProperty = true)
                PropertyColorDisplay(label = "Deterrence", value = task.deterrence, positiveProperty = false)
            }
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
        Box(
            modifier = Modifier
                .height(20.dp)
                .width(20.dp)
                .background(color = determineColor(value, positiveProperty))
                .align(Alignment.CenterHorizontally)
        )
    }
}