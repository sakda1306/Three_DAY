package com.example.myapplication

import AppDatabase
import Expense
import ExpenseRepository
import ExpenseViewModel
import ExpenseViewModelFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder( applicationContext, AppDatabase::class.java, "expense_db" ).build()
        val repository = ExpenseRepository(db.expenseDao())

        val expenseViewModel = ViewModelProvider(this, ExpenseViewModelFactory(repository))[ExpenseViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Surface( modifier = Modifier.fillMaxSize(), color = Color(0xFFF8F9FE) ){
                    AddExpenseScreen(
                        onSaveExpense = { expense ->
                            expenseViewModel.insertExpense(expense)
                        }
                    )
                }
            }
        }

    }
}


//@Preview(showBackground = true)
//@Composable
//fun preview(){
//    MyApplicationTheme {
//        Surface(
//            modifier = Modifier.fillMaxSize(),
//            color = Color(0xFFF8F9FE)
//        ) {
//            AddExpenseScreen(
//                onSaveExpense = { }
//            )
//            DashboardScreen {
//            }
//        }
//    }
//}

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
        CustomDropdownField(
            label = "ประเภทรายการ",
            selectedOption = selectedType,
            options = types,
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = it },
            onOptionSelected = { selectedType = it }
        )

        ExpenseInputField(label = "จำนวนเงิน", placeholder = "฿ 0.00", value = amount, onValueChange = { amount = it })
        ExpenseInputField(label = "ชื่อรายการ", placeholder = "ค่าอาหารกลางวัน", value = title, onValueChange = { title = it })

        CustomDropdownField(
            label = "หมวดหมู่",
            selectedOption = selectedCategory,
            options = categories,
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it },
            onOptionSelected = { selectedCategory = it }
        )

        // --- ส่วนเลือกวันที่และเวลา ---
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text(text = "วันที่และเวลา", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // TextField วันที่
                Box(modifier = Modifier.weight(1f).clickable { showDatePicker = true }) {
                    OutlinedTextField(
                        value = "📅 $dateDisplay",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false, // ปิดเพื่อให้ clickable ของ Box ทำงานแทน
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Color.LightGray,
                            disabledTextColor = Color.Black,
                            disabledContainerColor = Color.White
                        )
                    )
                }

                // TextField เวลา
                Box(modifier = Modifier.weight(0.7f).clickable { showTimePicker = true }) {
                    OutlinedTextField(
                        value = "⏰ $timeDisplay",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Color.LightGray,
                            disabledTextColor = Color.Black,
                            disabledContainerColor = Color.White
                        )
                    )
                }
            }
        }

        // --- Logic สำหรับ Date & Time Pickers ---
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedDate = datePickerState.selectedDateMillis ?: selectedDate
                        showDatePicker = false
                    }) { Text("ตกลง") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("ยกเลิก") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(initialHour = selectedHour, initialMinute = selectedMinute)
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedHour = timePickerState.hour
                        selectedMinute = timePickerState.minute
                        showTimePicker = false
                    }) { Text("ตกลง") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("ยกเลิก") }
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TimePicker(state = timePickerState)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = {
            val expense = Expense(
                amount = amount.toDoubleOrNull() ?: 0.0,
                title = title,
                type = selectedType,
                category = selectedCategory,
                date = selectedDate,
                hour = selectedHour,
                minute = selectedMinute )
            onSaveExpense(expense)
        })
        { Text("บันทึกรายการ") }
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