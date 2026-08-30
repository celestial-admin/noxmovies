package com.example.data

import com.example.model.MediaSource
import com.example.model.Movie

interface MediaProvider {
    suspend fun search(query: String): List<Movie>
    suspend fun getDetails(id: String): Movie?
    suspend fun getSources(id: String): List<MediaSource>
}
