package com.orion.mediaservice.Service;


import com.orion.mediaservice.Entity.Media;
import com.orion.mediaservice.Exceptions.EmptyFileException;
import com.orion.mediaservice.Exceptions.ResourceNotFoundException;
import com.orion.mediaservice.Exceptions.StorageException;
import com.orion.mediaservice.Repository.MediaRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    @Autowired
    private MediaRepository mediaRepository;

    @Value("${media.storage.location}")
    private String storageLocation;

    private Path rootLocation;
    private Path avatarLocation;
    private Path postLocation;


    // se ejecuta al iniciar el microservicio para asegurarse de que exista la carpeta
    @PostConstruct
    public void init(){
        try{
            rootLocation = Paths.get(storageLocation);
            avatarLocation = rootLocation.resolve("avatars");
            postLocation = rootLocation.resolve("posts");

            Files.createDirectories(rootLocation);
            Files.createDirectories(avatarLocation);
            Files.createDirectories(postLocation);

        } catch (IOException e) {
            throw new StorageException("No se pudo inicializar la carpeta de almacenamiento", e);
        }

    }


    public Media guardarArchivo(MultipartFile file, Long userId, Media.TipoMedia tipo){

        if (file.isEmpty()){
            log.warn("Intento de subida de archivo vacío por userId={}", userId);
            throw new EmptyFileException("El archivo está vacío");
        }

        String nombreOriginal = file.getOriginalFilename();
        String nombreGenerado = UUID.randomUUID().toString()+ "_" + nombreOriginal;

        // ruta donde se guardara fisicamente el media
        Path carpetaDestino = tipo == Media.TipoMedia.AVATAR ? avatarLocation : postLocation;
        Path destinationFile = carpetaDestino.resolve(nombreGenerado).normalize().toAbsolutePath();

        try {
            // se copia el archivo de la memoria al disco duro
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Fallo al guardar el archivo {} para userId={}", nombreOriginal, userId, e);
            throw new StorageException("No se pudo guardar el archivo: " + nombreOriginal, e);
        }

        //se guarda en base de datos
        Media media = new Media();
        media.setUserId(userId);
        media.setTipo(tipo);
        media.setNombreOriginal(nombreOriginal);
        media.setNombreGenerado(nombreGenerado);
        media.setUrlAcceso("/api/media/"+tipo.name().toLowerCase() + "/" + nombreGenerado);

        Media guardado = mediaRepository.save(media);
        log.info("Archivo {} guardado con id={} para userId={}", nombreGenerado, guardado.getId(), userId);
        return guardado;
    }

    // para poder devolver el archivo cuando se solicite
    public Resource cargarArchivo(String nombreGenerado, Media.TipoMedia tipo){

        Path carpetaOrigen = tipo == Media.TipoMedia.AVATAR ? avatarLocation : postLocation;
        Path file = carpetaOrigen.resolve(nombreGenerado);

        try {
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()){
                return resource;
            } else {
                log.warn("Archivo no encontrado o no legible: {}", nombreGenerado);
                throw new ResourceNotFoundException("No se pudo leer el archivo: " + nombreGenerado);
            }

        } catch (MalformedURLException e){
            log.error("URL de archivo inválida: {}", nombreGenerado, e);
            throw new StorageException("Error al cargar el archivo: " + nombreGenerado, e);
        }

    }

}
