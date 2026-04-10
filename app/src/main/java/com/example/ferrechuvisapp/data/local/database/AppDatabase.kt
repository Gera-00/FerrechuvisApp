package com.example.ferrechuvisapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ferrechuvisapp.data.local.dao.CategoriaDao
import com.example.ferrechuvisapp.data.local.dao.ProductoDao
import com.example.ferrechuvisapp.data.local.entity.Categoria
import com.example.ferrechuvisapp.data.local.entity.Producto

@Database(
    entities = [Producto::class, Categoria::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun  productoDao(): ProductoDao
    abstract fun categoriaDao(): CategoriaDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tlapaleria_db"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}