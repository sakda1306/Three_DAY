package com.example.myapplication

import Expense
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    expenseList: List<Expense>, // รับข้อมูลจริง
    onBack: () -> Unit // ฟังก์ชันสำหรับกดย้อนกลับ
) {
    // แปลงข้อมูล Expense Entity -> ExpenseItem (UI Model)
    // ตรงนี้เรา copy logic การแปลงมาจาก Dashboard เพื่อให้แสดงผลเหมือนกัน
    val allExpenses = remember(expenseList) {
        expenseList.map { expense ->
            val dateObj = Instant.ofEpochMilli(expense.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            ExpenseItem(
                id = expense.id,
                category = expense.category,
                amount = expense.amount,
                color = getCategoryColor(expense.category), // ใช้ฟังก์ชันจาก Dashboard.kt
                date = dateObj.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                type = expense.type,
                rawDate = dateObj
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // --- Header (ปุ่มย้อนกลับ + หัวข้อ) ---
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

        // --- รายการข้อมูล (ย้ายมาจาก Dashboard) ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allExpenses) { item ->
                // เรียกใช้ CategoryRow (ต้องมั่นใจว่า CategoryRow ใน Dashboard.kt เป็น public หรือ copy มาวางที่นี่ก็ได้)
                CategoryRow(item)
            }
        }
    }
}