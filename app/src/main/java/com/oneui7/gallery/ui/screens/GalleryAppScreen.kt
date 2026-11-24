@file:OptIn(ExperimentalMaterial3Api::class)

package com.oneui7.gallery.ui.screens

import android.content.Intent
import android.app.Activity
import android.content.ActivityNotFoundException
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.input.pointer.PointerEventPass
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddToHomeScreen
import androidx.compose.material.icons.rounded.AppShortcut
import androidx.compose.material.icons.rounded.AutoAwesomeMotion
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterVintage
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PhotoAlbum
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import coil.size.Size
import com.oneui7.gallery.model.Album
import com.oneui7.gallery.model.MediaItem
import com.oneui7.gallery.ui.GalleryViewModel
import com.oneui7.gallery.ui.components.ShortcutHelper

@Composable
fun GalleryAppScreen(
    viewModel: GalleryViewModel,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val albums by viewModel.albums.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedAlbum by viewModel.selectedAlbum.collectAsState()
    val viewingMedia by viewModel.viewingMedia.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    BackHandler(enabled = viewingMedia != null || selectedAlbum != null || isSearching) {
        when {
            viewingMedia != null -> viewModel.openMediaViewer(null)
            selectedAlbum != null -> viewModel.selectAlbum(null)
            isSearching -> viewModel.toggleSearch(false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        if (!hasPermission) {
            PermissionPromptScreen(onRequestPermission = onRequestPermission)
        } else if (isLoading && albums.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            AnimatedContent(
                targetState = selectedAlbum,
                transitionSpec = {
                    if (targetState != null) {
                        (slideInHorizontally(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)) { it / 2 } + fadeIn(tween(180)))
                            .togetherWith(slideOutHorizontally { -it / 4 } + fadeOut(tween(150)))
                    } else {
                        (slideInHorizontally(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)) { -it / 4 } + fadeIn(tween(180)))
                            .togetherWith(slideOutHorizontally { it / 2 } + fadeOut(tween(150)))
                    }
                },
                label = "OneUI7AlbumNav"
            ) { currentAlbum ->
                if (currentAlbum == null) {
                    MainAlbumsHome(
                        albums = albums,
                        activeTab = activeTab,
                        isSearching = isSearching,
                        searchQuery = searchQuery,
                        onSearchQueryChange = viewModel::updateSearch,
                        onToggleSearch = viewModel::toggleSearch,
                        onTabSelected = viewModel::setTab,
                        onAlbumClick = { viewModel.selectAlbum(it) },
                        onMediaClick = { viewModel.openMediaViewer(it) }
                    )
                } else {
                    AlbumDetailSplitView(
                        album = currentAlbum,
                        allAlbums = albums,
                        onBack = { viewModel.selectAlbum(null) },
                        onSwitchAlbum = { viewModel.selectAlbum(it) },
                        onMediaClick = { viewModel.openMediaViewer(it) }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = viewingMedia != null,
            enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.9f, animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMedium)),
            exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.92f)
        ) {
            viewingMedia?.let { media ->
                FullScreenHDMediaViewer(
                    media = media,
                    onClose = { viewModel.openMediaViewer(null) },
                    onDeleted = { viewModel.onMediaDeleted() }
                )
            }
        }
    }
}

