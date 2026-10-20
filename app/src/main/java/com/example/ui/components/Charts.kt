package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CategoryExpenseSummary
import com.example.domain.CurrencyFormatter

val ModernChartPalette = listOf(
    Color(0xFF2563EB), // Blue
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFFEC4899), // Pink
    Color(0xFF8B5CF6), // Purple
    Color(0xFF06B6D4), // Cyan
    Color(0xFFF97316), // Orange
    Color(0xFF64748B)  // Slate
)

@Composable
fun IncomeExpenseBarChart(
    incomePaise: Long,
    expensePaise: Long,
    modifier: Modifier = Modifier
) {
    val maxVal = maxOf(incomePaise, expensePaise, 1L).toFloat()
    val incomeRatio by animateFloatAsState(
        targetValue = (incomePaise.toFloat() / maxVal).coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "incomeBar"
    )
    val expenseRatio by animateFloatAsState(
        targetValue = (expensePaise.toFloat() / maxVal).coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "expenseBar"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cash Flow Comparison",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (incomePaise >= expensePaise) "Net Positive" else "Deficit Pressure",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (incomePaise >= expensePaise) Color(0xFF059669) else Color(0xFFDC2626),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Income bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Income",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = incomeRatio)
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF059669), Color(0xFF10B981))
                                )
                            )
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                CurrencyText(
                    amountPaise = incomePaise,
                    fontSize = 14.sp,
                    color = Color(0xFF059669),
                    showDecimals = false
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expense bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Expense",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = expenseRatio)
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFE11D48), Color(0xFFFB7185))
                                )
                            )
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                CurrencyText(
                    amountPaise = expensePaise,
                    fontSize = 14.sp,
                    color = Color(0xFFE11D48),
                    showDecimals = false
                )
            }
        }
    }
}

@Composable
fun SpendingDonutChart(
    categories: List<CategoryExpenseSummary>,
    modifier: Modifier = Modifier
) {
    val nonZero = categories.filter { it.totalExpensePaise > 0 }
    val totalExpense = nonZero.sumOf { it.totalExpensePaise }.toFloat()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Category Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(18.dp))

            if (totalExpense <= 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expenses recorded this month.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut Chart Canvas with centered total
                    Box(
                        modifier = Modifier.size(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(130.dp)) {
                            var startAngle = -90f
                            val strokeWidth = 22.dp.toPx()
                            val arcSize = size.width - strokeWidth

                            nonZero.forEachIndexed { index, cat ->
                                val sweepAngle = (cat.totalExpensePaise.toFloat() / totalExpense) * 360f
                                val color = ModernChartPalette[index % ModernChartPalette.size]
                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(width = strokeWidth)
                                )
                                startAngle += sweepAngle
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TOTAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                            CurrencyText(
                                amountPaise = totalExpense.toLong(),
                                fontSize = 14.sp,
                                showDecimals = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    // Legend Column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        nonZero.take(5).forEachIndexed { index, cat ->
                            val color = ModernChartPalette[index % ModernChartPalette.size]
                            val pct = (cat.totalExpensePaise.toFloat() / totalExpense) * 100f
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cat.categoryName,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = "${String.format("%.0f", pct)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
