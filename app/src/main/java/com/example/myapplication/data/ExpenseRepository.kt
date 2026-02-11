class ExpenseRepository(private val dao: ExpenseDao) {
    suspend fun insert(expense: Expense) = dao.insertExpense(expense)
    suspend fun getAll() = dao.getAllExpenses()
    // เพิ่มฟังก์ชันสำหรับลบ
    suspend fun delete(expense: Expense) = dao.deleteExpense(expense)
}
