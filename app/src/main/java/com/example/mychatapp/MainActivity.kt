package com.example.mychatapp

import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.support.v7.widget.Toolbar
import android.text.InputFilter
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import org.jetbrains.anko.doAsync
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryDark));
        setSupportActionBar(toolbar)

        setIPFilter()
        val conectarButton = findViewById<ImageButton>(R.id.iniciarConexaoButton)
        conectarButton.setOnClickListener {
            val intent = Intent(this, MessagesActivity::class.java)
            val ip = findViewById<EditText>(R.id.ipText).text.toString()
            val portatexto = findViewById<EditText>(R.id.portaText).text.toString()
            val nome = findViewById<EditText>(R.id.nomeText).text.toString()
            if (nome == "" || ip == "" || portatexto == "" ) {
                Toast.makeText(this@MainActivity, R.string.preencha_campos_toast, Toast.LENGTH_SHORT).show()
            }
//            val ip = "10.0.2.2"
//            val ip = "192.168.15.10"
//            val porta = 4242
//            val nome = "pedro"
//            if(false){}
            else{
                val porta = findViewById<EditText>(R.id.portaText).text.toString().toInt()
                doAsync {
                    val con = ConexaoSocket()
                    val retorno = con.conectarSocket(ip, porta)
                    if (retorno == "") {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, R.string.nao_conectou_toast, Toast.LENGTH_SHORT).show()
                        }
                    } else {

                        val msg = Mensagem()
                        msg.enviada = false
                        msg.conteudo = retorno
                        msg.user = ""

                        val dbHandler = MensagemDBHelper(this@MainActivity, null)
                        dbHandler.addMessage(msg)

                        con.enviarStringPeloSocket(nome, true)

                        MessagesActivity.conexaoSocket = con
                        intent.putExtra("nome", nome)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    fun setIPFilter() {
        val ipEditText = findViewById<EditText>(R.id.ipText)
        val filters = arrayOfNulls<InputFilter>(1)
        filters[0] = InputFilter { source, start, end, dest, dstart, dend ->
            if (end > start) {
                val destTxt = dest.toString()
                val resultingTxt =
                    destTxt.substring(0, dstart) + source.subSequence(start, end) + destTxt.substring(dend)
                if (!resultingTxt.matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?".toRegex())) {
                    return@InputFilter ""
                } else {
                    val splits = resultingTxt.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    for (i in splits.indices) {
                        if (Integer.valueOf(splits[i]) > 255) {
                            return@InputFilter ""
                        }
                    }
                }
            }
            null
        }
        ipEditText.filters = filters
    }
}
