package com.orion.Grupos_service.Service;

import com.orion.Grupos_service.Dto.MapperGrupo;
import com.orion.Grupos_service.Dto.RequestGrupo;
import com.orion.Grupos_service.Dto.ResponseGrupo;
import com.orion.Grupos_service.Entity.Grupo;
import com.orion.Grupos_service.Repository.Repository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceGrupoTest {

    @Mock
    private Repository repo;

    @Mock
    private MapperGrupo mapper;

    @InjectMocks
    private ServiceGrupo serviceGrupo;




    @Test
    @DisplayName("obtenerTodos - sin grupos - retorna lista vacia")
    void obtenerTodos_sinGrupos_retornaListaVacia() {
        // Given
        when(repo.findAll()).thenReturn(new ArrayList<>());

        // When
        List<ResponseGrupo> resultado = serviceGrupo.obtenerTodos();

        // Then
        assertThat(resultado).isEmpty();
    }



    @Test
    @DisplayName("obtenerPorId - id no existente - lanza RuntimeException")
    void obtenerPorId_noExistente_lanzaException() {
        // Given
        Long id = 99L;
        when(repo.findById(id)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> serviceGrupo.obtenerPorId(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Grupo no encontrado");
    }



    @Test
    @DisplayName("actualizar - ejecutado por otro usuario - lanza RuntimeException")
    void actualizar_porOtroUsuario_lanzaException() {
        // Given
        Long id = 1L;
        Long idCreador = 5L;
        Long idOtro = 99L;
        Grupo grupo = new Grupo();
        grupo.setIdCreador(idCreador);
        when(repo.findById(id)).thenReturn(Optional.of(grupo));

        // When / Then
        assertThatThrownBy(() -> serviceGrupo.actualizar(id, new RequestGrupo("X", "Y"), idOtro))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No tienes permiso");
    }




    @Test
    @DisplayName("unirseAGrupo - usuario nuevo - agrega como miembro")
    void unirseAGrupo_exitoso_agregaMiembro() {
        // Given
        Long idGrupo = 1L;
        Long idUsuario = 10L;
        Grupo grupo = new Grupo();
        grupo.setMiembros(new ArrayList<>());
        ResponseGrupo response = new ResponseGrupo(idGrupo, 1L, "G", "D", new ArrayList<>(), null);
        when(repo.findById(idGrupo)).thenReturn(Optional.of(grupo));
        when(repo.save(grupo)).thenReturn(grupo);
        when(mapper.toResponse(grupo)).thenReturn(response);

        // When
        serviceGrupo.unirseAGrupo(idGrupo, idUsuario);

        // Then
        assertThat(grupo.getMiembros()).contains(idUsuario);
        verify(repo).save(grupo);
    }

    @Test
    @DisplayName("unirseAGrupo - usuario ya es miembro - lanza RuntimeException")
    void unirseAGrupo_yaEsMiembro_lanzaException() {
        // Given
        Long idGrupo = 1L;
        Long idUsuario = 10L;
        Grupo grupo = new Grupo();
        grupo.setMiembros(new ArrayList<>(List.of(idUsuario)));
        when(repo.findById(idGrupo)).thenReturn(Optional.of(grupo));

        // When / Then
        assertThatThrownBy(() -> serviceGrupo.unirseAGrupo(idGrupo, idUsuario))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya estás registrado");
    }
}
