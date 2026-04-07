package com.example.ferrechuvisapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,
    val codigo: String,
    val precio: Double,
    val categoriaId: Int,
    val imagenPath: String?
)