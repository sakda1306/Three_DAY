package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.foundation.clickable
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.data.Expense
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF8F9FE)
                ) {
                    //AddExpenseScreen()
                    DashboardScreen {
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun preview(){
    MyApplicationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8F9FE)
        ) {
            AddExpenseScreen(
                onSaveExpense = { }
            )
//            DashboardScreen {
//            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onSaveExpense: (Expense) -> Unit // callback ส่ง Expense ออกไป
) {
    val calendar = remember { Calendar.getInstance() }

    var amount by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    var typeExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("รายจ่าย") }
    val types = listOf("รายรับ", "รายจ่าย")

    var categoryExpanded by remember { mutableStateOf(false) }
    val categories = listOf("🍔 อาหาร", "🚗 เดินทาง", "🎬 บันเทิง", "🛍️ ของใช้ส่วนตัว")
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    var selectedDate by remember { mutableLongStateOf(calendar.timeInMillis) }
    var selectedHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("th")) }
    val dateDisplay = dateFormatter.format(Date(selectedDate))
    val timeDisplay = String.format("%02d:%02d", selectedHour, selectedMinute)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... UI ทั้งหมดเหมือนเดิม ...

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                // แปลง amount จาก String เป็น Double
                val expense = Expense(
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    title = title,
                    type = selectedType,
                    category = selectedCategory,
                    date = selectedDate,
                    hour = selectedHour,
                    minute = selectedMinute
                )
                onSaveExpense(expense) // ส่งออกไปยัง ViewModel/Database
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A4EE3))
        ) {
            Text("บันทึกรายการ", fontSize = 18.sp, color = Color.White)
        }
    }
}


// ฟังก์ชันสำหรับ Dropdown ที่ใช้ร่วมกันได้ทั้งประเภทรายการและหมวดหมู่
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdownField(
    label: String,
    selectedOption: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onOptionSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { onExpandedChange(!expanded) },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                options.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            onOptionSelected(item)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

// ฟังก์ชันสำหรับ Input ทั่วไปที่แก้ไขให้รับค่า (State) ได้
@Composable
fun ExpenseInputField(label: String, placeholder: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}