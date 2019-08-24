package com.example.mychatapp

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.nio.charset.Charset


data class mensagemSerializable(var comando: String, var mensagem: String)
data class mensagemFinalSerializable(var comando: String)
data class mensagemInicialSerializable(var comando: String, var apelidoUsuario: String)

class ConexaoSocket() {


    private var socket: Socket? = null
    private var ip = ""
    private var porta = 0
    private var bufferLeitura: BufferedReader? = null

    fun conectarSocket(ip: String, porta: Int): String {
        try {
            this.ip = ip
            this.porta = porta
            this.getSocket()

            var retorno: String = ""

            bufferLeitura = BufferedReader(InputStreamReader(this.socket?.inputStream))
            retorno = bufferLeitura?.readLine() ?: retorno.plus(retorno)


            val gson = Gson()
            val msgSerial = gson.fromJson(retorno, mensagemSerializable::class.java)

            return msgSerial.mensagem

        } catch (e: Exception) {
            return ""
        }

    }

    fun getSocket(): ConexaoSocket {
        this.socket = Socket(this.ip, this.porta)
        return this
    }

    fun isClosed(): Boolean? {
        return this.socket?.isClosed
    }

    fun enviarStringPeloSocket(mensagem: String, firstMessage: Boolean, desconectar: Boolean = false) {
        var final_message = ""
        var gson = Gson()
        if (firstMessage) {
            var msg = mensagemInicialSerializable("", "")
            msg.comando = "conectar"
            msg.apelidoUsuario = mensagem
            final_message = gson.toJson(msg) + " "
        } else if(desconectar){
            var msg = mensagemFinalSerializable("")
            msg.comando = "desconectar"
            final_message = gson.toJson(msg) + " "
        }else {
            var msg = mensagemSerializable("", "")
            msg.comando = "enviarMensagem"
            msg.mensagem = mensagem
            final_message = gson.toJson(msg) + " "
        }

//        var final_message = mensagem+" "+""
        this.socket?.getOutputStream()?.write(final_message.toByteArray(Charset.defaultCharset()))
        this.socket?.getOutputStream()?.flush()
    }

    fun fecharSocket() {
        this.bufferLeitura?.close()
        this.socket?.close()
    }

    fun receberMensagemPeloSocket(): Mensagem? {
        var retorno: String = ""

        bufferLeitura = BufferedReader(InputStreamReader(this.socket?.getInputStream()))
        while (true) {
            retorno += bufferLeitura?.readLine() ?: retorno.plus(retorno)
            if (!bufferLeitura!!.ready()) {
                break
            }
            retorno += "\n"
        }
        val gson = Gson()
        val msgSerial = gson.fromJson(retorno, mensagemSerializable::class.java)
        val msg = Mensagem()

        if (msgSerial.comando == "desconectar") {
            msg.conteudo = msgSerial.mensagem
            msg.user = ""
            msg.enviada = false
            this.fecharSocket()
            return msg
        } else if (msgSerial.comando == "respostaServidor") {
            if(msgSerial.mensagem.split(": ").size==1){
                msg.conteudo = msgSerial.mensagem
                msg.user = ""
                msg.enviada = false
                return msg
            }else {
                msg.conteudo = msgSerial.mensagem.split(": ")[1]
                msg.user = msgSerial.mensagem.split(" ")[0]
                msg.enviada = false
                return msg
            }
        }
        return null
    }
}