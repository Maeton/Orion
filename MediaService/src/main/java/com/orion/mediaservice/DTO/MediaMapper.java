package com.orion.mediaservice.DTO;

import com.orion.mediaservice.Entity.Media;
import org.springframework.stereotype.Component;

@Component
public class MediaMapper {

    public MediaResponseDTO response(Media media) {
        MediaResponseDTO dto = new MediaResponseDTO();
        dto.setId(media.getId());
        dto.setUserId(media.getUserId());
        dto.setTipo(media.getTipo());
        dto.setNombreOriginal(media.getNombreOriginal());
        dto.setNombreGenerado(media.getNombreGenerado());
        dto.setUrlAcceso(media.getUrlAcceso());
        dto.setSubidoEl(media.getSubidoEl());
        return dto;
    }
}
