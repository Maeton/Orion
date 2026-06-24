package com.Comentarios.Service;

import com.Comentarios.DTO.ComentarioMapper;
import com.Comentarios.DTO.RequestComentario;
import com.Comentarios.DTO.ResponseComentario;
import com.Comentarios.Entity.Comentario;
import com.Comentarios.Exceptions.ResourceNotFoundException;
import com.Comentarios.Repository.RepositoryComentario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceComentarioTest {

    @Mock
    private RepositoryComentario repo;

    @Mock
    private ComentarioMapper mapper;

    @InjectMocks
    private ServiceComentario serviceComentario;

    @Test
    @DisplayName("guardar - datos validos - retorna ResponseComentario")
    void guardar_retornaResponseComentario() {
        // Given
        Long postId = 1L;
        RequestComentario request = new RequestComentario(postId, 2L, "Excelente post");
        Comentario comentario = new Comentario();
        comentario.setPostId(postId);
        ResponseComentario response = new ResponseComentario(1L, postId, 2L, "Excelente post", LocalDateTime.now());
        when(mapper.aEntidad(request)).thenReturn(comentario);
        when(repo.save(comentario)).thenReturn(comentario);
        when(mapper.response(comentario)).thenReturn(response);

        // When
        ResponseComentario resultado = serviceComentario.guardar(postId, request);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContenido()).isEqualTo("Excelente post");
        verify(repo).save(comentario);
    }

    @Test
    @DisplayName("actualizar - comentario existente - retorna comentario actualizado")
    void actualizar_existente_retornaActualizado() {
        // Given
        Long id = 1L;
        RequestComentario request = new RequestComentario(1L, 2L, "Contenido editado");
        Comentario comentario = new Comentario(id, 1L, 2L, "Original", LocalDateTime.now());
        ResponseComentario response = new ResponseComentario(id, 1L, 2L, "Contenido editado", LocalDateTime.now());
        when(repo.findById(id)).thenReturn(Optional.of(comentario));
        when(repo.save(comentario)).thenReturn(comentario);
        when(mapper.response(comentario)).thenReturn(response);

        // When
        ResponseComentario resultado = serviceComentario.actualizar(id, request);

        // Then
        assertThat(resultado.getContenido()).isEqualTo("Contenido editado");
        verify(repo).save(comentario);
    }

    @Test
    @DisplayName("actualizar - id no existente - lanza ResourceNotFoundException")
    void actualizar_noExistente_lanzaResourceNotFoundException() {
        // Given
        Long id = 99L;
        when(repo.findById(id)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> serviceComentario.actualizar(id, new RequestComentario(1L, 2L, "X")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    @DisplayName("buscarPorPost - postId con comentarios - retorna lista")
    void buscarPorPost_retornaListaDeComentarios() {
        // Given
        Long postId = 1L;
        Comentario c1 = new Comentario(1L, postId, 2L, "C1", LocalDateTime.now());
        Comentario c2 = new Comentario(2L, postId, 3L, "C2", LocalDateTime.now());
        ResponseComentario r1 = new ResponseComentario(1L, postId, 2L, "C1", LocalDateTime.now());
        ResponseComentario r2 = new ResponseComentario(2L, postId, 3L, "C2", LocalDateTime.now());
        when(repo.findByPostId(postId)).thenReturn(Arrays.asList(c1, c2));
        when(mapper.response(c1)).thenReturn(r1);
        when(mapper.response(c2)).thenReturn(r2);

        // When
        List<ResponseComentario> resultado = serviceComentario.buscarPorPost(postId);

        // Then
        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("buscarPorPost - postId sin comentarios - retorna lista vacia")
    void buscarPorPost_sinComentarios_retornaListaVacia() {
        // Given
        Long postId = 1L;
        when(repo.findByPostId(postId)).thenReturn(Collections.emptyList());

        // When
        List<ResponseComentario> resultado = serviceComentario.buscarPorPost(postId);

        // Then
        assertThat(resultado).isEmpty();
    }




}
