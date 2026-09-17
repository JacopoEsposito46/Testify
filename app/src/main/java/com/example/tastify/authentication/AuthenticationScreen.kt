package com.example.tastify.authentication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tastify.R
import com.example.tastify.authentication.components.SignInForm
import com.example.tastify.authentication.components.SignInTags
import com.example.tastify.authentication.logic.AuthenticationEvent
import com.example.tastify.authentication.logic.AuthenticationViewModel
import kotlinx.coroutines.delay

@Composable
fun AuthenticationScreen(
    viewModel: AuthenticationViewModel = hiltViewModel(),
    onSubmit: () -> Unit
){
    val context = LocalContext.current
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.isAuthSuccessful) {
        if (uiState.isAuthSuccessful) {
            onSubmit()
        }
    }

    var isSplashFinished by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1500)
        isSplashFinished = true
        delay(200)
        showContent = true
    }

    val logoSize by animateDpAsState(
        targetValue = if (isSplashFinished) 120.dp else 200.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LogoSizeAnimation"
    )

    val topPadding by animateDpAsState(
        targetValue = if (isSplashFinished) 0.dp else 180.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "SwipeUpAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(topPadding))

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(logoSize)
        )

        AnimatedVisibility(
            visible = !isSplashFinished,
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                    shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessLow))
        ) {
            Text(
                text = "Welcome",
                style = MaterialTheme.typography.titleLarge
            )
        }

        AnimatedVisibility(
            visible = showContent && !uiState.showRegistrationForm,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                    slideInVertically(
                        initialOffsetY = { 50 },
                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                    ),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                    shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessLow))
        ) {
            Button(
                onClick = { viewModel.onEvent(AuthenticationEvent.OnAuthenticationClick(context)) },
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .width(220.dp)
                    .height(50.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Logo",
                    modifier = Modifier
                        .size(24.dp),
                    tint = Color.Unspecified
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Sign in with Google",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        AnimatedVisibility(
            visible = showContent && uiState.showRegistrationForm,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                    slideInVertically(
                        initialOffsetY = { 100 },
                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                    ),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SignInForm(
                        name = uiState.name,
                        surname = uiState.surname,
                        email = uiState.email,
                        username = uiState.nickname,
                        usernameError = uiState.nicknameError,
                        photoUrl = uiState.photoUrl,
                        onUsernameChange = {
                            viewModel.onEvent(AuthenticationEvent.OnNicknameChange(it))
                            viewModel.onEvent(AuthenticationEvent.ClearNicknameError)
                        }
                    )
                        SignInTags(
                            selectedCuisine = uiState.selectedCuisine,
                            selectedDietary = uiState.selectedDietary,
                            selectedRole = uiState.selectedRole,
                            onCuisineToggle = { viewModel.onEvent(AuthenticationEvent.OnCuisineToggle(it)) },
                            onDietaryToggle = { viewModel.onEvent(AuthenticationEvent.OnDietaryToggle(it)) },
                            onRoleToggle = { viewModel.onEvent(AuthenticationEvent.OnRoleToggle(it)) }
                        )
                    Button(
                        onClick = { viewModel.onEvent(AuthenticationEvent.OnSubmitRegistration) },
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                    modifier = Modifier
                        .fillMaxWidth()
                    ) {
                        Text(
                            text = "Sign in",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                    slideInVertically(
                        initialOffsetY = { 50 },
                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                    ),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                    shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessLow))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clickable(
                            onClick = { viewModel.onEvent(AuthenticationEvent.OnAuthenticationClick(context)) }
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}