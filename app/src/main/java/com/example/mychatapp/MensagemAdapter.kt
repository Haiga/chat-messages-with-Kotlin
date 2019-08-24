package com.example.mychatapp

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class MensagemAdapter (context: Context, msgs: List<Mensagem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var context: Context
    var mensagens: List<Mensagem>
    private val VIEW_TYPE_MESSAGE_SENT = 1
    private val VIEW_TYPE_MESSAGE_RECEIVED = 2
    init {
        this.context = context
        this.mensagens = msgs

    }

    override fun getItemViewType(position: Int): Int {
        val message = mensagens.get(position)

        return if (message.enviada.equals(true)) {
            VIEW_TYPE_MESSAGE_SENT
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(context)

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            return MensagemHolder(inflater.inflate(R.layout.single_message_sent, parent, false))
        }
//        else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
        return MensagemHolder(inflater.inflate(R.layout.single_message_received, parent, false))
//        }
//        return MensagemHolder(inflater.inflate(R.layout.single_message, parent, false))

    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


        val mensagem = mensagens[position]
        val m = holder as MensagemHolder

        m.lbl_conteudo.text = mensagem.conteudo//

        if(!mensagem.enviada){
            m.lbl_nome.text = mensagem.user//
        }else{
            m.lbl_nome.text = ""
        }
//        m.lbl_enviada.setText(mensagem.enviada.toString())

    }
    override fun getItemCount(): Int {
        return mensagens.size
    }

    internal class MensagemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var lbl_conteudo: TextView
        var lbl_nome: TextView
//        var lbl_enviada: TextView

        init {
            lbl_conteudo = itemView.findViewById(R.id.lbl_conteudo) as TextView
            lbl_nome = itemView.findViewById(R.id.name) as TextView
//            lbl_enviada = itemView.findViewById(R.id.lbl_enviada) as TextView
        }
    }
}