package com.posteos.Service;

import com.posteos.DTO.PostMapper;
import com.posteos.DTO.PostRequestDTO;
import com.posteos.DTO.PostResponseDTO;
import com.posteos.Entity.Post;
import com.posteos.Exception.ResourceNotFoundException;
import com.posteos.Repository.Repository_Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicePostTest {

    @Mock
    private Repository_Post repo;

    @Mock
    private PostMapper mapper;

    @InjectMocks
    private ServicePost servicePost;

    @Test
    @DisplayName("guardar - request valido - retorna PostResponseDTO")
    void guardar_retornaPostResponseDTO() {
        // Given
        PostRequestDTO request = new PostRequestDTO(1L, "Hola mundo", null);
        Post post = new Post();
        PostResponseDTO response = new PostResponseDTO(1L, 1L, "Hola mundo", null, LocalDateTime.now());
        when(mapper.aEntidad(request)).thenReturn(post);
        when(repo.save(post)).thenReturn(post);
        when(mapper.response(post)).thenReturn(response);

        // When
        PostResponseDTO resultado = servicePost.guardar(request);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).isEqualTo("Hola mundo");
        verify(repo).save(post);
    }

    @Test
    @DisplayName("buscarPorId - id existente - retorna PostResponseDTO")
    void buscarPorId_existente_retornaResponse() {
        // Given
        Long id = 1L;
        Post post = new Post();
        PostResponseDTO response = new PostResponseDTO(id, 1L, "Contenido", null, LocalDateTime.now());
        when(repo.findById(id)).thenReturn(Optional.of(post));
        when(mapper.response(post)).thenReturn(response);

        // When
        PostResponseDTO resultado = servicePost.buscarPorId(id);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("buscarPorId - id no existente - lanza ResourceNotFoundException")
    void buscarPorId_noExistente_lanzaResourceNotFoundException() {
        // Given
        Long id = 99L;
        when(repo.findById(id)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> servicePost.buscarPorId(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post no encontrado");
    }






    @Test
    @DisplayName("eliminar - id existente - llama deleteById")
    void eliminar_existente_llamaDeleteById() {
        // Given
        Long id = 1L;
        Post post = new Post();
        when(repo.findById(id)).thenReturn(Optional.of(post));

        // When
        servicePost.eliminar(id);

        // Then
        verify(repo).deleteById(id);
    }

    @Test
    @DisplayName("eliminar - id no existente - lanza ResourceNotFoundException")
    void eliminar_noExistente_lanzaException() {
        // Given
        Long id = 99L;
        when(repo.findById(id)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> servicePost.eliminar(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post no encontrado");
    }




}
