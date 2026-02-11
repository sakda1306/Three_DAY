import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf // เพิ่ม import


class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    // 1. สร้างตัวแปร State เพื่อเก็บรายการรายจ่าย ให้ UI สังเกตการเปลี่ยนแปลงได้
    var expenses = mutableStateOf<List<Expense>>(emptyList())
        private set

    // 2. โหลดข้อมูลทันทีเมื่อ ViewModel ถูกสร้าง
    init {
        loadAllExpenses()
    }

    fun insertExpense(expense: Expense) {
        viewModelScope.launch {
            repository.insert(expense)
            loadAllExpenses() // 3. โหลดข้อมูลใหม่ทันทีหลังจากบันทึกเสร็จ
        }
    }
    // --- เพิ่มฟังก์ชันนี้ ---
    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense)
            loadAllExpenses() // ลบเสร็จแล้วโหลดข้อมูลใหม่ทันที
        }
    }

    // ฟังก์ชันสำหรับไปดึงข้อมูลจาก Repository
    private fun loadAllExpenses() {
        viewModelScope.launch {
            expenses.value = repository.getAll()
        }
    }
}

// Factory สำหรับสร้าง ViewModel พร้อม Repository
class ExpenseViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
