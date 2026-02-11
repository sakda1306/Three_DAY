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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. Model ข้อมูลจำลอง
// 1. ปรับ Model ให้มี type
data class ExpenseItem(
    val id: Int,
    val category: String,
    val amount: Double,
    val color: Color,
    val date: String,
    val type: String // "รายรับ" หรือ "รายจ่าย"
)

// 2. ข้อมูลจำลองใหม่ (อ้างอิงจากฟอร์ม: มีประเภท รายการ หมวดหมู่ วันที่)
val mockExpenses = listOf(
    ExpenseItem(1, "อาหาร", 4500.0, Color(0xFF4CAF50), "2023-07-10", "รายจ่าย"),
    ExpenseItem(2, "เงินเดือน", 25000.0, Color(0xFF2196F3), "2023-07-01", "รายรับ"),
    ExpenseItem(3, "เดินทาง", 1200.0, Color(0xFF2196F3), "2023-07-11", "รายจ่าย"),
    ExpenseItem(4, "ชอปปิ้ง", 3000.0, Color(0xFFFF9800), "2023-07-12", "รายจ่าย"),
    ExpenseItem(5, "ขายของ", 5000.0, Color(0xFF9C27B0), "2023-07-15", "รายรับ")
)


@Composable
fun DashboardScreen(onNavigateToAddExpense: () -> Unit) {
    // 1. States สำหรับการควบคุม UI
    var selectedMonth by remember { mutableStateOf("07") } // เก็บเป็นเลขเดือนเพื่อให้กรองง่าย
    var selectedFilter by remember { mutableStateOf("ทั้งหมด") }
    var isMonthMenuExpanded by remember { mutableStateOf(false) }
    var isFilterMenuExpanded by remember { mutableStateOf(false) }
    var isSortAscending by remember { mutableStateOf(false) }

    val filterOptions = listOf("ทั้งหมด", "วันนี้", "เดือนนี้")
    val monthOptions = mapOf("06" to "มิถุนายน", "07" to "กรกฎาคม", "08" to "สิงหาคม")

    // 2. Logic การกรองข้อมูล (Filtering)
    // ใช้ remember ที่มี keys เพื่อให้คำนวณใหม่เฉพาะเมื่อ filter เปลี่ยน
    val filteredExpenses = remember(selectedFilter, selectedMonth) {
        mockExpenses.filter { item ->
            when (selectedFilter) {
                "วันนี้" -> item.date == "2023-07-12" // สมมติวันที่ปัจจุบัน
                "เดือนนี้" -> item.date.contains("-$selectedMonth-")
                else -> true // ทั้งหมด
            }
        }
    }

    // 3. Logic การเรียงลำดับ (Sorting)
    val displayedExpenses = remember(filteredExpenses, isSortAscending) {
        if (isSortAscending) {
            filteredExpenses.sortedBy { it.amount }
        } else {
            filteredExpenses.sortedByDescending { it.amount }
        }
    }

    // 4. คำนวณยอดเงินจากข้อมูลที่กรองแล้วเท่านั้น
    val totalIncome = remember(displayedExpenses) {
        displayedExpenses.filter { it.type == "รายรับ" }.sumOf { it.amount }
    }
    val totalExpense = remember(displayedExpenses) {
        displayedExpenses.filter { it.type == "รายจ่าย" }.sumOf { it.amount }
    }
    val balance = totalIncome - totalExpense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header ---
        Text(text = "Dashboard", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(text = "สรุปผลการใช้จ่าย", fontSize = 12.sp, color = Color.Gray)

        // --- ส่วนที่ 2: Filter & Sort (ปรับปรุงใหม่) ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Dropdown เลือกช่วงเวลา
                Box {
                    Text(
                        text = selectedFilter,
                        modifier = Modifier.clickable { isFilterMenuExpanded = true },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    DropdownMenu(expanded = isFilterMenuExpanded, onDismissRequest = { isFilterMenuExpanded = false }) {
                        filterOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { selectedFilter = option; isFilterMenuExpanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Dropdown เลือกเดือน (แสดงเฉพาะเมื่อเลือก "เดือนนี้")
                if (selectedFilter == "เดือนนี้") {
                    Box {
                        Row(modifier = Modifier.clickable { isMonthMenuExpanded = true }) {
                            Text(text = "(${monthOptions[selectedMonth]})", color = Color.Gray)
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp) // ใช้ Modifier ในการกำหนดขนาด
                            )
                        }
                        DropdownMenu(expanded = isMonthMenuExpanded, onDismissRequest = { isMonthMenuExpanded = false }) {
                            monthOptions.forEach { (key, value) ->
                                DropdownMenuItem(
                                    text = { Text(value) },
                                    onClick = { selectedMonth = key; isMonthMenuExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            // ปุ่ม Sort
            IconButton(onClick = { isSortAscending = !isSortAscending }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Sort",
                    tint = if (isSortAscending) MaterialTheme.colorScheme.primary else Color.Black
                )
            }
        }

        // --- ส่วนที่ 3: กราฟวงกลม (ใช้ข้อมูลที่กรองแล้ว) ---
        Box(modifier = Modifier.size(200.dp).padding(16.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                displayedExpenses.forEach { item ->
                    val sweepAngle = if (totalExpense > 0) (item.amount.toFloat() / totalExpense.toFloat()) * 360f else 0f
                    drawArc(
                        color = item.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 35.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("รวมยอดที่เลือก", fontSize = 10.sp, color = Color.Gray)
                Text("฿${String.format("%,.0f", totalExpense)}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- ส่วนที่ 4: สรุปยอดเงิน ---
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color(0xFFF9F9F9)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryRow(label = "รายรับทั้งหมด", value = totalIncome, color = Color(0xFF4CAF50))
                SummaryRow(label = "ยอดที่แสดง", value = totalExpense, color = Color(0xFFF44336))
                HorizontalDivider()
                SummaryRow(label = "คงเหลือ", value = balance, color = if (balance >= 0) Color(0xFF2196F3) else Color.Red, isBold = true)
            }
        }

        // --- ส่วนที่ 5: รายการ (LazyColumn) ---
        Text(
            "รายการ (${displayedExpenses.size})",
            modifier = Modifier.align(Alignment.Start).padding(top = 20.dp, bottom = 8.dp),
            fontWeight = FontWeight.Bold, fontSize = 14.sp
        )

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(displayedExpenses) { item -> CategoryRow(item) }
        }

        // --- ส่วนที่ 6: ปุ่มบันทึก ---
        Button(onClick = onNavigateToAddExpense, modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(56.dp), shape = RoundedCornerShape(12.dp)) {
            Text("บันทึกค่าใช้จ่ายใหม่")
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
    // กำหนดสีของตัวเลขตามประเภท
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
                // วงกลมสีหมวดหมู่
                Box(modifier = Modifier.size(12.dp).background(item.color, shape = CircleShape))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = item.category, fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // แสดงประเภทเล็กๆ
                        Text(
                            text = item.type,
                            fontSize = 10.sp,
                            color = if(item.type == "รายรับ") Color(0xFF4CAF50) else Color.Gray,
                            modifier = Modifier.background(
                                color = if(item.type == "รายรับ") Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(4.dp)
                            ).padding(horizontal = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = item.date, fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
            // แสดงยอดเงินพร้อมเครื่องหมาย
            Text(
                text = "$prefix ฿${String.format("%,.2f", item.amount)}",
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}