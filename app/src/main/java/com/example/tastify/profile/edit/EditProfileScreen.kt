package com.example.tastify.profile.edit

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.Bitmap
import com.example.tastify.components.SharedBottomNavBar
import com.example.tastify.components.SharedTopBar
import com.example.tastify.profile.components.EditProfileAvatarSection
import com.example.tastify.profile.components.EditProfileCuisinesSection
import com.example.tastify.profile.components.EditProfileDietaryRestrictionsSection
import com.example.tastify.profile.components.EditProfileFrameSection
import com.example.tastify.profile.components.EditProfileInfoSection
import com.example.tastify.profile.components.EditProfileRoleSection
import com.example.tastify.profile.edit.logic.EditProfileEvent
import com.example.tastify.profile.edit.logic.EditProfileViewModel
import com.example.tastify.utils.toTempImageUri
import java.io.File

@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToRecipeProposalList: () -> Unit = {},
    onNavigateToMyProfile: () -> Unit = {},
    onNavigateToCookBook: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    unreadNotificationCount: Int = 0
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = uiState.profile
    val nameError = uiState.nameError
    val surnameError = uiState.surnameError
    val usernameError = uiState.usernameError
    val emailError = uiState.emailError
    val availableRoles = uiState.availableRoles
    val availableFrames = uiState.availableFrames

    val context = LocalContext.current

    val cameraIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                val uri = imageBitmap.toTempImageUri(context, fileName = "temp_avatar.jpg")
                viewModel.onEvent(EditProfileEvent.UpdateProfilePicture(uri.toString()))
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                cameraIntentLauncher.launch(takePictureIntent)
            } catch (e: ActivityNotFoundException) {
                println("No camera app found")
            }
        } else {
            println("Camera permission denied")
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) viewModel.onEvent(EditProfileEvent.UpdateProfilePicture(uri.toString()))
    }

    val createTempFileAndGetUri = {
        val file = File.createTempFile("profile_pic", ".jpg", context.cacheDir)
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    val handleBackPress = {
        onBack()
    }

    val handleSave = {
        viewModel.onEvent(EditProfileEvent.ValidateAndSave(onSuccess = { onBack() }))
    }

    BackHandler {
        handleBackPress()
    }

    Scaffold(
        topBar = {
            SharedTopBar(
                title = "Profile Editor",
                onBack = handleBackPress,
                actions = {
                    TextButton(onClick = { handleSave() }){
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            SharedBottomNavBar(
                selectedRoute = "profile",
                onRouteSelected = { route ->
                    when (route) {
                        "home" -> onNavigateToRecipeProposalList()
                        "cookbook" -> onNavigateToCookBook()
                        "updates" -> onNavigateToNotifications()
                        "profile" -> onNavigateToMyProfile()
                        "chat" -> onNavigateToChat()
                    }
                },
                unreadNotificationCount = unreadNotificationCount
            )
        }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(color = MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(color = MaterialTheme.colorScheme.background)
            ) {
                val isLandscape = maxWidth > maxHeight
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLandscape) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                EditProfileAvatarSection(
                                    profile = profile,
                                    onCameraClick = {
                                        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                            try {
                                                cameraIntentLauncher.launch(takePictureIntent)
                                            } catch (e: ActivityNotFoundException) {
                                                println("Error: No camera app found")
                                            }
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    onGalleryClick = { galleryLauncher.launch("image/*") },
                                    sizeFraction = 0.55f
                                )
                            }

                            Column(modifier = Modifier.weight(2f)) {
                                EditProfileInfoSection(
                                    profile = profile,
                                    nameError = nameError,
                                    surnameError = surnameError,
                                    usernameError = usernameError,
                                    emailError = emailError,
                                    onProfileChange = { viewModel.onEvent(EditProfileEvent.UpdateProfile(it)) }
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                EditProfileRoleSection(
                                    profile = profile,
                                    availableRoles = availableRoles,
                                    onProfileChange = { viewModel.onEvent(EditProfileEvent.UpdateProfile(it)) }
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                EditProfileFrameSection(
                                    profile = profile,
                                    availableFrames = availableFrames,
                                    onProfileChange = { viewModel.onEvent(EditProfileEvent.UpdateProfile(it)) }
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(24.dp))
                        EditProfileAvatarSection(
                            profile = profile,
                            onCameraClick = {
                                val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                    try {
                                        cameraIntentLauncher.launch(takePictureIntent)
                                    } catch (e: ActivityNotFoundException) {
                                        println("Error: No camera app found")
                                    }
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            onGalleryClick = { galleryLauncher.launch("image/*") }
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        EditProfileInfoSection(
                            profile = profile,
                            nameError = nameError,
                            surnameError = surnameError,
                            usernameError = usernameError,
                            emailError = emailError,
                            onProfileChange = { viewModel.onEvent(EditProfileEvent.UpdateProfile(it)) }
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        EditProfileRoleSection(
                            profile = profile,
                            availableRoles = availableRoles,
                            onProfileChange = { viewModel.onEvent(EditProfileEvent.UpdateProfile(it)) }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        EditProfileFrameSection(
                            profile = profile,
                            availableFrames = availableFrames,
                            onProfileChange = { viewModel.onEvent(EditProfileEvent.UpdateProfile(it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    EditProfileDietaryRestrictionsSection(
                        profile = profile,
                        title = "Restrictions / Ingredients",
                        onProfileChange = { viewModel.onEvent(EditProfileEvent.UpdateProfile(it)) }
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    EditProfileCuisinesSection(
                        profile = profile,
                        title = "Kitchen styles",
                        onProfileChange = { viewModel.onEvent(EditProfileEvent.UpdateProfile(it)) }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}