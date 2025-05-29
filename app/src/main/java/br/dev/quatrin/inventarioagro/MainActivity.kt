package br.dev.quatrin.inventarioagro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.dev.quatrin.inventarioagro.service.SyncService
import br.dev.quatrin.inventarioagro.ui.tipo.TipoListActivity
import br.dev.quatrin.inventarioagro.ui.marca.MarcaListActivity
import br.dev.quatrin.inventarioagro.ui.maquina.MaquinaListActivity
import br.dev.quatrin.inventarioagro.util.NetworkUtils

class MainActivity : AppCompatActivity() {

    private val syncReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val message = intent?.getStringExtra(SyncService.EXTRA_MESSAGE) ?: ""
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupButtons()
        registerSyncReceiver()

        // Sincronização automática ao abrir o app
        autoSyncAllData()
    }

    private fun autoSyncAllData() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Sincronizando todos os dados...", Toast.LENGTH_SHORT).show()
            SyncService.startFetchAll(this)
        } else {
            Toast.makeText(
                this,
                "Sem conexão de rede - dados locais disponíveis",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSyncReceiver()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btn_tipos).setOnClickListener {
            startActivity(Intent(this, TipoListActivity::class.java))
        }

        findViewById<Button>(R.id.btn_marcas).setOnClickListener {
            startActivity(Intent(this, MarcaListActivity::class.java))
        }

        findViewById<Button>(R.id.btn_maquinas).setOnClickListener {
            startActivity(Intent(this, MaquinaListActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_fetch_server -> {
                Toast.makeText(this, "Buscando dados do servidor...", Toast.LENGTH_SHORT).show()
                SyncService.startFetchAll(this)
                true
            }

            R.id.action_sync_offline -> {
                Toast.makeText(this, "Sincronizando dados offline...", Toast.LENGTH_SHORT).show()
                SyncService.startSyncOffline(this)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun registerSyncReceiver() {
        val filter = IntentFilter().apply {
            addAction(SyncService.BROADCAST_SYNC_COMPLETE)
            addAction(SyncService.BROADCAST_SYNC_ERROR)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(syncReceiver, filter)
    }

    private fun unregisterSyncReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(syncReceiver)
    }
}
