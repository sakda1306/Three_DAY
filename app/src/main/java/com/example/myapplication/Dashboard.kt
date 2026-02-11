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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. Model ข้อมูลจำลอง
data class ExpenseItem(
    val id: Int,
    val category: String,
    val amount: Double,
    val color: Color,
    val date: String // ใน SQL จริงจะเก็บเป็น Long หรือ String วันที่
)

// 2. ข้อมูลจำลอง (Mock Data)
val mockExpenses = listOf(
    ExpenseItem(1, "อาหาร", 4500.0, Color(0xFF4CAF50), "2023-07-10"),
    ExpenseItem(2, "เดินทาง", 1200.0, Color(0xFF2196F3), "2023-07-11"),
    ExpenseItem(3, "ชอปปิ้ง", 3000.0, Color(0xFFFF9800), "2023-07-12"),
    ExpenseItem(4, "ที่พัก", 8500.0, Color(0xFFE91E63), "2023-07-01")
)

@Composable
fun DashboardScreen(onNavigateToAddExpense: () -> Unit) {
    // State สำหรับ Filter และ Sort
    var selectedMonth by remember { mutableStateOf("กรกฎาคม") }
    var isMonthMenuExpanded by remember { mutableStateOf(false) }
    var isSortAscending by remember { mutableStateOf(false) } // เรียงจากมากไปน้อย/น้อยไปมาก

    // จำลองการ Sort ข้อมูล
    val displayedExpenses = if (isSortAscending) {
        mockExpenses.sortedBy { it.amount }
    } else {
        mockExpenses.sortedByDescending { it.amount }
    }

    val totalIncome = 25000.0
    val totalExpense = mockExpenses.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- ส่วนที่ 1: Header ---
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "สรุปผลการใช้จ่ายประจำเดือน",
            fontSize = 12.sp,
            color = Color.Gray
        )

        // --- ส่วนที่ 2: Filter & Sort ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ตัวเลือกเดือน (Dropdown)
            Box {
                Row(
                    modifier = Modifier.clickable { isMonthMenuExpanded = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "เดือน $selectedMonth", fontWeight = FontWeight.Medium)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = isMonthMenuExpanded,
                    onDismissRequest = { isMonthMenuExpanded = false }
                ) {
                    listOf("มิถุนายน", "กรกฎาคม", "สิงหาคม").forEach { month ->
                        DropdownMenuItem(
                            text = { Text(month) },
                            onClick = {
                                selectedMonth = month
                                isMonthMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // ปุ่ม Sort
            IconButton(onClick = { isSortAscending = !isSortAscending }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List, // หรือ Icons.Default.Menu
                    contentDescription = "Sort",
                    tint = if (isSortAscending) MaterialTheme.colorScheme.primary else Color.Black
                )
            }
        }

        // --- ส่วนที่ 3: กราฟวงกลม (จำลอง) ---
        Box(
            modifier = Modifier
                .size(200.dp) // เพิ่มขนาดเล็กน้อยให้ดูสวย
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val total = totalExpense.toFloat()
                var startAngle = -90f // เริ่มวาดจากด้านบนสุด (เที่ยงตรง)

                mockExpenses.forEach { item ->
                    // คำนวณองศาตามสัดส่วนเงิน (เงินหมวดหมู่ / เงินทั้งหมด * 360 องศา)
                    val sweepAngle = (item.amount.toFloat() / total) * 360f

                    drawArc(
                        color = item.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false, // ตั้งเป็น false เพื่อทำเป็นวงแหวน (Donut)
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 40.dp.toPx(), // ความหนาของเส้นกราฟ
                            cap = androidx.compose.ui.graphics.StrokeCap.Round // ทำให้ปลายโค้งมนดู Modern
                        )
                    )
                    startAngle += sweepAngle
                }
            }

            // ข้อความตรงกลางกราฟ (Donut hole)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ยอดใช้จ่ายรวม", fontSize = 12.sp, color = Color.Gray)
                Text("฿${String.format("%,.0f", totalExpense)}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- ส่วนที่ 4: สรุปรายรับ/รายจ่าย ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF9F9F9)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryRow(label = "รายรับทั้งหมด", value = totalIncome, color = Color(0xFF4CAF50))
                SummaryRow(label = "รายจ่ายทั้งหมด", value = totalExpense, color = Color(0xFFF44336))
                Divider()
                SummaryRow(
                    label = "คงเหลือ",
                    value = balance,
                    color = if (balance >= 0) Color(0xFF2196F3) else Color.Red,
                    isBold = true
                )
            }
        }

        // --- ส่วนที่ 5: รายการแยกตามหมวดหมู่ ---
        Text(
            "แยกตามหมวดหมู่ (${if(isSortAscending) "น้อยไปมาก" else "มากไปน้อย"})",
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 20.dp, bottom = 8.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(displayedExpenses) { item ->
                CategoryRow(item)
            }
        }

        // --- ส่วนที่ 6: ปุ่มเพิ่มรายการ ---
        Button(
            onClick = onNavigateToAddExpense,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("บันทึกค่าใช้จ่ายใหม่", fontSize = 16.sp)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: Double, color: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(color, shape = CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 14.sp)
        }
        Text(
            text = "฿$value",
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun CategoryRow(item: ExpenseItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
            Text(text = "฿${item.amount}", fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}