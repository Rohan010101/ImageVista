package com.example.imagevista.domain.repository

interface Downloader {

    fun downloadFile(url: String, filename: String?)
}