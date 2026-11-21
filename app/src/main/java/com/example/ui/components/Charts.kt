package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalSaaSColors
import kotlin.math.max

@Composable
fun SmoothAreaLineChart(
    data: List<Pair<String, Float>>,
    lineColor: Color,
    valuePrefix: String = "₹",
    valueSuffix: String = "K",
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) return
    var selectedIndex by remember(data) { mutableIntStateOf(data.lastIndex) }
    val semantic = LocalSaaSColors.current
    val gridColor = semantic.cardBorder

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val selected = data.getOrElse(selectedIndex) { data.last() }
            Surface(
                color = lineColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    text = "${selected.first}: $valuePrefix${selected.second.toInt()}$valueSuffix",
                    color = lineColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            Text(
                text = "Tap month to inspect",
                style = MaterialTheme.typography.labelSmall,
                color = semantic.textMuted,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp),
        ) {
            val maxVal = max(data.maxOf { it.second }, 1f) * 1.15f
            val leftPad = 12.dp.toPx()
            val rightPad = 12.dp.toPx()
            val topPad = 12.dp.toPx()
            val bottomPad = 10.dp.toPx()
            val chartW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
            val chartH = (size.height - topPad - bottomPad).coerceAtLeast(1f)

            // Dashed horizontal grid lines
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
            repeat(4) { idx ->
                val y = topPad + chartH * (idx / 3f)
                drawLine(
                    color = gridColor,
                    start = Offset(leftPad, y),
                    end = Offset(size.width - rightPad, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect,
                )
            }

            val stepX = if (data.size > 1) chartW / (data.size - 1) else 0f
            val points = data.mapIndexed { index, pair ->
                val x = leftPad + index * stepX
                val y = topPad + chartH - (pair.second / maxVal) * chartH
                Offset(x, y)
            }

            val linePath = Path().apply {
                points.forEachIndexed { idx, pt ->
                    if (idx == 0) moveTo(pt.x, pt.y)
                    else {
                        val prev = points[idx - 1]
                        val midX = (prev.x + pt.x) / 2f
                        cubicTo(midX, prev.y, midX, pt.y, pt.x, pt.y)
                    }
                }
            }

            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(points.last().x, topPad + chartH)
                lineTo(points.first().x, topPad + chartH)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.28f), lineColor.copy(alpha = 0.01f)),
                    startY = topPad,
                    endY = topPad + chartH,
                ),
            )

            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )

            points.forEachIndexed { idx, pt ->
                val isSelected = idx == selectedIndex
                if (isSelected) {
                    drawCircle(color = lineColor.copy(alpha = 0.22f), radius = 10.dp.toPx(), center = pt)
                }
                drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                drawCircle(color = lineColor, radius = 3.5.dp.toPx(), center = pt)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            data.forEachIndexed { index, (label, _) ->
                val isSelected = index == selectedIndex
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) lineColor else semantic.textMuted,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { selectedIndex = index }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
fun SaaSBarChart(
    data: List<Pair<String, Float>>,
    barColor: Color,
    valuePrefix: String = "₹",
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) return
    var selectedIdx by remember(data) { mutableIntStateOf(data.lastIndex) }
    val semantic = LocalSaaSColors.current
    val maxVal = max(data.maxOf { it.second }, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        val current = data.getOrElse(selectedIdx) { data.last() }
        Text(
            text = "${current.first}: $valuePrefix${current.second.toInt()}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = barColor,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
        ) {
            val count = data.size
            val slotW = size.width / count
            val barW = (slotW * 0.48f).coerceAtMost(36.dp.toPx())
            val maxH = size.height - 10.dp.toPx()

            data.forEachIndexed { index, (_, value) ->
                val h = ((value / maxVal) * maxH).coerceAtLeast(8.dp.toPx())
                val left = index * slotW + (slotW - barW) / 2f
                val top = size.height - h
                val active = index == selectedIdx
                drawRoundRect(
                    color = if (active) barColor else barColor.copy(alpha = 0.45f),
                    topLeft = Offset(left, top),
                    size = Size(barW, h),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            data.forEachIndexed { idx, (label, _) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (idx == selectedIdx) FontWeight.Bold else FontWeight.Normal,
                    color = if (idx == selectedIdx) barColor else semantic.textMuted,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { selectedIdx = idx }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
fun CategoryDonutChart(
    slices: List<Triple<String, Float, Color>>,
    centerLabel: String,
    modifier: Modifier = Modifier,
) {
    val total = slices.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier.size(116.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 18.dp.toPx()
                var startAngle = -90f
                slices.forEach { (_, value, color) ->
                    val sweep = (value / total) * 360f
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = (sweep - 3f).coerceAtLeast(2f),
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                    startAngle += sweep
                }
            }
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            slices.forEach { (label, value, color) ->
                val pct = ((value / total) * 100).toInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
