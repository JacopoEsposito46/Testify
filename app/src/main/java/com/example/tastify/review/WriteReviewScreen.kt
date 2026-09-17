package com.example.tastify.review

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.tastify.components.SharedTopBar
import com.example.tastify.models.Recipe
import com.example.tastify.review.components.PhotoSourceBottomSheet
import com.example.tastify.review.components.PhotoUploadSection
import com.example.tastify.review.components.RatingInput
import com.example.tastify.review.components.RecipeSummaryCard
import com.example.tastify.review.components.ReviewTextInput
import com.example.tastify.utils.toTempImageUri

@Composable
fun WriteReviewScreen(
    recipe: Recipe?,
    onBack: () -> Unit,
    onSubmit: (Int, String, List<String>) -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }
    val images = remember { mutableStateListOf<String>() }
    var showPhotoSourceSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val cameraIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                val uri = imageBitmap.toTempImageUri(context, fileName = "review_photo_${System.currentTimeMillis()}.jpg")
                if (images.size < 3) {
                    images.add(uri.toString())
                } else {
                    Toast.makeText(context, "Maximum 3 photos allowed", Toast.LENGTH_SHORT).show()
                }
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
                Toast.makeText(context, "No camera app found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val remainingSlots = 3 - images.size
        if (uris.size > remainingSlots) {
            Toast.makeText(context, "You can only add $remainingSlots more photo(s)", Toast.LENGTH_SHORT).show()
        }
        val allowedUris = uris.take(remainingSlots).map { it.toString() }
        images.addAll(allowedUris)
    }

    if (showPhotoSourceSheet) {
        PhotoSourceBottomSheet(
            onDismissRequest = { showPhotoSourceSheet = false },
            onCameraSelected = {
                val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    try {
                        cameraIntentLauncher.launch(takePictureIntent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "No camera app found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onGallerySelected = {
                galleryLauncher.launch("image/*")
            }
        )
    }

    Scaffold(
        topBar = {
            SharedTopBar(
                title = "Write a Review",
                onBack = onBack,
                actions = {}
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onSubmit(rating, text, images.toList()) },
                    enabled = rating > 0 && text.isNotBlank(),
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Publish", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (recipe == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    RecipeSummaryCard(recipe = recipe)
                    Spacer(modifier = Modifier.height(32.dp))
                }
                item {
                    RatingInput(
                        rating = rating,
                        onRatingChange = { rating = it }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
                item {
                    ReviewTextInput(
                        text = text,
                        onTextChange = { text = it }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    PhotoUploadSection(
                        images = images,
                        onAddPhoto = {
                            if (images.size >= 3) {
                                Toast.makeText(context, "Maximum 3 photos allowed", Toast.LENGTH_SHORT).show()
                            } else {
                                showPhotoSourceSheet = true
                            }
                        },
                        onRemovePhoto = { uri -> images.remove(uri) }
                    )
                }
            }
        }
    }
}