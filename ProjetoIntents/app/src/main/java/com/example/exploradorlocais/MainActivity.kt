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


class MainActivity : AppCompatActivity() {

    private lateinit var campoBusca: AutoCompleteTextView
    private lateinit var botaoExplorar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        campoBusca = findViewById(R.id.campoBusca)
        botaoExplorar = findViewById(R.id.botaoExplorar)

        configurarAutoComplete()


        botaoExplorar.setOnClickListener { explorar() }

        campoBusca.setOnItemClickListener { _, _, _, _ -> explorar() }


        campoBusca.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                explorar()
                true
            } else {
                false
            }
        }
    }


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


    private fun abrirNoMapa(local: String) {
        val consulta = Uri.encode(local)


        val geoUri = Uri.parse("geo:0,0?q=$consulta")


        val intentGoogleMaps = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (tentarAbrir(intentGoogleMaps)) return


        val intentQualquerMapa = Intent(Intent.ACTION_VIEW, geoUri)
        if (tentarAbrir(intentQualquerMapa)) return


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
