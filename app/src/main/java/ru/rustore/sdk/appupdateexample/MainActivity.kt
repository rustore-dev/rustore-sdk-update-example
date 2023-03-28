package ru.rustore.sdk.appupdateexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.init(this)

        lifecycleScope.launch {
            viewModel.events
                .flowWithLifecycle(lifecycle)
                .collect { event ->
                    when (event) {
                        Event.UpdateCompleted -> popupSnackBarForCompleteUpdate()
                    }
                }
        }
    }

    private fun popupSnackBarForCompleteUpdate() {
        Snackbar.make(
            findViewById(R.id.root_layout),
            getString(R.string.downloading_completed),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(getString(R.string.button_install)) { viewModel.completeUpdateRequested() }
            show()
        }
    }
}