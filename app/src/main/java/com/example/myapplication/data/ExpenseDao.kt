import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExpenseDao {
    // เพิ่ม @JvmSuppressWildcards ตรงนี้
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    suspend fun insertExpense(expense: Expense): Long

    // และตรงนี้
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    @JvmSuppressWildcards
    suspend fun getAllExpenses(): List<Expense>
}
