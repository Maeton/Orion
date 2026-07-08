package com.orion.mediaservice.Service;

import com.orion.mediaservice.Entity.Media;
import com.orion.mediaservice.Exceptions.EmptyFileException;
import com.orion.mediaservice.Exceptions.ResourceNotFoundException;
import com.orion.mediaservice.Repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @InjectMocks
    private StorageService storageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storageService, "storageLocation", tempDir.toString());
        storageService.init();
    }

    @Test
    @DisplayName("guardarArchivo - avatar valido - lo persiste y guarda en disco")
    void guardarArchivo_avatarValido_persisteYGuardaEnDisco() {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", "contenido".getBytes());
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> {
            Media media = invocation.getArgument(0);
            media.setId(1L);
            return media;
        });

        // When
        Media resultado = storageService.guardarArchivo(file, 5L, Media.TipoMedia.AVATAR);

        // Then
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getUserId()).isEqualTo(5L);
        assertThat(resultado.getNombreOriginal()).isEqualTo("foto.jpg");
        assertThat(resultado.getUrlAcceso()).startsWith("/api/media/avatar/");
        assertThat(tempDir.resolve("avatars").resolve(resultado.getNombreGenerado())).exists();
        verify(mediaRepository).save(any(Media.class));
    }

    @Test
    @DisplayName("guardarArchivo - foto de post valida - la guarda en la carpeta posts")
    void guardarArchivo_fotoPost_guardaEnCarpetaPosts() {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "post.png", "image/png", "contenido".getBytes());
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Media resultado = storageService.guardarArchivo(file, 7L, Media.TipoMedia.POST);

        // Then
        assertThat(resultado.getUrlAcceso()).startsWith("/api/media/post/");
        assertThat(tempDir.resolve("posts").resolve(resultado.getNombreGenerado())).exists();
    }

    @Test
    @DisplayName("guardarArchivo - archivo vacio - lanza EmptyFileException y no persiste")
    void guardarArchivo_archivoVacio_lanzaExcepcion() {
        // Given
        MockMultipartFile vacio = new MockMultipartFile("file", "vacio.jpg", "image/jpeg", new byte[0]);

        // When / Then
        assertThatThrownBy(() -> storageService.guardarArchivo(vacio, 5L, Media.TipoMedia.AVATAR))
                .isInstanceOf(EmptyFileException.class);

        verify(mediaRepository, never()).save(any(Media.class));
    }

    @Test
    @DisplayName("cargarArchivo - archivo existente - retorna un Resource legible")
    void cargarArchivo_archivoExistente_retornaResource() {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "contenido".getBytes());
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Media guardado = storageService.guardarArchivo(file, 9L, Media.TipoMedia.AVATAR);

        // When
        Resource resource = storageService.cargarArchivo(guardado.getNombreGenerado(), Media.TipoMedia.AVATAR);

        // Then
        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
    }

    @Test
    @DisplayName("cargarArchivo - archivo inexistente - lanza ResourceNotFoundException")
    void cargarArchivo_archivoInexistente_lanzaExcepcion() {
        // When / Then
        assertThatThrownBy(() -> storageService.cargarArchivo("no-existe.jpg", Media.TipoMedia.POST))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
