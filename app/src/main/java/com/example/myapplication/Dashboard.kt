package com.example.myapplication

import Expense
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

// --- Model และ Helper Function ---
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
        // --- หมวดหมู่รายจ่าย ---
        "อาหาร", "🍔 อาหาร" -> Color(0xFF4CAF50)     // สีเขียว
        "เดินทาง", "🚗 เดินทาง" -> Color(0xFFFFEB3B)   // สีเหลือง
        "บันเทิง", "🎬 บันเทิง" -> Color(0xFF9C27B0)   // สีม่วง
        "ของใช้ส่วนตัว", "🛍️ ของใช้ส่วนตัว" -> Color(0xFFFF9800) // สีส้ม
        "ค่าเช่า/น้ำไฟ", "🏠 ค่าเช่า/น้ำไฟ" -> Color(0xFF00BCD4) // สีฟ้า Cyan
        "รักษาพยาบาล", "💊 รักษาพยาบาล" -> Color(0xFFE91E63)   // สีชมพู

        // --- หมวดหมู่รายรับ ---
        "เงินเดือน", "💵 เงินเดือน" -> Color(0xFF2196F3) // สีฟ้า
        "โบนัส", "💰 โบนัส" -> Color(0xFFFFC107)      // สีเหลืองอำพัน (Amber)
        "ค้าขาย", "🏪 ค้าขาย" -> Color(0xFF009688)      // สีเขียวอมฟ้า (Teal)
        "การลงทุน", "📈 การลงทุน" -> Color(0xFF3F51B5)   // สีน้ำเงินเข้ม (Indigo)
        "รายได้อื่นๆ", "🎁 รายได้อื่นๆ" -> Color(0xFF607D8B) // สีเทาอมฟ้า (Blue Gray)

        // กรณีไม่เข้าพวก
        else -> Color.LightGray
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    expenseList: List<Expense>,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToList: () -> Unit
) {
    // --- States ---
    var showDatePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()

    // แปลงข้อมูลจาก Room Entity -> UI Model
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

    // --- Logic การกรองข้อมูลตามวันที่ ---
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

    // เรียงลำดับรายการล่าสุดขึ้นก่อน (เพื่อแสดงในรายการด้านล่าง)
    val recentExpenses = remember(filteredExpenses) {
        filteredExpenses.sortedByDescending { it.rawDate }
    }

    // คำนวณยอดเงินรวม
    val totalIncome = filteredExpenses.filter { it.type == "รายรับ" }.sumOf { it.amount }
    val totalExpense = filteredExpenses.filter { it.type == "รายจ่าย" }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    // คำนวณวันที่สำหรับแสดงบนปุ่ม
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

        // --- ส่วนเลือกวันที่ & ปุ่มไปหน้ารายการเต็ม ---
        // --- ส่วนเลือกวันที่ & ปุ่มไปหน้ารายการเต็ม ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- ปุ่มเลือกวันที่ (แก้ตรงนี้) ---
            OutlinedButton(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(50.dp), // กำหนดความสูงให้สวยงาม
                border = BorderStroke(1.dp, Color.Gray), // เพิ่มขอบให้ชัด
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent, // พื้นหลังใส
                    contentColor = Color.Black // สี Default ของ content เป็นสีดำ
                )
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Black // บังคับสีไอคอนเป็นสีดำ
                )
                Spacer(Modifier.width(8.dp))

                // แสดงข้อความวันที่
                Text(
                    text = selectedDateText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    // --- จุดสำคัญ: บังคับสีตัวอักษร ---
                    color = if (selectedDateText == "เลือกช่วงวันที่") {
                        Color.Gray // ถ้ายังไม่เลือก เป็นสีเทา
                    } else {
                        Color.Black // ถ้าเลือกแล้ว เป็นสีดำ (ชัดเจนทุกเครื่องแน่นอน)
                    }
                    // -----------------------------
                )
            }

            Spacer(Modifier.width(8.dp))

            // ปุ่มไปหน้า List (เหมือนเดิม)
            IconButton(onClick = onNavigateToList) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Go to List",
                    tint = Color.Black
                )
            }
        }

        // --- กราฟวงกลม (รายรับ vs รายจ่าย) ---
        Box(modifier = Modifier.size(200.dp).padding(16.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 30.dp.toPx()
                val totalFlow = totalIncome + totalExpense

                if (totalFlow == 0.0) {
                    drawCircle(color = Color.LightGray, style = Stroke(width = strokeWidth))
                } else {
                    var startAngle = -90f

                    // 1. วาดส่วนรายรับ (สีเขียว)
                    if (totalIncome > 0) {
                        val incomeSweep = ((totalIncome / totalFlow) * 360).toFloat()
                        drawArc(
                            color = Color(0xFF4CAF50),
                            startAngle = startAngle,
                            sweepAngle = incomeSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += incomeSweep
                    }

                    // 2. วาดส่วนรายจ่าย (สีแดง)
                    if (totalExpense > 0) {
                        val expenseSweep = ((totalExpense / totalFlow) * 360).toFloat()
                        drawArc(
                            color = Color(0xFFF44336),
                            startAngle = startAngle,
                            sweepAngle = expenseSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("รายจ่ายช่วงนี้", fontSize = 10.sp, color = Color.Gray)
                Text("฿${String.format("%,.0f", totalExpense)}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- สรุปตัวเลข ---
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color(0xFFF5F5F5)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryRow(label = "รายรับ", value = totalIncome, color = Color(0xFF4CAF50))
                SummaryRow(label = "รายจ่าย", value = totalExpense, color = Color(0xFFF44336))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                SummaryRow(label = "คงเหลือ", value = balance, color = if (balance >= 0) Color(0xFF2196F3) else Color.Red, isBold = true)
            }
        }

        // --- ส่วนแสดงรายการ (เพิ่มใหม่) ---
        Text(
            "รายการล่าสุด",
            modifier = Modifier.align(Alignment.Start).padding(top = 16.dp, bottom = 8.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f) // ใช้พื้นที่ที่เหลือทั้งหมด
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recentExpenses) { item ->
                CategoryRow(item)
            }
        }

        // ปุ่มเพิ่มรายการ
        Button(
            onClick = onNavigateToAddExpense,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),

            // --- เพิ่มตรงนี้เพื่อล็อคสีปุ่ม ---
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6750A4), // ใส่สีพื้นหลังปุ่มที่ต้องการ (เช่น สีม่วง หรือสีแบรนด์)
                contentColor = Color.White          // ใส่สีตัวหนังสือในปุ่ม (สีขาว)
            )
            // ----------------------------
        ) {
            Text(
                text = "บันทึกค่าใช้จ่าย",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
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

// Helper Composable
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