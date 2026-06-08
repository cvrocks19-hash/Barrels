package com.taskflow.android.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.room.Room
import com.taskflow.android.data.Status
import com.taskflow.android.data.Task
import com.taskflow.android.data.TaskDatabase
import kotlinx.coroutines.flow.first

class TaskFlowWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db    = Room.databaseBuilder(context, TaskDatabase::class.java, "taskflow.db").build()
        val tasks = db.taskDao().observeDailyFocus().first()
        provideContent { WidgetContent(tasks) }
    }
}

class TaskFlowWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TaskFlowWidget()
}

@Composable
private fun WidgetContent(tasks: List<Task>) {
    Column(GlanceModifier.fillMaxSize().padding(12.dp)) {
        Text(
            "TaskFlow",
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = TextUnit(14f, TextUnitType.Sp)),
        )
        Spacer(GlanceModifier.height(6.dp))
        if (tasks.isEmpty()) {
            Text("All done for today!", style = TextStyle(fontSize = TextUnit(12f, TextUnitType.Sp)))
        } else {
            tasks.take(5).forEach { task ->
                Row(
                    GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val icon = if (task.status == Status.DONE) "●" else "○"
                    Text(
                        "$icon  ${task.title}",
                        style = TextStyle(fontSize = TextUnit(12f, TextUnitType.Sp)),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
