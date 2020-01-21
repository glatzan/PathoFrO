package com.patho.main.util.helper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FileUtil {

    /**
     * Reads the content of a template and returns the content as string.
     *
     * @param file
     * @return
     */
    public static String getContentOfFile(Resource fileResource) {


        StringBuilder toPrint = new StringBuilder();
        try {
            InputStream is = fileResource.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = br.readLine()) != null) {
                toPrint.append(line + "\r\n");
            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return toPrint.toString();
    }

    public static String getContentOfFile(String file) {

        log.debug("Getting content of file one of " + file);
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext();
        Resource resource = appContext.getResource(file);

        StringBuilder toPrint = new StringBuilder();
        try {
            InputStream is = resource.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = br.readLine()) != null) {
                toPrint.append(line + "\r\n");
            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
            appContext.close();
        }

        return toPrint.toString();
    }

    /**
     * Reads the content of a template and returns the content as a string array.
     *
     * @param file
     * @return
     */
    public static List<String> getContentOfFileAsArray(String file) {
        log.debug("Getting content of file one of " + file);
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext();
        Resource resource = appContext.getResource(file);

        List<String> result = new ArrayList<String>();
        try {
            InputStream is = resource.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
            appContext.close();
        }

        return result;
    }

    public static byte[] getFileAsBinary(String file) {
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext();
        Resource resource = appContext.getResource(file);

        byte[] result;

        try {
            result = FileUtils.readFileToByteArray(resource.getFile());
        } catch (IOException e) {
            e.printStackTrace();
            result = new byte[0];
        } finally {
            appContext.close();
        }

        return result;
    }

    public static boolean saveContentOfFile(File fileName, byte[] content) {
        try {
            FileUtils.writeByteArrayToFile(fileName, content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static URI getAbsolutePath(String path) {
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext();
        Resource resource = appContext.getResource(path);
        URI result = null;
        try {
            result = resource.getURI();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            appContext.close();
        }

        return result;
    }
}
