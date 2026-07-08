package com.orion.mediaservice.Controller;


import com.orion.mediaservice.DTO.MediaMapper;
import com.orion.mediaservice.DTO.MediaResponseDTO;
import com.orion.mediaservice.Entity.Media;
import com.orion.mediaservice.Service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequestMapping("/api/media")
@Tag(name = "Media", description = "Subida y consulta de archivos multimedia (avatares y fotos de post)")
public class MediaController {

    @Autowired
    private StorageService storageService;

    @Autowired
    private MediaMapper mapper;

    @PostMapping("/avatar/upload")
    @Operation(summary = "Sube el avatar del usuario autenticado")
    @ApiResponse(responseCode = "201", description = "Avatar guardado")
    @ApiResponse(responseCode = "400", description = "Archivo vacío o inválido")
    public ResponseEntity<MediaResponseDTO> subirAvatar(@RequestParam("file") MultipartFile file){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        Media mediaGuardada = storageService.guardarArchivo(file, userId, Media.TipoMedia.AVATAR);
        MediaResponseDTO dto = mapper.response(mediaGuardada);
        return ResponseEntity.created(URI.create(dto.getUrlAcceso())).body(dto);
    }


    @GetMapping("/avatar/{nombreGenerado}")
    @Operation(summary = "Obtiene un avatar por su nombre generado")
    @ApiResponse(responseCode = "200", description = "Archivo encontrado")
    @ApiResponse(responseCode = "404", description = "Archivo no encontrado")
    public ResponseEntity<Resource> verAvatar(@PathVariable String nombreGenerado){
        Resource file = storageService.cargarArchivo(nombreGenerado,Media.TipoMedia.AVATAR);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"inline; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.IMAGE_JPEG)
                .body(file);

    }


    // Endpoints para posts

    @PostMapping("/post/upload")
    @Operation(summary = "Sube una foto asociada a un post")
    @ApiResponse(responseCode = "201", description = "Foto guardada")
    @ApiResponse(responseCode = "400", description = "Archivo vacío o inválido")
    public ResponseEntity<MediaResponseDTO> subirFotoPost(@RequestParam("file") MultipartFile file){

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        Media mediaGuardada = storageService.guardarArchivo(file, userId, Media.TipoMedia.POST);
        MediaResponseDTO dto = mapper.response(mediaGuardada);
        return ResponseEntity.created(URI.create(dto.getUrlAcceso())).body(dto);

    }


    @GetMapping("/post/{nombreGenerado}")
    @Operation(summary = "Obtiene una foto de post por su nombre generado")
    @ApiResponse(responseCode = "200", description = "Archivo encontrado")
    @ApiResponse(responseCode = "404", description = "Archivo no encontrado")
    public ResponseEntity<Resource> verFotoPost(@PathVariable String nombreGenerado){

        Resource file = storageService.cargarArchivo(nombreGenerado,Media.TipoMedia.POST);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"inline; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.IMAGE_JPEG)
                .body(file);

    }


}
