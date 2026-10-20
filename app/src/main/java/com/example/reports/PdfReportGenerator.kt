package com.example.reports

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.domain.CurrencyFormatter
import com.example.domain.FinancialHealthReport
import com.example.domain.MonthlyFinancialSummary
import com.example.data.local.entity.RecurringBillOccurrenceEntity
import com.example.data.local.entity.SavingsGoalEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfReportGenerator(private val context: Context) {

    fun generateMonthlyReport(
        monthName: String,
        year: Int,
        summary: MonthlyFinancialSummary,
        healthReport: FinancialHealthReport,
        pendingBills: List<RecurringBillOccurrenceEntity>,
        goals: List<SavingsGoalEntity>
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard (595 x 842 points)
        var pageNumber = 1
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Paints
        val primaryPaint = Paint().apply {
            color = Color.rgb(30, 58, 138) // #1E3A8A Navy
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.rgb(30, 41, 59) // Slate 800
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val boldPaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // Slate 900
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            color = Color.rgb(30, 58, 138)
            textSize = 18f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val subHeaderPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
        }
        val greenPaint = Paint().apply {
            color = Color.rgb(16, 185, 129)
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val redPaint = Paint().apply {
            color = Color.rgb(220, 38, 38)
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        var y = 40f
        val margin = 40f
        val rightMargin = 555f

        fun checkPageBreak(neededHeight: Float) {
            if (y + neededHeight > 780f) {
                // Draw footer
                textPaint.textSize = 8f
                canvas.drawText("Page $pageNumber", margin, 810f, textPaint)
                canvas.drawText("Orr Expency — Offline Family Finance", 360f, 810f, textPaint)
                textPaint.textSize = 10f

                pdfDocument.finishPage(page)
                pageNumber++
                val nextInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(nextInfo)
                canvas = page.canvas
                y = 40f
            }
        }

        // Header Title
        canvas.drawText("Orr Expency — Family Finance Report", margin, y, headerPaint)
        y += 18f

        val genTime = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.ENGLISH).format(Date())
        canvas.drawText("Reporting Period: $monthName $year  |  Generated: $genTime", margin, y, subHeaderPaint)
        y += 15f
        canvas.drawLine(margin, y, rightMargin, y, linePaint)
        y += 20f

        // Financial Overview Box
        canvas.drawRect(margin, y, rightMargin, y + 65f, Paint().apply { color = Color.rgb(248, 250, 252) })
        canvas.drawRect(margin, y, rightMargin, y + 65f, Paint().apply { color = Color.rgb(203, 213, 225); style = Paint.Style.STROKE })

        boldPaint.textSize = 11f
        canvas.drawText("Monthly Financial Overview", margin + 12f, y + 18f, boldPaint)
        boldPaint.textSize = 10f

        val col1 = margin + 12f
        val col2 = margin + 180f
        val col3 = margin + 350f

        canvas.drawText("Total Income: ${CurrencyFormatter.formatPaise(summary.totalIncomePaise)}", col1, y + 36f, textPaint)
        canvas.drawText("Total Expenses: ${CurrencyFormatter.formatPaise(summary.totalExpensePaise)}", col1, y + 52f, textPaint)

        val netSign = if (summary.netCashFlowPaise >= 0) "Surplus" else "Deficit"
        val netPaint = if (summary.netCashFlowPaise >= 0) greenPaint else redPaint
        canvas.drawText("Net $netSign: ${CurrencyFormatter.formatPaise(summary.netCashFlowPaise)}", col2, y + 36f, netPaint)

        val savingsRateStr = summary.savingsRatePercentage?.let { "${String.format("%.1f", it)}%" } ?: "N/A"
        canvas.drawText("Savings Rate: $savingsRateStr", col2, y + 52f, textPaint)

        canvas.drawText("Recorded Balance: ${CurrencyFormatter.formatPaise(summary.availableBalancePaise)}", col3, y + 36f, textPaint)
        val rationStr = CurrencyFormatter.formatPaise(summary.rationExpensePaise)
        canvas.drawText("Ration & Groceries: $rationStr", col3, y + 52f, textPaint)
        y += 80f

        // Financial Health Assessment
        boldPaint.textSize = 12f
        canvas.drawText("Financial Health Status: ${healthReport.title}", margin, y, boldPaint)
        boldPaint.textSize = 10f
        y += 14f

        healthReport.reasons.take(3).forEach { reason ->
            checkPageBreak(14f)
            canvas.drawText("• $reason", margin + 10f, y, textPaint)
            y += 14f
        }
        y += 10f

        // Member-wise Breakdown Table
        checkPageBreak(40f)
        boldPaint.textSize = 12f
        canvas.drawText("Family Member Breakdown", margin, y, boldPaint)
        boldPaint.textSize = 10f
        y += 16f

        // Table header
        canvas.drawRect(margin, y, rightMargin, y + 18f, Paint().apply { color = Color.rgb(241, 245, 249) })
        canvas.drawText("Member", margin + 8f, y + 13f, boldPaint)
        canvas.drawText("Paid By (₹)", margin + 130f, y + 13f, boldPaint)
        canvas.drawText("Responsible For (₹)", margin + 250f, y + 13f, boldPaint)
        canvas.drawText("Income (₹)", margin + 400f, y + 13f, boldPaint)
        y += 20f

        summary.memberSummaries.forEach { member ->
            checkPageBreak(16f)
            canvas.drawText(member.memberName, margin + 8f, y + 12f, textPaint)
            canvas.drawText(CurrencyFormatter.formatPaise(member.paidByPaise), margin + 130f, y + 12f, textPaint)
            canvas.drawText(CurrencyFormatter.formatPaise(member.responsibleForPaise), margin + 250f, y + 12f, textPaint)
            canvas.drawText(CurrencyFormatter.formatPaise(member.earnedIncomePaise), margin + 400f, y + 12f, textPaint)
            y += 16f
            canvas.drawLine(margin, y, rightMargin, y, linePaint)
        }
        y += 15f

        // Two-Wheeler Expenses Section
        checkPageBreak(40f)
        boldPaint.textSize = 12f
        canvas.drawText("Two-Wheeler Vehicle Expenses", margin, y, boldPaint)
        boldPaint.textSize = 10f
        y += 16f

        canvas.drawRect(margin, y, rightMargin, y + 18f, Paint().apply { color = Color.rgb(241, 245, 249) })
        canvas.drawText("Vehicle", margin + 8f, y + 13f, boldPaint)
        canvas.drawText("Petrol (₹)", margin + 130f, y + 13f, boldPaint)
        canvas.drawText("Service/Repairs (₹)", margin + 240f, y + 13f, boldPaint)
        canvas.drawText("Total Cost (₹)", margin + 400f, y + 13f, boldPaint)
        y += 20f

        summary.vehicleSummaries.forEach { veh ->
            checkPageBreak(16f)
            canvas.drawText(veh.vehicle.name, margin + 8f, y + 12f, textPaint)
            canvas.drawText(CurrencyFormatter.formatPaise(veh.petrolPaise), margin + 130f, y + 12f, textPaint)
            canvas.drawText(CurrencyFormatter.formatPaise(veh.serviceRepairsPaise), margin + 240f, y + 12f, textPaint)
            canvas.drawText(CurrencyFormatter.formatPaise(veh.totalMonthPaise), margin + 400f, y + 12f, boldPaint)
            y += 16f
            canvas.drawLine(margin, y, rightMargin, y, linePaint)
        }
        y += 15f

        // Category-wise Breakdown Table
        checkPageBreak(40f)
        boldPaint.textSize = 12f
        canvas.drawText("Top Category Expenses", margin, y, boldPaint)
        boldPaint.textSize = 10f
        y += 16f

        canvas.drawRect(margin, y, rightMargin, y + 18f, Paint().apply { color = Color.rgb(241, 245, 249) })
        canvas.drawText("Category", margin + 8f, y + 13f, boldPaint)
        canvas.drawText("Spent (₹)", margin + 220f, y + 13f, boldPaint)
        canvas.drawText("Budget Limit (₹)", margin + 350f, y + 13f, boldPaint)
        y += 20f

        summary.categorySummaries.take(8).forEach { cat ->
            checkPageBreak(16f)
            canvas.drawText(cat.categoryName, margin + 8f, y + 12f, textPaint)
            canvas.drawText(CurrencyFormatter.formatPaise(cat.totalExpensePaise), margin + 220f, y + 12f, textPaint)
            val budgetText = cat.budgetLimitPaise?.let { CurrencyFormatter.formatPaise(it) } ?: "No Budget"
            canvas.drawText(budgetText, margin + 350f, y + 12f, textPaint)
            y += 16f
            canvas.drawLine(margin, y, rightMargin, y, linePaint)
        }
        y += 15f

        // Savings Goals
        if (goals.isNotEmpty()) {
            checkPageBreak(40f)
            boldPaint.textSize = 12f
            canvas.drawText("Savings Goals Progress", margin, y, boldPaint)
            boldPaint.textSize = 10f
            y += 16f

            goals.forEach { g ->
                checkPageBreak(16f)
                val pct = if (g.targetAmountPaise > 0) (g.allocatedAmountPaise.toDouble() / g.targetAmountPaise) * 100.0 else 0.0
                canvas.drawText("• ${g.name}: ${CurrencyFormatter.formatPaise(g.allocatedAmountPaise)} / ${CurrencyFormatter.formatPaise(g.targetAmountPaise)} (${String.format("%.1f", pct)}%)", margin + 8f, y + 12f, textPaint)
                y += 16f
            }
            y += 10f
        }

        // Draw last page footer
        textPaint.textSize = 8f
        canvas.drawText("Page $pageNumber", margin, 810f, textPaint)
        canvas.drawText("Orr Expency — Offline Family Finance", 360f, 810f, textPaint)

        pdfDocument.finishPage(page)

        // Write to output file
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val file = File(reportsDir, "Orr_Expency_Report_${monthName}_$year.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }
}
