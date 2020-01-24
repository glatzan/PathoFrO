package com.patho.main.repository.miscellaneous.impl

import com.patho.main.config.PathoConfig
import com.patho.main.repository.miscellaneous.MediaRepository
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.RandomStringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO

@Service
class MediaRepositoryImpl @Autowired constructor(
        private val resourceLoader: ResourceLoader,
        private val pathoConfig: PathoConfig
) : MediaRepository {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Replaces wildcards like fileRepository and workingDirectory
     */
    fun checkPath(path: String): String? {
        return if (path.startsWith("fileRepository:")) {
            path.replace("fileRepository:", "file:D:/patho/")
        } else if (path.startsWith("workDirectory:")) {
            path.replace("workDirectory:", "file:D:/patho/pdf/")
        } else path
    }

    override fun getResource(path: String): Resource {
        return resourceLoader.getResource(checkPath(path))
    }

    override fun getImage(path: String): BufferedImage {
        return getImage(File(path))
    }

    override fun getImage(path: File): BufferedImage {
        var image: BufferedImage? = null
        try {
            getInputStream(path).use { stream -> image = ImageIO.read(stream) }
        } catch (e: IOException) {
            e.printStackTrace()
            logger.error("Cannot read Image")
        }
        return image!!
    }

    override fun saveImage(img: BufferedImage, path: String): Boolean {
        return saveImage(img, File(path))
    }

    override fun saveImage(img: BufferedImage, file: File): Boolean {
        return try {
            ImageIO.write(img, "png", getFileForPath(file.path))
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    override fun saveBytes(bytes: ByteArray, path: String): Boolean {
        return saveBytes(bytes, File(path))
    }

    override fun saveBytes(bytes: ByteArray, path: File): Boolean {
        return try {
            FileUtils.writeByteArrayToFile(getFileForPath(path.path), bytes)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    override fun saveString(data: String, path: String): Boolean {
        return saveString(data, File(path))
    }

    override fun saveString(data: String, path: File): Boolean {
        return try {
            FileUtils.writeStringToFile(getFileForPath(path.path), data, Charset.defaultCharset().name())
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    override fun getBytes(path: String): ByteArray {
        return getBytes(File(path))
    }

    override fun getBytes(path: File): ByteArray {
        var bytes: ByteArray? = null

        try {
            getInputStream(path).use { stream -> bytes = IOUtils.toByteArray(stream) }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bytes!!
    }

    override fun getString(file: String): String {
        return getString(File(file))
    }

    override fun getString(file: File): String {
        var result: String? = null
        try {
            getInputStream(file).use { stream -> result = IOUtils.toString(stream, Charset.defaultCharset().name()) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result!!
    }

    override fun getStrings(file: String): List<String> {
        return getStrings(File(file))
    }

    override fun getStrings(file: File): List<String> {
        var result: List<String>? = null
        try {
            getInputStream(file).use { stream ->
                result = IOUtils.readLines(stream, Charset.defaultCharset().name())
                stream.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result!!
    }

    override fun getUniqueName(path: String, suffix: String): String {
        return getUniqueName(File(path), suffix)
    }

    override fun getUniqueName(path: File, suffix: String): String {
        var i = 0
        while (i < 10) {
            val name = RandomStringUtils.randomAlphanumeric(8) + suffix
            val f = getFileForPath(File(path, name).path)
            logger.debug("Generating name: " + f.absolutePath)
            if (f.exists() && !f.isDirectory) {
                i++
                logger.debug("File exists, new try")
                continue
            }
            return name
        }
        throw FileNotFoundException("Cannot create File after 10 attempts")
    }

    override fun delete(path: String): Boolean {
        return delete(File(path))
    }

    override fun delete(path: File): Boolean {
        val file = getFileForPath(path.path)
        val result = FileUtils.deleteQuietly(file)
        logger.debug("Deleted " + file.absolutePath + " success: " + result)
        return result
    }

    override fun isFile(path: String): Boolean {
        return isFile(File(path))
    }

    override fun isFile(path: File): Boolean {
        return getFileForPath(path.path).isFile
    }

    override fun isDirectory(path: String): Boolean {
        return isDirectory(File(path))
    }

    override fun isDirectory(path: File): Boolean {
        return return isDirectoryCreateIfNotPresent(path, false)
    }

    override fun isDirectoryCreateIfNotPresent(path: String, create: Boolean): Boolean {
        return return isDirectoryCreateIfNotPresent(File(path), true)
    }

    override fun isDirectoryCreateIfNotPresent(path: File, create: Boolean): Boolean {
        var file = getFileForPath(path.path)
        if (file.exists()) {
            if (!file.isDirectory)
                return false
        } else {
            if (create)
                file.mkdir();
            else
                return false
        }

        return true
    }

    override fun getFilesOfDirectory(pattern: String): Array<Resource> {
        return getFilesOfDirectory(File(pattern))
    }

    override fun getFilesOfDirectory(pattern: File): Array<Resource> {
        try {
            return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(checkPath(pattern.path))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return emptyArray<Resource>()
    }

    // TODO Error Handling
    override fun getFileForPath(path: String): File {
        val tmp = getResource(path)
        try {
            return tmp.file
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return File("")
    }

    override fun getParentDirectory(path: String): File {
        return getParentDirectory(File(path))
    }

    override fun getParentDirectory(path: File): File {
        return getFileForPath(path.path).parentFile
    }

    override fun getInputStream(path: String): InputStream {
        return getInputStream(File(path))
    }

    override fun getInputStream(path: File): InputStream {
        return getResource(path.path).inputStream
    }

    override fun moveFile(srcFile: String, destFile: String): Boolean {
        return moveFile(File(srcFile), File(destFile))
    }

    override fun moveFile(srcFile: File, destFile: File): Boolean {
        val srcFilePath = getFileForPath(srcFile.path)
        val destFilePath = getFileForPath(destFile.path)
        try {
            Files.move(srcFilePath.toPath(), destFilePath.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    override fun moveFileToDirectory(srcFile: String, destDir: String): Boolean {
        return moveFileToDirectory(File(srcFile), File(destDir))
    }

    override fun moveFileToDirectory(srcFile: File, destDir: File): Boolean {
        val srcFilePath = getFileForPath(srcFile.path)
        val destFilePath = getFileForPath(destDir.path)

        return try {
            FileUtils.moveFileToDirectory(srcFilePath, destFilePath, true)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    override fun copyFile(srcFile: String, destFile: String): Boolean {
        return copyFile(File(srcFile), File(destFile))
    }

    override fun copyFile(srcFile: File, destFile: File): Boolean {
        val srcFilePath = getFileForPath(srcFile.path)
        val destFilePath = getFileForPath(destFile.path)
        try {
            FileUtils.copyFile(srcFilePath, destFilePath)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    override fun copyFileToDirectory(srcFile: String, destDir: String): Boolean {
        return copyFileToDirectory(File(srcFile), File(destDir))
    }

    override fun copyFileToDirectory(srcFile: File, destDir: File): Boolean {
        val srcFilePath = getFileForPath(srcFile.path)
        val destFilePath = getFileForPath(destDir.path)

        return try {
            FileUtils.copyFileToDirectory(srcFilePath, destFilePath, true)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    override fun copyResourceToFile(srcFile: Resource, destFile: String): Boolean {
        return copyResourceToFile(srcFile, File(destFile))
    }

    override fun copyResourceToFile(srcFile: Resource, destFile: File): Boolean {
        val srcResource = srcFile.inputStream
        FileUtils.copyInputStreamToFile(srcResource, destFile)
        srcResource.close()
        return true
    }
}