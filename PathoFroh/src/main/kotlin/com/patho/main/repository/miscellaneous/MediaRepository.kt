package com.patho.main.repository.miscellaneous

import org.springframework.core.io.Resource
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

interface MediaRepository {

    /**
     * Returns a spring resource for the file
     */
    fun getResource(path: String): Resource

    /**
     * Loads an image from a path. Handles path wildcards
     */
    fun getImage(path: String): BufferedImage

    /**
     * Loads an image from a path. Handles path wildcards
     */
    @Deprecated("use string")
    fun getImage(path: File): BufferedImage

    /**
     * Saves an image to a file. Handles path wildcards
     */
    fun saveImage(img: BufferedImage, path: String): Boolean

    /**
     * Saves an image to a file. Handles path wildcards
     */
    @Deprecated("use string")
    fun saveImage(img: BufferedImage, file: File): Boolean

    /**
     * Saves bytes to a file. Handles path wildcards
     */
    fun saveBytes(bytes: ByteArray, path: String): Boolean

    /**
     * Saves bytes to a file. Handles path wildcards
     */
    @Deprecated("use string")
    fun saveBytes(bytes: ByteArray, path: File): Boolean

    /**
     * Saves a string to a file. Handles path wildcards
     */
    fun saveString(data: String, path: String): Boolean

    /**
     * Saves a string to a file. Handles path wildcards
     */
    @Deprecated("use string")
    fun saveString(data: String, path: File): Boolean

    /**
     * Reads bytes from a file. Handles path wildcards
     */
    fun getBytes(path: String): ByteArray

    /**
     * Reads bytes from a file. Handles path wildcards
     */
    @Deprecated("use string")
    fun getBytes(path: File): ByteArray

    /**
     * Reads a string from a file. Handles path wildcards
     */
    fun getString(file: String): String

    /**
     * Reads a string from a file. Handles path wildcards
     */
    @Deprecated("use string")
    fun getString(file: File): String

    /**
     * Reads an array of strings from a file. Handles path wildcards
     */
    fun getStrings(file: String): List<String>

    /**
     * Reads an array of strings from a file. Handles path wildcards
     */
    @Deprecated("use string")
    fun getStrings(file: File): List<String>

    /**
     * Creates an file with a unique name, aborts after 10 tries.
     */
    @Throws(FileNotFoundException::class)
    fun getUniqueName(path: String, suffix: String): String

    /**
     * Creates an file with a unique name, aborts after 10 tries.
     */
    @Deprecated("use string")
    @Throws(FileNotFoundException::class)
    fun getUniqueName(path: File, suffix: String): String

    /**
     * Deletes the given file. Handles path wildcards
     */
    fun delete(path: String): Boolean

    /**
     * Deletes the given file. Handles path wildcards
     */
    fun delete(path: File): Boolean

    /**
     * Checks if path is file. Handles path wildcards
     */
    fun isFile(path: String): Boolean

    /**
     * Checks if path is file. Handles path wildcards
     */
    fun isFile(path: File): Boolean

    /**
     * Checks if path is directory. Handles path wildcards
     */
    fun isDirectory(path: String): Boolean

    /**
     * Checks if path is directory. Handles path wildcards
     */
    fun isDirectory(path: File): Boolean

    /**
     * Checks if path is directory. If create is true and no file/dir with that name
     * exists a directory will be created. Handles path wildcards
     */
    fun isDirectoryCreateIfNotPresent(path: String, create: Boolean): Boolean

    /**
     * Checks if path is directory. If create is true and no file/dir with that name
     * exists a directory will be created. Handles path wildcards
     */
    fun isDirectoryCreateIfNotPresent(path: File, create: Boolean): Boolean

    /**
     * Returns a list of resources of a directory. Handles path wildcards
     */
    fun getFilesOfDirectory(pattern: String): Array<Resource>

    /**
     * Returns a list of resources of a directory. Handles path wildcards
     */
    fun getFilesOfDirectory(pattern: File): Array<Resource>

    /**
     * Returns a file created via spring resources. Handles path wildcards
     */
    fun getFileForPath(path: String): File

    /**
     * Returns the parent of the given file. Handles path wildcards
     */
    fun getParentDirectory(path: String): File

    /**
     * Returns the parent of the given file. Handles path wildcards
     */
    fun getParentDirectory(path: File): File

    /**
     * Returns an input stream, created via spring resources. Handles path wildcards
     */
    @Throws(IOException::class)
    fun getInputStream(path: String): InputStream

    /**
     * Returns an input stream, created via spring resources. Handles path wildcards
     */
    @Throws(IOException::class)
    fun getInputStream(path: File): InputStream

    /**
     * Moves a file to a target file. Handles path wildcards
     */
    fun moveFile(srcFile: String, destFile: String): Boolean

    /**
     * Moves a file to a target file. Handles path wildcards
     */
    fun moveFile(srcFile: File, destFile: File): Boolean

    /**
     * Moves a file to a directory. Creates directory if not present. Handles path wildcards
     */
    fun moveFileToDirectory(srcFile: String, destDir: String): Boolean

    /**
     * Moves a file to a directory. Creates directory if not present. Handles path wildcards
     */
    fun moveFileToDirectory(srcFile: File, destDir: File): Boolean

    /**
     * Copies a file to a target file. Handles path wildcards
     */
    fun copyFile(srcFile: String, destFile: String): Boolean

    /**
     * Copies a file to a target file. Handles path wildcards
     */
    fun copyFile(srcFile: File, destFile: File): Boolean

    /**
     * Copies a file to a directory. Creates directory if not present. Handles path wildcards
     */
    fun copyFileToDirectory(srcFile: String, destDir: String): Boolean

    /**
     * Copies a file to a directory. Creates directory if not present. Handles path wildcards
     */
    fun copyFileToDirectory(srcFile: File, destDir: File): Boolean

    /**
     * Copies a file (as spring) resource, using inputStream) to a target file. Handles path wildcards
     * Can be used with jar files
     */
    fun copyResourceToFile(srcFile: Resource, destFile: String): Boolean

    /**
     * Copies a file (as spring) resource, using inputStream) to a target file. Handles path wildcards
     * Can be used with jar files
     */
    fun copyResourceToFile(srcFile: Resource, destFile: File): Boolean
}