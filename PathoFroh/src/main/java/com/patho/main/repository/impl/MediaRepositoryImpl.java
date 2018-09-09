package com.patho.main.repository.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;

import com.patho.main.config.PathoConfig;
import com.patho.main.repository.MediaRepository;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Service
@Getter
@Setter
public class MediaRepositoryImpl implements MediaRepository {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ResourceLoader resourceLoader;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PathoConfig pathoConfig;

	public BufferedImage getImage(String path) {
		return getImage(new File(path));
	}

	public BufferedImage getImage(File path) {
		BufferedImage image = null;
		InputStream stream = null;
		try {
			stream = getInputStream(path);
			image = ImageIO.read(stream);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Cannot read Image");
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {
				logger.error("Cannot close stream");
			}
		}

		return image;
	}

	public boolean saveImage(BufferedImage img, String path) {
		return saveImage(img, new File(path));
	}

	public boolean saveImage(BufferedImage img, File path) {
		try {
			File file = getWriteFile(path);
			logger.debug("Saving image to " + file.getAbsolutePath());
			ImageIO.write(img, "png", file);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean saveBytes(byte[] bytes, String path) {
		return saveBytes(bytes, new File(path));
	}

	public boolean saveBytes(byte[] bytes, File path) {
		File file = getWriteFile(path);

		try {
			logger.debug("Saving file to " + file.getAbsolutePath());
			FileUtils.writeByteArrayToFile(file, bytes);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean saveString(String data, String path) {
		return saveString(data, new File(path));
	}

	public boolean saveString(String data, File path) {
		File file = getWriteFile(path);
		try {
			FileUtils.writeStringToFile(file, data, Charset.defaultCharset().name());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public byte[] getBytes(String path) {
		return getBytes(new File(path));
	}

	public byte[] getBytes(File file) {
		System.out.println("---" + file.getPath());
		byte[] bytes = null;
		InputStream stream = null;
		try {
			stream = getInputStream(file);
			bytes = IOUtils.toByteArray(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bytes;
	}

	public String getString(String file) {
		return getString(new File(file));
	}

	public String getString(File file) {
		String result = null;
		InputStream stream = null;
		try {
			stream = getInputStream(file);
			result = IOUtils.toString(stream, Charset.defaultCharset().name());
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public List<String> getStrings(String file) {
		return getStrings(new File(file));
	}

	public List<String> getStrings(File file) {
		List<String> result = null;
		InputStream stream = null;
		try {
			stream = getInputStream(file);
			result = IOUtils.readLines(stream, Charset.defaultCharset().name());
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public String getUniqueName(String path, String suffix) {
		return getUniqueName(new File(path), suffix);
	}

	public String getUniqueName(File path, String suffix) {
		logger.debug("test");
		int i = 0;
		while (i < 10) {
			String name = RandomStringUtils.randomAlphanumeric(8) + suffix;
			logger.debug("Generating name " + name);
			File f = new File(getWriteFile(path), name);
			if (f.exists() && !f.isDirectory()) {
				i++;
				continue;
			}

			return name;
		}

		return null;
	}

	public File getUniqueNameAsFile(String path, String suffix) {
		return new File(path, getUniqueName(path, suffix));
	}

	public File getUniqueNameAsFile(File path, String suffix) {
		return new File(path, getUniqueName(path, suffix));
	}

	public boolean delete(String path) {
		return delete(new File(path));
	}

	public boolean delete(File path) {
		File file = getWriteFile(path);
		boolean result = FileUtils.deleteQuietly(file);
		logger.debug("Deleted " + file.getAbsolutePath() + " success: " + result);
		return result;
	}

	public boolean isFile(String path) {
		return isFile(new File(path));
	}

	public boolean isFile(File path) {
		return getWriteFile(path).isFile();
	}

	public File getWriteFile(String path) {
		return getWriteFile(new File(path));
	}

	public File[] getFilesOfDirectory(String pattern) {
		try {
			Resource[] res = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern);
			File[] result = new File[res.length];
			for (int i = 0; i < res.length; i++) {
				result[i] = res[i].getFile();
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return new File[0];
		}
	}

	/**
	 * Returns a file for writing, ignores class path, because writing is not
	 * supported in jar
	 * 
	 * @param path
	 * @return
	 */
	public File getWriteFile(File path) {
		if (path.getPath().startsWith("fileRepository:")) {
			return new File(pathoConfig.getFileSettings().getFileRepository(),
					path.getPath().replace("fileRepository:", ""));
		} else if (path.getPath().startsWith("classpath:")) {
			try {
				return resourceLoader.getResource(path.getPath()).getFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return path;
	}

	public InputStream getInputStream(String path) throws IOException {
		return getInputStream(new File(path));
	}

	public InputStream getInputStream(File path) throws IOException {
		if (path.getPath().startsWith("classpath:")) {
			return resourceLoader.getResource(path.getPath()).getInputStream();
		} else if (path.getPath().startsWith("fileRepository:")) {
			return new FileInputStream(new File(pathoConfig.getFileSettings().getFileRepository(),
					path.getPath().replace("fileRepository:", "")));
		} else
			return new FileInputStream(path);
	}

}