@Composable
private fun PermissionPromptScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
            modifier = Modifier.size(88.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.PhotoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Allow Gallery Access",
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Grant media access to enjoy 120Hz Full HD+ photo & video browsing on your Redmi Note 13 5G.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onRequestPermission,
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(text = "Grant Media Permission", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun MainAlbumsHome(
    albums: List<Album>,
    activeTab: Int,
    isSearching: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: (Boolean) -> Unit,
    onTabSelected: (Int) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onMediaClick: (MediaItem) -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var photoColumns by remember { mutableIntStateOf(3) }
    val filteredAlbums = remember(albums, searchQuery) {
        if (searchQuery.isBlank()) albums
        else albums.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSearching) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search albums...") },
                        singleLine = true,
                        shape = RoundedCornerShape(26.dp),
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { onToggleSearch(false) }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Close Search")
                            }
                        }
                    )
                } else {
                    Text(
                        text = when (activeTab) {
                            0 -> "Pictures"
                            2 -> "Stories"
                            3 -> "Settings"
                            else -> "Gallery"
                        },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Rounded.Tune, contentDescription = "HyperOS Shortcut Settings")
                    }
                }
            }

            when (activeTab) {
                0 -> {
                    val recentItems = albums.firstOrNull()?.items ?: emptyList()
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(photoColumns),
                        contentPadding = PaddingValues(start = 3.dp, end = 3.dp, bottom = 115.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                        verticalArrangement = Arrangement.spacedBy(2.5.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .pinchToChangeColumns { zoomIn ->
                                photoColumns = (photoColumns + if (zoomIn) -1 else 1).coerceIn(1, 7)
                            }
                    ) {
                        items(recentItems, key = { "${it.isVideo}-${it.id}" }) { item ->
                            MediaGridTile(item = item, onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onMediaClick(item)
                            })
                        }
                    }
                }
                3 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(text = "Redmi Note 13 5G Mode Active", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "- 120Hz High-Refresh Animations\n- 1080x2400 Full HD+ Original Viewer\n- Pitch-Black AMOLED Dark Theme\n- Hardware GPU Bitmap Acceleration",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.AppShortcut, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Enable Home Screen Shortcuts", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(
                                        text = "Tap here -> Other permissions -> Allow 'Home screen shortcuts' for HyperOS.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Essential albums",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${filteredAlbums.size} albums",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 115.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredAlbums) { album ->
                            AnimatedAlbumCard(album = album, onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onAlbumClick(album)
                            })
                        }
                    }
                }
            }
        }

        OneUI7FloatingBottomBar(
            activeTab = activeTab,
            onTabSelected = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onTabSelected(it)
            },
            onSearchClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggleSearch(!isSearching)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }
}

