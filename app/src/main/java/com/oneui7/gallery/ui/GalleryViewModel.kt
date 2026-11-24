package com.oneui7.gallery.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oneui7.gallery.data.MediaRepository
import com.oneui7.gallery.model.Album
import com.oneui7.gallery.model.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MediaRepository(application)

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    private val _viewingMedia = MutableStateFlow<MediaItem?>(null)
    val viewingMedia: StateFlow<MediaItem?> = _viewingMedia.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _activeTab = MutableStateFlow(1)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    fun refreshMedia(initialAlbumToOpen: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            val loaded = repository.loadAlbums()
            _albums.value = loaded
            _isLoading.value = false

            _selectedAlbum.value?.let { current ->
                _selectedAlbum.value = loaded.find { it.name == current.name }
            }

            if (initialAlbumToOpen != null) {
                val match = loaded.find { it.name.equals(initialAlbumToOpen, ignoreCase = true) }
                if (match != null) {
                    _selectedAlbum.value = match
                }
            }
        }
    }

    fun onMediaDeleted() {
        _viewingMedia.value = null
        refreshMedia()
    }

    fun selectAlbum(album: Album?) {
        _selectedAlbum.value = album
    }

    fun openMediaViewer(item: MediaItem?) {
        _viewingMedia.value = item
    }

    fun setTab(index: Int) {
        _activeTab.value = index
    }

    fun toggleSearch(active: Boolean) {
        _isSearching.value = active
        if (!active) _searchQuery.value = ""
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }
}
