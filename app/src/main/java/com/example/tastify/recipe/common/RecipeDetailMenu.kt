package com.example.tastify.recipe.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailMenu(
    isVisible: Boolean,
    isOwner: Boolean,
    isPublic: Boolean,
    onDismiss: () -> Unit,
    onStatsClick: () -> Unit,
    onReviewClick: () -> Unit,
    onCommentClick: () -> Unit,
    onReportClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onSaveClick: () -> Unit,
    isDone: Boolean = false,
    onDoneClick: () -> Unit,
    onModifyClick: () -> Unit,
    onDeleteClick: () -> Unit = {},
    onPublishClick: () -> Unit = {},
    onUnpublishClick: () -> Unit = {}
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
     if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                if (isOwner) {
                    if (!isPublic) {
                        MenuListItem(
                            Icons.Filled.Upload,
                            "Publish",
                            "Publish this recipe to the community",
                            false,
                        ) {
                            onDismiss()
                            onPublishClick()
                        }
                    } else {
                        MenuListItem(
                            Icons.Outlined.PublicOff,
                            "Unpublish",
                            "Make this recipe private",
                            false,
                        ) {
                            onDismiss()
                            onUnpublishClick()
                        }
                    }
                    MenuListItem(
                        Icons.Outlined.BookmarkBorder,
                        "Save",
                        "Save in a custom courses",
                        false,
                    ) {
                        onDismiss()
                        onSaveClick()
                    }
                    MenuListItem(
                        Icons.Outlined.Assessment,
                        "Analytics",
                        "Show the analytics of the recipe",
                        false,
                    ) {
                        onDismiss()
                        onStatsClick()
                    }
                    MenuListItem(
                        Icons.Outlined.Edit,
                        "Modify",
                        "Modify some details of the recipe",
                        false,
                    ) {
                        onDismiss()
                        onModifyClick()
                    }
                    MenuListItem(
                        Icons.Outlined.StarBorder,
                        "Reviews",
                        "Show all the reviews of the recipe",
                        false,
                    ) {
                        onDismiss()
                        onReviewClick()
                    }
                    MenuListItem(
                        Icons.AutoMirrored.Outlined.Comment,
                        "Comments",
                        "Show all the comments of the recipe",
                        false,
                    ) {
                        onDismiss()
                        onCommentClick()
                    }
                    MenuListItem(
                        Icons.Outlined.Delete,
                        "Delete",
                        "Delete permanently the recipe",
                        true,
                    ) {
                        onDismiss()
                        onDeleteClick()
                    }
                } else {
                    MenuListItem(
                        Icons.Outlined.BookmarkBorder,
                        "Save",
                        "Save in a custom courses",
                        false,
                    ) {
                        onDismiss()
                        onSaveClick()
                    }
                    MenuListItem(
                        Icons.Outlined.Download,
                        "Download",
                        "Download the recipe as a PDF",
                        false,
                    ) {
                        onDismiss()
                        onDownloadClick()
                    }
                    MenuListItem(
                        Icons.Outlined.CheckCircle,
                        if (isDone) "Cooked" else "Mark as cooked",
                        if (isDone) "Remove this recipe from your cooked history" else "Add this recipe to your cooked history",
                        false,
                    ) {
                        onDismiss()
                        onDoneClick()
                    }
                    MenuListItem(
                        Icons.Outlined.AddToPhotos,
                        "Copy",
                        "Create and publish your own variant",
                        false,
                    ) {
                        onDismiss()
                        onModifyClick()
                    }
                    MenuListItem(
                        Icons.Outlined.StarBorder,
                        "Reviews",
                        "Show all the reviews of the recipe",
                        false,
                    ) {
                        onDismiss()
                        onReviewClick()
                    }
                    MenuListItem(
                        Icons.AutoMirrored.Outlined.Comment,
                        "Comments",
                        "Show all the comments of the recipe",
                        false,
                    ) {
                        onDismiss()
                        onCommentClick()
                    }
                    MenuListItem(
                        Icons.Outlined.Block,
                        "Report",
                        "Report the recipe to the community",
                        true,
                    ) {
                        onDismiss()
                        onReportClick()
                    }
                }
            }
        }
    }
}

@Composable
fun MenuListItem(
    icon: ImageVector,
    label: String,
    support: String,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = label,
                    style = MaterialTheme.typography.titleMedium,
                color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = support,
                style = MaterialTheme.typography.bodyMedium,
                color = if(isDanger)MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(35.dp),
                tint = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp, horizontal = 16.dp),
    )
}