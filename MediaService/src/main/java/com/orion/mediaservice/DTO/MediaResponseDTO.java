package com.orion.mediaservice.DTO;

import com.orion.mediaservice.Entity.Media;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Metadatos de un archivo multimedia almacenado")
public class MediaResponseDTO {

    @Schema(description = "Id del recurso", example = "1")
    private Long id;

    @Schema(description = "Id del usuario propietario del archivo", example = "10")
    private Long userId;

    @Schema(description = "Tipo de media", example = "AVATAR")
    private Media.TipoMedia tipo;

    @Schema(description = "Nombre original del archivo subido", example = "foto.jpg")
    private String nombreOriginal;

    @Schema(description = "Nombre generado internamente para evitar colisiones", example = "b3f1..._foto.jpg")
    private String nombreGenerado;

    @Schema(description = "Ruta relativa para acceder al archivo", example = "/api/media/avatar/b3f1..._foto.jpg")
    private String urlAcceso;

    @Schema(description = "Fecha de subida del archivo")
    private LocalDateTime subidoEl;
}
