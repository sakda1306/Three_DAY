package com.example.myapplication

import Expense
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    expenseList: List<Expense>,
    onBack: () -> Unit,
    onDelete: (Expense) -> Unit // รับฟังก์ชันลบเข้ามา
) {
    // แปลงข้อมูล
    val allExpenses = remember(expenseList) {
        expenseList.map { expense ->
            val dateObj = Instant.ofEpochMilli(expense.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            // จับคู่ข้อมูลจริง (Expense) กับข้อมูลแสดงผล (ExpenseItem)
            Pair(
                expense,
                ExpenseItem(
                    id = expense.id,
                    category = expense.category,
                    amount = expense.amount,
                    color = getCategoryColor(expense.category), // ต้องมีฟังก์ชัน getCategoryColor ในโปรเจกต์ (เช่นใน Dashboard.kt)
                    date = dateObj.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    type = expense.type,
                    rawDate = dateObj
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "รายการทั้งหมด",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            color = Color(0xFFFEE7E6), // พื้นหลังสีแดงอ่อน ช่วยให้สะดุดตา
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F), // ไอคอนถังขยะสีแดง
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ปัดซ้ายที่รายการเพื่อลบข้อมูล",
                    style = MaterialTheme.typography.bodyMedium, // ปรับขนาดตัวอักษรให้ใหญ่ขึ้น
                    fontWeight = FontWeight.Bold, // ปรับตัวหนา
                    color = Color(0xFFD32F2F) // สีตัวอักษรแดงเข้มเพื่อความชัดเจน
                )
            }
        }

        // List รายการ
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ใช้ key เป็น id เพื่อประสิทธิภาพที่ดีขึ้น
            items(items = allExpenses, key = { it.second.id }) { (realExpense, uiItem) ->

                // State สำหรับการปัด
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            onDelete(realExpense) // สั่งลบเมื่อปัดสุด
                            true
                        } else {
                            false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color by animateColorAsState(
                            if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Color.Red else Color.Transparent,
                            label = "color"
                        )
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(color, shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White
                            )
                        }
                    },
                    content = {
                        CategoryRow(uiItem) // แสดง UI ของรายการ (จาก Dashboard.kt)
                    }
                )
            }
        }
    }
}