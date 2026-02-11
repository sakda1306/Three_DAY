package com.example.myapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

// 1. Model ข้อมูล
data class ExpenseItem(
    val id: Int,
    val category: String,
    val amount: Double,
    val color: Color,
    val date: String, // Format: yyyy-MM-dd
    val type: String // "รายรับ" หรือ "รายจ่าย"
)

// 2. ข้อมูลจำลอง (Mock Data)
val mockExpenses = listOf(
    ExpenseItem(1, "อาหาร", 4500.0, Color(0xFF4CAF50), "2026-02-10", "รายจ่าย"),
    ExpenseItem(2, "เงินเดือน", 25000.0, Color(0xFF2196F3), "2026-02-01", "รายรับ"),
    ExpenseItem(3, "เดินทาง", 1200.0, Color(0xFFFFEB3B), "2026-02-11", "รายจ่าย"),
    ExpenseItem(4, "ชอปปิ้ง", 3000.0, Color(0xFFFF9800), "2026-02-12", "รายจ่าย"),
    ExpenseItem(5, "ขายของ", 5000.0, Color(0xFF9C27B0), "2026-02-05", "รายรับ")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onNavigateToAddExpense: () -> Unit) {
    // --- States สำหรับการควบคุม UI ---
    var showDatePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()
    var isSortAscending by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // --- คำนวณช่วงวันที่เพื่อแสดงผลบนปุ่ม ---
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

    // --- Logic การกรองข้อมูล (Filtering) ---
    val filteredExpenses = remember(dateRangePickerState.selectedStartDateMillis, dateRangePickerState.selectedEndDateMillis) {
        val startDate = dateRangePickerState.selectedStartDateMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        val endDate = dateRangePickerState.selectedEndDateMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }

        if (startDate != null && endDate != null) {
            mockExpenses.filter { item ->
                val itemDate = LocalDate.parse(item.date, formatter)
                (itemDate.isEqual(startDate) || itemDate.isAfter(startDate)) &&
                        (itemDate.isEqual(endDate) || itemDate.isBefore(endDate))
            }
        } else {
            mockExpenses
        }
    }

    // --- Logic การเรียงลำดับ (Sorting) ---
    val displayedExpenses = remember(filteredExpenses, isSortAscending) {
        if (isSortAscending) {
            filteredExpenses.sortedBy { it.amount }
        } else {
            filteredExpenses.sortedByDescending { it.amount }
        }
    }

    // --- คำนวณยอดเงินรวม ---
    val totalIncome = displayedExpenses.filter { it.type == "รายรับ" }.sumOf { it.amount }
    val totalExpense = displayedExpenses.filter { it.type == "รายจ่าย" }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(text = "Dashboard", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(text = "สรุปผลการใช้จ่าย", fontSize = 12.sp, color = Color.Gray)

        // --- ส่วนเลือกวันที่ & Sort ---
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

            IconButton(onClick = { isSortAscending = !isSortAscending }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Sort",
                    tint = if (isSortAscending) MaterialTheme.colorScheme.primary else Color.Black
                )
            }
        }

        // --- กราฟวงกลม ---
        Box(modifier = Modifier.size(200.dp).padding(16.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                displayedExpenses.filter { it.type == "รายจ่าย" }.forEach { item ->
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

        // --- รายการข้อมูล ---
        Text(
            "รายการในช่วงนี้ (${displayedExpenses.size})",
            modifier = Modifier.align(Alignment.Start).padding(top = 20.dp, bottom = 8.dp),
            fontWeight = FontWeight.Bold, fontSize = 14.sp
        )

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(displayedExpenses) { item -> CategoryRow(item) }
        }

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