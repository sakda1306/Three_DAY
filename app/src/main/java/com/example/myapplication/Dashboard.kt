package com.example.myapplication

import Expense
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
// import androidx.compose.foundation.lazy.LazyColumn  <-- ลบออกไม่ได้ใช้แล้ว
// import androidx.compose.foundation.lazy.items       <-- ลบออกไม่ได้ใช้แล้ว
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

// --- Model และ Helper Function เก็บไว้เหมือนเดิม ---
data class ExpenseItem(
    val id: Long,
    val category: String,
    val amount: Double,
    val color: Color,
    val date: String,
    val type: String,
    val rawDate: LocalDate
)

fun getCategoryColor(category: String): Color {
    return when (category) {
        "อาหาร", "🍔 อาหาร" -> Color(0xFF4CAF50)
        "เดินทาง", "🚗 เดินทาง" -> Color(0xFFFFEB3B)
        "บันเทิง", "🎬 บันเทิง" -> Color(0xFF9C27B0)
        "ของใช้ส่วนตัว", "🛍️ ของใช้ส่วนตัว" -> Color(0xFFFF9800)
        "เงินเดือน" -> Color(0xFF2196F3)
        "รายรับ" -> Color(0xFF00BCD4)
        else -> Color.Gray
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    expenseList: List<Expense>,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToList: () -> Unit // *** เพิ่มพารามิเตอร์นี้ ***
) {
    // --- States ---
    var showDatePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()
    // var isSortAscending ... ลบออกได้เลยเพราะย้าย List ไปหน้าอื่นแล้ว

    // แปลงข้อมูล
    val allExpenses = remember(expenseList) {
        expenseList.map { expense ->
            val dateObj = Instant.ofEpochMilli(expense.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            ExpenseItem(
                id = expense.id,
                category = expense.category,
                amount = expense.amount,
                color = getCategoryColor(expense.category),
                date = dateObj.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                type = expense.type,
                rawDate = dateObj
            )
        }
    }

    // --- Logic การกรองข้อมูล (เก็บไว้คำนวณกราฟ) ---
    val filteredExpenses = remember(allExpenses, dateRangePickerState.selectedStartDateMillis, dateRangePickerState.selectedEndDateMillis) {
        val startDate = dateRangePickerState.selectedStartDateMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        val endDate = dateRangePickerState.selectedEndDateMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }

        if (startDate != null && endDate != null) {
            allExpenses.filter { item ->
                (item.rawDate.isEqual(startDate) || item.rawDate.isAfter(startDate)) &&
                        (item.rawDate.isEqual(endDate) || item.rawDate.isBefore(endDate))
            }
        } else {
            allExpenses
        }
    }

    // คำนวณยอดเงินรวม
    val totalIncome = filteredExpenses.filter { it.type == "รายรับ" }.sumOf { it.amount }
    val totalExpense = filteredExpenses.filter { it.type == "รายจ่าย" }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    // คำนวณวันที่สำหรับปุ่ม
    val selectedDateText = remember(dateRangePickerState.selectedStartDateMillis, dateRangePickerState.selectedEndDateMillis) {
        val start = dateRangePickerState.selectedStartDateMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        val end = dateRangePickerState.selectedEndDateMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        if (start != null && end != null) {
            "${start.format(DateTimeFormatter.ofPattern("dd MMM"))} - ${end.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}"
        } else {
            "เลือกช่วงวันที่"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(text = "Dashboard", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(text = "สรุปผลการใช้จ่าย", fontSize = 12.sp, color = Color.Gray)

        // --- ส่วนเลือกวันที่ & ปุ่มไปหน้ารายการ ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(text = selectedDateText, fontSize = 14.sp)
            }

            Spacer(Modifier.width(8.dp))

            // *** แก้ไขตรงนี้: ปุ่มกดไปหน้า List ***
            IconButton(onClick = onNavigateToList) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Go to List",
                    tint = Color.Black
                )
            }
        }

        // --- กราฟวงกลม ---
        Box(modifier = Modifier.size(200.dp).padding(16.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                val expenseItems = filteredExpenses.filter { it.type == "รายจ่าย" }

                if (expenseItems.isEmpty()) {
                    drawCircle(color = Color.LightGray, style = Stroke(width = 30.dp.toPx()))
                } else {
                    expenseItems.forEach { item ->
                        val sweepAngle = if (totalExpense > 0) (item.amount.toFloat() / totalExpense.toFloat()) * 360f else 0f
                        drawArc(
                            color = item.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 30.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                        startAngle += sweepAngle
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("รายจ่ายช่วงนี้", fontSize = 10.sp, color = Color.Gray)
                Text("฿${String.format("%,.0f", totalExpense)}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- สรุปยอดเงิน ---
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color(0xFFF5F5F5)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryRow(label = "รายรับ", value = totalIncome, color = Color(0xFF4CAF50))
                SummaryRow(label = "รายจ่าย", value = totalExpense, color = Color(0xFFF44336))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                SummaryRow(label = "คงเหลือ", value = balance, color = if (balance >= 0) Color(0xFF2196F3) else Color.Red, isBold = true)
            }
        }

        // *** ลบ LazyColumn (List) ตรงนี้ออกไปแล้ว ***

        Spacer(modifier = Modifier.weight(1f)) // ดันปุ่มไปล่างสุด

        // ปุ่มเพิ่มรายการ
        Button(
            onClick = onNavigateToAddExpense,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("บันทึกค่าใช้จ่ายใหม่")
        }
    }

    // --- DateRangePicker Dialog ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("ตกลง") }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.height(450.dp).padding(16.dp),
                title = { Text("เลือกช่วงวันที่ที่ต้องการดูสรุป") },
                showModeToggle = false
            )
        }
    }
}

// *** เก็บ SummaryRow และ CategoryRow ไว้ที่เดิมเพื่อให้ ListScreen เรียกใช้ได้ ***
@Composable
fun SummaryRow(label: String, value: Double, color: Color, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(color, shape = CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 14.sp)
        }
        Text(text = "฿${String.format("%,.2f", value)}", fontSize = 14.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun CategoryRow(item: ExpenseItem) {
    val amountColor = if (item.type == "รายรับ") Color(0xFF4CAF50) else Color.Black
    val prefix = if (item.type == "รายรับ") "+" else "-"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(item.color, shape = CircleShape))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = item.category, fontWeight = FontWeight.Medium)
                    Text(text = item.date, fontSize = 11.sp, color = Color.Gray)
                }
            }
            Text(text = "$prefix ฿${String.format("%,.2f", item.amount)}", fontWeight = FontWeight.Bold, color = amountColor)
        }
    }
}