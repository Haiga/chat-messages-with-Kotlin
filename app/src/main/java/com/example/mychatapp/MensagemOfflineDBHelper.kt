package com.example.mychatapp;

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlin.collections.ArrayList

class MensagemOfflineDBHelper(
    context: Context,
    factory: SQLiteDatabase.CursorFactory?
) :
    SQLiteOpenHelper(
        context, DATABASE_NAME,
        factory, DATABASE_VERSION
    ) {
    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TABLE = ("CREATE TABLE " +
                TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_CONTEUDO + " TEXT,"
                + COLUMN_ENVIADA + " TEXT,"
                + COLUMN_USER + " TEXT"
                + ")")
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun deleteMessages() {
        try{
            val db = this.writableDatabase
            db.execSQL("DELETE FROM "+ TABLE_NAME)
            db.close()
        }catch (e: Exception){
            var x = 2
        }
    }

    fun addMessage(msg: Mensagem) {
        val values = ContentValues()
        values.put(COLUMN_CONTEUDO, msg.conteudo)
        values.put(COLUMN_ENVIADA, msg.enviada.toString())
        values.put(COLUMN_USER, msg.user)
//        values.put(COLUMN_DATA, msg.data.toString())
        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllMessages(): ArrayList<Mensagem>? {
        val empList = ArrayList<Mensagem>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.count > 0) {
            cursor!!.moveToFirst()
            val msg = Mensagem()
            msg.conteudo = cursor.getString(cursor.getColumnIndex(COLUMN_CONTEUDO))
            msg.enviada = cursor.getString(cursor.getColumnIndex(COLUMN_ENVIADA))!!.toBoolean()
            msg.user = cursor.getString(cursor.getColumnIndex(COLUMN_USER))
//            msg.data = formatter.format(cursor.getString(cursor.getColumnIndex(COLUMN_DATA)))
            empList.add(msg)
            while (cursor.moveToNext()) {
                val other_msg = Mensagem()
                other_msg.conteudo = cursor.getString(cursor.getColumnIndex(COLUMN_CONTEUDO))
                other_msg.enviada = cursor.getString(cursor.getColumnIndex(COLUMN_ENVIADA))!!.toBoolean()
                other_msg.user = cursor.getString(cursor.getColumnIndex(COLUMN_USER))
//                other_msg.data = formatter.format(cursor.getString(cursor.getColumnIndex(COLUMN_DATA)))
                empList.add(other_msg)
            }
        }
        cursor.close()
        return empList
    }

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "chat2.db"
        val TABLE_NAME = "offlinemensagens"
        val COLUMN_ID = "_id"
        val COLUMN_CONTEUDO = "conteudo"
        val COLUMN_ENVIADA = "enviada"
        val COLUMN_USER = "user"
    }
}