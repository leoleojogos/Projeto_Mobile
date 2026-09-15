package com.example.exploradorlocais

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * App Explorador de Locais.
 *
 * O usuário digita um local (cidade, monumento, praça, restaurante...) e o app
 * dispara uma Intent IMPLÍCITA (ACTION_VIEW) para que outro app do sistema
 * — de preferência o Google Maps — mostre esse lugar no mapa.
 *
 * Referência: https://developer.android.com/guide/components/intents-filters?hl=pt-br
 */
class MainActivity : AppCompatActivity() {

    private lateinit var campoBusca: AutoCompleteTextView
    private lateinit var botaoExplorar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        campoBusca = findViewById(R.id.campoBusca)
        botaoExplorar = findViewById(R.id.botaoExplorar)

        configurarAutoComplete()

        // 1) Clique no botão "Explorar"
        botaoExplorar.setOnClickListener { explorar() }

        // 2) Clique em uma sugestão da lista -> já abre o mapa
        campoBusca.setOnItemClickListener { _, _, _, _ -> explorar() }

        // 3) Tecla "Pesquisar" do teclado virtual
        campoBusca.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                explorar()
                true
            } else {
                false
            }
        }
    }

    /**
     * Liga a lista de sugestões ao campo de busca.
     * A partir de 1 caractere digitado o dropdown já aparece.
     */
    private fun configurarAutoComplete() {
        val sugestoes = resources.getStringArray(R.array.sugestoes_locais)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            sugestoes
        )
        campoBusca.setAdapter(adapter)
        campoBusca.threshold = 1
    }

    private fun explorar() {
        val local = campoBusca.text.toString().trim()

        if (local.isEmpty()) {
            campoBusca.error = getString(R.string.erro_campo_vazio)
            campoBusca.requestFocus()
            return
        }

        abrirNoMapa(local)
    }

    /**
     * Estratégia em 3 níveis (fallback), como recomenda a documentação:
     *  1. Intent com geo: direcionada ao pacote do Google Maps;
     *  2. Intent com geo: aberta a qualquer app de mapas instalado;
     *  3. URL https do Google Maps -> abre no navegador.
     *
     * Sempre tratamos ActivityNotFoundException, porque uma Intent implícita
     * pode não encontrar nenhum app capaz de recebê-la.
     */
    private fun abrirNoMapa(local: String) {
        val consulta = Uri.encode(local)

        // geo:latitude,longitude?q=termo  -> 0,0 significa "sem centro definido,
        // deixe o app de mapas geocodificar o termo de busca".
        val geoUri = Uri.parse("geo:0,0?q=$consulta")

        // --- Nível 1: Google Maps explicitamente ---
        val intentGoogleMaps = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (tentarAbrir(intentGoogleMaps)) return

        // --- Nível 2: qualquer app de mapas que declare um intent-filter para geo: ---
        val intentQualquerMapa = Intent(Intent.ACTION_VIEW, geoUri)
        if (tentarAbrir(intentQualquerMapa)) return

        // --- Nível 3: navegador ---
        val urlWeb = Uri.parse("https://www.google.com/maps/search/?api=1&query=$consulta")
        val intentNavegador = Intent(Intent.ACTION_VIEW, urlWeb)
        if (tentarAbrir(intentNavegador)) return

        Toast.makeText(this, R.string.erro_sem_app, Toast.LENGTH_LONG).show()
    }

    private fun tentarAbrir(intent: Intent): Boolean {
        return try {
            startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
