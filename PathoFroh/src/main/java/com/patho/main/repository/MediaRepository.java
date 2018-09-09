package com.patho.main.repository;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface MediaRepository {
	
	public BufferedImage getImage(String path);

	public BufferedImage getImage(File path);

	public boolean saveImage(BufferedImage img, String path);

	public boolean saveImage(BufferedImage img, File file);

	public boolean saveBytes(byte[] bytes, String path);

	public boolean saveBytes(byte[] bytes, File path);

	public boolean saveString(String data, String path);

	public boolean saveString(String data, File path);

	public byte[] getBytes(String path);

	public byte[] getBytes(File file);

	public String getString(String file);
	
	public String getString(File file);

	public List<String> getStrings(String file);
	
	public List<String> getStrings(File file);
	
	public String getUniqueName(String path, String suffix);

	public String getUniqueName(File path, String suffix);

	public File getUniqueNameAsFile(String path, String suffix);

	public File getUniqueNameAsFile(File path, String suffix);

	public boolean delete(String path);

	public boolean delete(File path);
	
	public boolean isFile(String path);
	
	public boolean isFile(File path);

	public File[] getFilesOfDirectory(String pattern);
	
	public File getWriteFile(String path);

	public File getWriteFile(File path);

	public InputStream getInputStream(String path) throws IOException;

	public InputStream getInputStream(File path) throws IOException;

}
