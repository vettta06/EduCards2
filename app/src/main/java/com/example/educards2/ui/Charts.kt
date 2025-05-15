package com.example.educards2.ui
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.educards2.database.DailyStat
import com.example.educards2.database.MonthlyStat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.forEachIndexed


@Composable
fun LineChart(
    data: List<DailyStat>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    Canvas(modifier = modifier) {
        val maxY = data.maxOfOrNull { it.count }?.toFloat() ?: 1f
        val barWidth = size.width / data.size
        val height = size.height
        val padding = 16.dp.toPx()

        data.forEachIndexed { index, stat ->
            val barHeight = stat.count.toFloat() / maxY * (height - padding * 2)
            drawRect(
                color = lineColor,
                topLeft = Offset(index * barWidth + padding, height - barHeight - padding),
                size = Size(barWidth - 2.dp.toPx(), barHeight)
            )
        }

        drawContext.canvas.nativeCanvas.apply {
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 12.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
            }
            data.forEachIndexed { index, _ ->
                if (index % 3 == 0) {
                    drawText(
                        "${index + 1}",
                        index * barWidth + barWidth/2 + padding,
                        height - 8.dp.toPx(),
                        textPaint
                    )
                }
            }
            val yStep = maxY / 2
            listOf(0f, yStep, maxY).forEach { value ->
                val yPos = height - (value/maxY * (height - padding * 2)) - padding
                drawText(
                    "%.0f".format(value),
                    8.dp.toPx(),
                    yPos + 4.dp.toPx(),
                    textPaint
                )
            }
        }
    }
}

@Composable
fun BarChart(
    data: List<MonthlyStat>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val months = arrayOf(
        "Янв", "Фев", "Мар",
        "Апр", "Май", "Июн",
        "Июл", "Авг", "Сен",
        "Окт", "Ноя", "Дек"
    )

    if (data.isEmpty()) return

    Canvas(modifier = modifier) {
        val maxY = data.maxOfOrNull { it.total.toFloat() }?.takeIf { it > 0 } ?: 1f
        val padding = 32.dp.toPx()
        val barSpacing = 4.dp.toPx()
        val graphHeight = size.height - 2 * padding
        val barWidth = (size.width - 2 * padding - barSpacing * (data.size - 1)) / data.size
        data.forEachIndexed { index, stat ->
            val barHeight = (stat.total / maxY * graphHeight)
            val x = padding + (barWidth + barSpacing) * index
            val y = padding + graphHeight - barHeight
            drawRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )
        }

        drawContext.canvas.nativeCanvas.apply {
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 12.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
            }

            data.forEachIndexed { index, stat ->
                val monthNumber = try {
                    SimpleDateFormat("yyyy-MM", Locale.US)
                        .parse(stat.month)
                        ?.let { date ->
                            val calendar = Calendar.getInstance()
                            calendar.time = date
                            calendar.get(Calendar.MONTH) // 0-11
                        } ?: 0
                } catch (e: Exception) {
                    0
                }

                val monthName = months.getOrElse(monthNumber) { "" }

                drawContext.canvas.nativeCanvas.drawText(
                    monthName,
                    padding + (barWidth + barSpacing) * index + barWidth/2,
                    size.height - 8.dp.toPx(),
                    textPaint
                )
            }

            val yStep = maxY / 2
            listOf(0f, yStep, maxY).forEach { value ->
                val yPos = padding + graphHeight - (value/maxY * graphHeight)
                drawText(
                    "%.0f".format(value),
                    8.dp.toPx(),
                    yPos + 4.dp.toPx(),
                    textPaint
                )
            }
        }
    }
}