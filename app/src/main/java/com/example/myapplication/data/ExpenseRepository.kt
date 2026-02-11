class ExpenseRepository(private val dao: ExpenseDao) {
    suspend fun insert(expense: Expense) = dao.insertExpense(expense)
    suspend fun getAll() = dao.getAllExpenses()
}
