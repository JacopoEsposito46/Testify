package com.example.tastify

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.tastify.data.ChatLocalRepository
import com.example.tastify.navigation.LoginGraph
import com.example.tastify.navigation.MainAppGraph
import com.example.tastify.navigation.NavigationActions
import com.example.tastify.navigation.loginGraph
import com.example.tastify.navigation.mainGraph
import com.example.tastify.notifications.NotificationWorkScheduler
import com.example.tastify.notifications.SnackbarManager
import com.example.tastify.ui.theme.TastifyTheme
import com.example.tastify.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var snackbarManager: SnackbarManager

    @Inject
    lateinit var chatLocalRepository: ChatLocalRepository

    private val notificationNavigationRequests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private fun extractSharedUrl(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        if (intent.type != "text/plain") return null
        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null
        val urlRegex = Regex("""https?://\S+""")
        return urlRegex.find(text)?.value
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.navigationBarColor = android.graphics.Color.BLACK

        setContent {
            val isDarkModePref by SessionManager.isDarkMode.collectAsStateWithLifecycle(initialValue = null)
            val useDarkTheme = isDarkModePref ?: isSystemInDarkTheme()
            
            TastifyTheme(darkTheme = useDarkTheme) {
                val isLoggedIn by SessionManager.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)

                val navCtrl_r = rememberNavController()

                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn == true) {
                        val sharedUrl = extractSharedUrl(intent)
                        if (sharedUrl != null) {
                            val navActions = NavigationActions(navCtrl_r)
                            navActions.navigateToImportRecipe(sharedUrl)
                            intent.action = Intent.ACTION_MAIN
                        }
                    }
                }

                val snackbarHostState = remember { SnackbarHostState() }
                val navActions = remember(navCtrl_r) { NavigationActions(navCtrl_r) }
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = if(isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                                actionColor = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                ) { innerPadding ->
                    if (isLoggedIn != null) {
                        NavHost(
                            navController = navCtrl_r,
                            startDestination = if (isLoggedIn == true) MainAppGraph else LoginGraph,
                            modifier = Modifier
                        ) {
                            loginGraph(navCtrl_r)
                            mainGraph(navCtrl_r)
                        }
                    }
                }

                val scope = rememberCoroutineScope()
                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn == true) {
                        askNotificationPermission()
                        if (intent.isNotificationIntent()) {
                            intent.removeExtra(NotificationWorkScheduler.DESTINATION_KEY)
                            navActions.navigateToNotifications()
                        }
                    }
                }
                LaunchedEffect(Unit) {
                    notificationNavigationRequests.collect {
                        navActions.navigateToNotifications()
                    }
                }
                LaunchedEffect(Unit) {
                    snackbarManager.messages.collect { msg ->
                        val dismissJob = if (msg.actionLabel == null) {
                            scope.launch {
                                delay(2000)
                                snackbarHostState.currentSnackbarData?.dismiss()
                            }
                        } else null

                        val result = snackbarHostState.showSnackbar(
                            message = msg.text,
                            actionLabel = msg.actionLabel,
                            duration = if (msg.actionLabel != null)
                                SnackbarDuration.Short
                            else
                                SnackbarDuration.Indefinite
                        )
                        dismissJob?.cancel()
                        if (result == SnackbarResult.ActionPerformed) {
                            msg.onAction?.invoke() ?: run {
                                navActions.navigateToNotifications()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            chatLocalRepository.clearMessages()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.isNotificationIntent()) {
            notificationNavigationRequests.tryEmit(Unit)
        }
    }

    private fun askNotificationPermission() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED &&
            !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun Intent.isNotificationIntent(): Boolean {
        return getStringExtra(NotificationWorkScheduler.DESTINATION_KEY) ==
                NotificationWorkScheduler.NOTIFICATIONS_DESTINATION
    }
}