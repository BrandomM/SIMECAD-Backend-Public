package com.simecad.simecad.service.Impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.simecad.simecad.domain.Producto;
import com.simecad.simecad.domain.Usuario;
import com.simecad.simecad.service.FileStorageService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Qualifier("aws")
public class AwsStorageService implements FileStorageService {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Override
    public String almacenarFotoProducto(Producto producto, MultipartFile foto) {
        String id = producto.getId().toString();
        String extension = getExtension(foto.getOriginalFilename());
        String newFilename = System.currentTimeMillis() + "_p" + id + "." + extension;

        try {
            File mainFile = File.createTempFile("pro", null);
            FileOutputStream stream = new FileOutputStream(mainFile);
            stream.write(foto.getBytes());
            System.out.println("Subiendo archivo con el nombre... " + newFilename);
            PutObjectRequest request = new PutObjectRequest(bucketName, newFilename, mainFile).withCannedAcl(CannedAccessControlList.PublicRead);
            amazonS3.putObject(request);
            String url = amazonS3.getUrl(bucketName, newFilename).toString();
            mainFile.delete();
            return url;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return "Something went wrong";
        }
    }

    @Override
    public String almacenarFotoUsuario(Usuario usuario, MultipartFile foto) {
        String id = usuario.getId().toString();
        String extension = getExtension(foto.getOriginalFilename());
        String newFilename = System.currentTimeMillis() + "_u" + id + "." + extension;

        try {
            File mainFile = File.createTempFile("usr", null);
            FileOutputStream stream = new FileOutputStream(mainFile);
            stream.write(foto.getBytes());
            System.out.println("Subiendo archivo con el nombre... " + newFilename);
            PutObjectRequest request = new PutObjectRequest(bucketName, newFilename, mainFile).withCannedAcl(CannedAccessControlList.PublicRead);
            amazonS3.putObject(request);
            String url = amazonS3.getUrl(bucketName, newFilename).toString();
            return url;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return "Something went wrong";
        }
    }

    private String getExtension(String filename) {
        String extension = "";
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i + 1);
        }
        return extension;
    }

}
