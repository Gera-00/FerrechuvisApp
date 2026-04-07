package com.example.ferrechuvisapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.ferrechuvisapp.data.local.entity.Producto

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos")
    fun getAll(): List<Producto>

    @Query("""
        SELECT * FROM productos
        WHERE nombre LIKE '%' || :query || '%'
        OR codigo LIKE '%' || :query || '%'
    """)
    fun buscar(query: String): List<Producto>

    @Insert
    fun insert(producto: Producto)

    @Update
    fun update(producto: Producto)

    @Delete
    fun delete(producto: Producto)
}