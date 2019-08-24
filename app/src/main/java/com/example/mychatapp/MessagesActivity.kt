package com.example.mychatapp

import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.widget.*
import kotlinx.android.synthetic.main.activity_messages.*
import org.jetbrains.anko.doAsync
import kotlin.collections.ArrayList
import android.view.Menu
import android.view.MenuItem
import android.content.BroadcastReceiver
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkInfo


class MessagesActivity : AppCompatActivity() {


    companion object {
        var conexaoSocket: ConexaoSocket? = null
    }


    var adapter: MensagemAdapter? = null
    var empList: ArrayList<Mensagem>? = null
    var nome = ""
    var dbHandlerOffline: MensagemOfflineDBHelper? = null
    var conectado = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

//        registerConnectionReceiver()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryDark))
        setSupportActionBar(toolbar)

        val dbHandler = MensagemDBHelper(this@MessagesActivity, null)
        dbHandlerOffline = MensagemOfflineDBHelper(this@MessagesActivity, null)

        empList = dbHandler.getAllMessages()
        nome = intent.getStringExtra("nome")

        conectado = true
        atualizarTela()
        escutarMensagens()
        enviarMensagensArmazenadasOffline()
        val buttonEnviar = findViewById<ImageView>(R.id.sendButton)

        buttonEnviar.setOnClickListener {
            mandarMensagemDigitada()
        }
    }

    private fun incluirMensagem(novaMensagem: Mensagem) {
        doAsync {
            val dbHandler = MensagemDBHelper(this@MessagesActivity, null)
            dbHandler.addMessage(novaMensagem)
        }
        empList?.add(novaMensagem)
    }

    private fun incluirMensagemOffline(novaMensagem: Mensagem) {
        doAsync {
            //            val dbHandler = MensagemOfflineDBHelper(this@MessagesActivity, null)
            dbHandlerOffline?.addMessage(novaMensagem)
        }
    }

    private fun atualizarTela() {
        adapter = MensagemAdapter(this, empList!!)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        mensagens_tela.setLayoutManager(layoutManager)
        mensagens_tela.adapter = adapter

        layoutManager.scrollToPositionWithOffset(empList!!.size - 1, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_principal, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId

        if (id == R.id.action_cancelar) {
            Toast.makeText(
                this@MessagesActivity,
                R.string.offline_toast,
                Toast.LENGTH_LONG
            ).show()
            doAsync {
                conexaoSocket?.enviarStringPeloSocket("", firstMessage = false, desconectar = true)
//                startActivity(intent)
            }
            conectado = false
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun estaConectado(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        return isConnected && conectado
    }

    fun escutarMensagens() {
        doAsync {
            while (true) {
                if (conexaoSocket?.isClosed()!!) {
                    break
                }
                val mensagemRecebida = conexaoSocket?.receberMensagemPeloSocket()
                runOnUiThread {
                    if (mensagemRecebida != null) {
                        if (mensagemRecebida.user != nome) {
                            incluirMensagem(mensagemRecebida)
                            atualizarTela()
                        }
                    }
                }
            }
        }
    }

    fun mandarMensagemDigitada() {
        val editTextEnviar = findViewById<EditText>(R.id.sendText)
        val mensagemParaEnviar = editTextEnviar.text.toString()
        runOnUiThread {
            editTextEnviar.setText("")

            val msg = Mensagem()
            msg.enviada = true
            msg.conteudo = mensagemParaEnviar
            msg.user = "Eu"

            incluirMensagem(msg)
            atualizarTela()
            val conectado = estaConectado(this@MessagesActivity)
            if (!conectado) {
                incluirMensagemOffline(msg)
            } else {
                doAsync {
                    conexaoSocket?.enviarStringPeloSocket(mensagemParaEnviar, false)
                }
            }
        }
    }

    fun enviarMensagensArmazenadasOffline() {
        val listaPraEnviar = dbHandlerOffline?.getAllMessages()
        doAsync {
            listaPraEnviar?.forEach {
                conexaoSocket?.enviarStringPeloSocket(it.conteudo, false)
            }
            dbHandlerOffline?.deleteMessages()
        }
    }

    //    private fun registerConnectionReceiver() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            val receiver = Receiver()
//            val intentFilter = IntentFilter()
//            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
//            registerReceiver(receiver, intentFilter)
//        }
//    }

//    internal class Receiver: BroadcastReceiver() {
//
//        override fun onReceive(context: Context, intent: Intent) {
//            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
//            val isConnected: Boolean = activeNetwork?.isConnected == true
//
////            Toast.makeText(context, if(isConnected) "Connection present" else "No Internet Connection", Toast.LENGTH_SHORT).show()
//
//            if(isConnected){
//                val dbHandler = MensagemOfflineDBHelper(context, null)
//                var listaPraEnviar = dbHandler.getAllMessages()
//                listaPraEnviar?.forEach {
//                    doAsync {
//                        conexaoSocket?.enviarStringPeloSocket(it.conteudo, false)
//                    }
//                }
//                dbHandler.deleteMessages()
//
//
//            }
//        }
//    }

}
