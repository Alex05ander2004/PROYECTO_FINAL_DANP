package com.example.refood.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.refood.data.local.dao.CartDao
import com.example.refood.data.local.dao.OrderDao
import com.example.refood.data.local.dao.ProductDao
import com.example.refood.data.local.dao.UserDao
import com.example.refood.data.local.entity.CartItemEntity
import com.example.refood.data.local.entity.OrderEntity
import com.example.refood.data.local.entity.OrderItemEntity
import com.example.refood.data.local.entity.ProductEntity
import com.example.refood.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE orders ADD COLUMN paymentReference TEXT")
            }
        }

        fun getInstance(context: Context, applicationScope: CoroutineScope): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context, applicationScope).also { instance = it }
            }

        private fun build(context: Context, applicationScope: CoroutineScope): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "refood.db"
            ).addMigrations(MIGRATION_1_2)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        applicationScope.launch {
                            instance?.productDao()?.insertAll(DatabaseSeeder.sampleProducts())
                        }
                    }
                }).build()
    }
}
