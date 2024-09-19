package com.example.trocadeitens

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.database.Cursor

// Classe para criar e gerenciar o banco de dados
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "trade_items.db"
        private const val DATABASE_VERSION = 1

        // Tabela de Usuários
        private const val TABLE_USER = "User"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_EMAIL = "email"

        // Tabela de Itens
        private const val TABLE_ITEM = "Item"
        private const val COLUMN_ITEM_ID = "id"
        private const val COLUMN_ITEM_NAME = "name"
        private const val COLUMN_ITEM_CATEGORY = "category"
        private const val COLUMN_ITEM_VISIBLE = "visible"
        private const val COLUMN_ITEM_USER_ID = "user_id"  // Chave estrangeira para o User

        // Criação das tabelas
        private const val CREATE_TABLE_USER = """
            CREATE TABLE $TABLE_USER (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT,
                $COLUMN_EMAIL TEXT
            );
        """

        private const val CREATE_TABLE_ITEM = """
            CREATE TABLE $TABLE_ITEM (
                $COLUMN_ITEM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ITEM_NAME TEXT,
                $COLUMN_ITEM_CATEGORY TEXT,
                $COLUMN_ITEM_VISIBLE INTEGER,
                $COLUMN_ITEM_USER_ID INTEGER,
                FOREIGN KEY ($COLUMN_ITEM_USER_ID) REFERENCES $TABLE_USER($COLUMN_USER_ID)
            );
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_USER)
        db.execSQL(CREATE_TABLE_ITEM)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ITEM")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    // Método para adicionar um usuário
    fun addUser(username: String, email: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_EMAIL, email)
        }
        return db.insert(TABLE_USER, null, values)
    }

    // Método para adicionar um item
    fun addItem(name: String, category: String, visible: Boolean, userId: Long): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ITEM_NAME, name)
            put(COLUMN_ITEM_CATEGORY, category)
            put(COLUMN_ITEM_VISIBLE, if (visible) 1 else 0)
            put(COLUMN_ITEM_USER_ID, userId)
        }
        return db.insert(TABLE_ITEM, null, values)
    }

    // Método para atualizar a visibilidade do item
    fun updateItemVisibility(itemId: Long, visible: Boolean): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ITEM_VISIBLE, if (visible) 1 else 0)
        }
        return db.update(TABLE_ITEM, values, "$COLUMN_ITEM_ID = ?", arrayOf(itemId.toString()))
    }
}
