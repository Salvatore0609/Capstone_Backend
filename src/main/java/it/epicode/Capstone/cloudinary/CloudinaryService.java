package it.epicode.Capstone.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {
    @Autowired
    private  Cloudinary cloudinary;

    public Map<String, String> uploadRawFile(MultipartFile file) {
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "resource_type", "raw",
                    "folder", "Avatar_Archiplanner",
                    "use_filename", true,
                    "unique_filename", false
            );

            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
            System.out.println("Cloudinary upload result: " + result);

            String fileUrl = result.get("secure_url").toString();
            String fileName = file.getOriginalFilename();
            String fileType = file.getContentType();
            String fileSize = String.valueOf(file.getSize());

            Map<String, String> response = new HashMap<>();
            response.put("fileUrl", fileUrl);
            response.put("fileName", fileName);
            response.put("fileType", fileType);
            response.put("fileSize", fileSize);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Errore nell'upload raw su Cloudinary", e);
        }
    }

    public String uploadImage(MultipartFile file) {

        try {
            // folder è il nome della cartella dove l'immagine sarà salvata in cloudinary
            // public_id rappresenta il nome dell'immagine
            Map result = cloudinary.uploader()
                    .upload(file.getBytes(),  Cloudinary.asMap("folder", "Avatar_Archiplanner", "public_id", file.getOriginalFilename()));

            // recupera dalla risposta di cloudinary l'url di visualizzazione dell'immagine
            // che può essere memorizzata in un database
            String url = result.get("secure_url").toString();

            return url;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}