@Composable
private fun AnimatedAlbumCard(album: Album, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
        label = "AlbumCardScale"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (album.coverUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(album.coverUri)
                        .size(360)
                        .videoFrameMillis(1000)
                        .crossfade(true)
                        .build(),
                    contentDescription = album.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.PhotoAlbum,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = album.name,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = album.totalCount.toString(),
            fontSize = 11.5.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun OneUI7FloatingBottomBar(
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .shadow(12.dp, RoundedCornerShape(34.dp))
                .clip(RoundedCornerShape(34.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(34.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavPillItem(
                icon = Icons.Rounded.Image,
                label = "Pictures",
                selected = activeTab == 0,
                onClick = { onTabSelected(0) }
            )
            NavPillItem(
                icon = Icons.Rounded.PhotoAlbum,
                label = "Albums",
                selected = activeTab == 1,
                onClick = { onTabSelected(1) }
            )
            NavPillItem(
                icon = Icons.Rounded.AutoAwesomeMotion,
                label = "Stories",
                selected = activeTab == 2,
                onClick = { onTabSelected(2) }
            )
            NavPillItem(
                icon = Icons.Rounded.Menu,
                label = "Menu",
                selected = activeTab == 3,
                onClick = { onTabSelected(3) }
            )
        }

        Surface(
            onClick = onSearchClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f),
            shadowElevation = 12.dp,
            modifier = Modifier
                .size(56.dp)
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun NavPillItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent,
        label = "NavTabBg"
    )
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(21.dp),
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
        )
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumDetailSplitView(
    album: Album,
    allAlbums: List<Album>,
    onBack: () -> Unit,
    onSwitchAlbum: (Album) -> Unit,
    onMediaClick: (MediaItem) -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }
    var showAddShortcutSheet by remember { mutableStateOf(false) }
    var gridColumns by remember { mutableIntStateOf(3) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Column {
                    Text(
                        text = album.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${album.photoCount} images  ${album.videoCount} videos",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Album Options")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.clip(RoundedCornerShape(22.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("Add to Home screen", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Rounded.AddToHomeScreen, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            showAddShortcutSheet = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Start slideshow") },
                        leadingIcon = { Icon(Icons.Rounded.PlayCircle, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            album.items.firstOrNull()?.let { onMediaClick(it) }
                        }
                    )
                }
            }
        }

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.14f))

        Row(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .width(82.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(vertical = 10.dp, horizontal = 6.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(allAlbums) { sideAlbum ->
                    val isSelected = sideAlbum.name == album.name
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSwitchAlbum(sideAlbum)
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(sideAlbum.coverUri)
                                    .size(200)
                                    .build(),
                                contentDescription = sideAlbum.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sideAlbum.name,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = sideAlbum.totalCount.toString(),
                            fontSize = 9.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .weight(1f)
                    .pinchToChangeColumns { zoomIn ->
                        gridColumns = (gridColumns + if (zoomIn) -1 else 1).coerceIn(1, 7)
                    }
            ) {
                items(album.items, key = { "${it.isVideo}-${it.id}" }) { media ->
                    MediaGridTile(item = media, onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onMediaClick(media)
                    })
                }
            }
        }
    }

    if (showAddShortcutSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddShortcutSheet = false },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add to Home screen?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pin '${album.name}' directly to your Redmi Home screen for instant 1-tap access.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(22.dp))

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = album.coverUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(22.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(0xFFFF3B5C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FilterVintage,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = album.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = "1 x 1 Shortcut", fontSize = 12.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { showAddShortcutSheet = false }) {
                        Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            showAddShortcutSheet = false
                            ShortcutHelper.pinAlbumShortcut(context, album.name)
                        },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Add to Home", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MediaGridTile(item: MediaItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.uri)
                .size(480)
                .videoFrameMillis(1000)
                .crossfade(true)
                .build(),
            contentDescription = item.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (item.isVideo) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Video",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = item.formattedDuration(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun FullScreenHDMediaViewer(
    media: MediaItem,
    onClose: () -> Unit,
    onDeleted: () -> Unit
) {
    val context = LocalContext.current
    val mime = if (media.isVideo) "video/*" else "image/*"

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) onDeleted()
    }

    val openEditor = {
        val edit = Intent(Intent.ACTION_EDIT).apply {
            setDataAndType(media.uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        try {
            context.startActivity(Intent.createChooser(edit, "Edit"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No editor app found", Toast.LENGTH_SHORT).show()
        }
    }

    val openCrop = {
        val crop = Intent("com.android.camera.action.CROP").apply {
            setDataAndType(media.uri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        try {
            context.startActivity(crop)
        } catch (e: Exception) {
            openEditor()
        }
    }

    val doDelete = {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val pi = MediaStore.createDeleteRequest(context.contentResolver, listOf(media.uri))
                deleteLauncher.launch(IntentSenderRequest.Builder(pi.intentSender).build())
            } else {
                context.contentResolver.delete(media.uri, null, null)
                onDeleted()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not delete", Toast.LENGTH_SHORT).show()
        }
    }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showInfo by remember { mutableStateOf(false) }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "DoubleTapZoom"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1.2f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.8f
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 6f)
                    if (scale > 1f) {
                        offset += pan
                    } else {
                        offset = Offset.Zero
                    }
                }
            }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(media.uri)
                .size(Size.ORIGINAL)
                .crossfade(true)
                .build(),
            contentDescription = media.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = animatedScale,
                    scaleY = animatedScale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media.displayName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${media.formattedResolution()} - ${media.formattedSize()}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
            IconButton(onClick = { showInfo = !showInfo }) {
                Icon(Icons.Rounded.Info, contentDescription = "Details", tint = Color.White)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFF1A1A20).copy(alpha = 0.9f))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (media.isVideo) {
                IconButton(onClick = {
                    val play = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(media.uri, "video/*")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                        context.startActivity(play)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "No video player found", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", tint = Color.White)
                }
            } else {
                IconButton(onClick = { openCrop() }) {
                    Icon(Icons.Rounded.Crop, contentDescription = "Crop", tint = Color.White)
                }
                IconButton(onClick = { openEditor() }) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = Color.White)
                }
            }

            IconButton(onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mime
                    putExtra(Intent.EXTRA_STREAM, media.uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share"))
            }) {
                Icon(Icons.Rounded.Share, contentDescription = "Share", tint = Color.White)
            }

            IconButton(onClick = { doDelete() }) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.White)
            }

            if (!media.isVideo) {
                IconButton(onClick = {
                    scale = if (scale > 1.2f) 1f else 2.8f
                    if (scale == 1f) offset = Offset.Zero
                }) {
                    Icon(Icons.Rounded.ZoomIn, contentDescription = "Zoom", tint = Color.White)
                }
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text("OK") }
            },
            title = { Text("Details") },
            text = {
                Text(
                    "Name: ${media.displayName}\n" +
                        "Folder: ${media.albumName}\n" +
                        "Resolution: ${media.formattedResolution()}\n" +
                        "Size: ${media.formattedSize()}"
                )
            }
        )
    }
}

@Composable
private fun Modifier.pinchToChangeColumns(onPinch: (zoomIn: Boolean) -> Unit): Modifier {
    val currentOnPinch by rememberUpdatedState(onPinch)
    return this.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            var accumulated = 1f
            do {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.changes.size >= 2) {
                    accumulated *= event.calculateZoom()
                    event.changes.forEach { it.consume() }
                    if (accumulated > 1.3f) {
                        currentOnPinch(true)
                        accumulated = 1f
                    } else if (accumulated < 0.77f) {
                        currentOnPinch(false)
                        accumulated = 1f
                    }
                }
            } while (event.changes.any { it.pressed })
        }
    }
}
