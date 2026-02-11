import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")   // ต้องตรงกับชื่อที่ใช้ใน @Query
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val amount: Double,
    val title: String,
    val type: String,
    val category: String,
    val date: Long,
    val hour: Int,
    val minute: Int
)
