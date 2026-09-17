package com.example.tastify.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.tastify.authentication.AuthenticationScreen

fun NavGraphBuilder.loginGraph(navCtrl_r: NavHostController) {
    val navActions = NavigationActions(navCtrl_r)
    navigation<LoginGraph>(startDestination = LoginRoute) {
        composable<LoginRoute> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AuthenticationScreen(
                        onSubmit = { navActions.navigateToMainAppGraph() }
                    )
                }
            }
        }
    }
}
