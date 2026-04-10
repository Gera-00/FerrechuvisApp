package com.example.ferrechuvisapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.ferrechuvisapp.data.local.entity.Categoria

@Dao
interface CategoriaDao {
    @Query("SELECT * FROM categorias ORDER BY nombre ASC")
    fun getAll(): List<Categoria>

    @Query("SELECT * FROM categorias WHERE id = :id LIMIT 1")
    fun getById(id: Int): Categoria?

    @Query("SELECT * FROM categorias WHERE nombre = :nombre LIMIT 1")
    fun getByNombre(nombre: String): Categoria?

    @Insert
    fun insert(categoria: Categoria)

    @Update
    fun update(categoria: Categoria)

    @Delete
    fun delete(categoria: Categoria)
}