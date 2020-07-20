package com.patho.main.service;

import com.patho.main.model.PDFContainer;
import com.patho.main.repository.miscellaneous.MediaRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@Transactional
@ConfigurationProperties(prefix = "patho.fax")
@Getter
@Setter
public class FaxService extends AbstractService {

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private MediaRepository mediaRepository;

    private String faxPost;
    private String faxStatus;

    public String sendFax(String faxNumber, PDFContainer attachment) {
        CloseableHttpClient client = HttpClients.createDefault();
        System.out.println(faxNumber + " " + faxPost);
        HttpPost httpPost = new HttpPost(faxPost.replace("$number", processFaxNumber(faxNumber)));

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("pdf", mediaRepository.getBytes(attachment.getPath()));
        System.out.println("sending");

        HttpEntity multipart = builder.build();
        httpPost.setEntity(multipart);

        CloseableHttpResponse response = null;
        try {
            response = client.execute(httpPost);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println(response + " " + response.getEntity());
        return "";
    }

    /**
     * Checks if internal or external number TODO: check if correct number
     *
     * @param number
     * @return
     */
    public static String processFaxNumber(String number) {
        if (number.startsWith("#"))
            return number.replace("#", "");
        else
            return "0" + number;
    }
}
