package com.orion.Usuarios.Service;

import com.orion.Usuarios.DTO.RegisterRequest;
import com.orion.Usuarios.Entity.Rol;
import com.orion.Usuarios.Entity.Usuario;
import com.orion.Usuarios.Entity.UsuarioPerfil;
import com.orion.Usuarios.Exception.ResourceAlreadyExistsException;
import com.orion.Usuarios.Exception.ResourceNotFoundException;
import com.orion.Usuarios.Repository.RolRepository;
import com.orion.Usuarios.Repository.UserProfileRepository;
import com.orion.Usuarios.Repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("registrarUsuario - datos nuevos - guarda y retorna el usuario")
    void registrar_nuevo_exitoso_guardaYRetornaUsuario() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("mateo");
        request.setEmail("mateo@orion.cl");
        request.setPassword("pass123");
        request.setBiografia("Dev backend");
        request.setUbicacion("Santiago");

        Rol rolUser = new Rol();
        rolUser.setNombre("ROLE_USER");

        Usuario usuarioGuardado = new Usuario();
        usuarioGuardado.setUsername("mateo");

        when(usuarioRepository.existsByUsername("mateo")).thenReturn(false);
        when(usuarioRepository.existsByEmail("mateo@orion.cl")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("$2a$hash");
        when(rolRepository.findByNombre("ROLE_USER")).thenReturn(Optional.of(rolUser));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        // When
        Usuario resultado = usuarioService.registrarUsuario(request);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsername()).isEqualTo("mateo");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("registrarUsuario - username ya existe - lanza ResourceAlreadyExistsException")
    void registrar_usernameExistente_lanzaResourceAlreadyExistsException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("mateo");
        request.setEmail("nuevo@orion.cl");
        request.setPassword("pass");
        when(usuarioRepository.existsByUsername("mateo")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> usuarioService.registrarUsuario(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("mateo");
    }

    @Test
    @DisplayName("registrarUsuario - email ya existe - lanza ResourceAlreadyExistsException")
    void registrar_emailExistente_lanzaResourceAlreadyExistsException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("nuevo");
        request.setEmail("mateo@orion.cl");
        request.setPassword("pass");
        when(usuarioRepository.existsByUsername("nuevo")).thenReturn(false);
        when(usuarioRepository.existsByEmail("mateo@orion.cl")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> usuarioService.registrarUsuario(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("correo");
    }

    @Test
    @DisplayName("eliminarUsuario - id existente - llama deleteById")
    void eliminar_existente_llamaDeleteById() {
        // Given
        Long id = 1L;
        when(usuarioRepository.existsById(id)).thenReturn(true);

        // When
        usuarioService.eliminarUsuario(id);

        // Then
        verify(usuarioRepository).deleteById(id);
    }

    @Test
    @DisplayName("eliminarUsuario - id no existente - lanza ResourceNotFoundException")
    void eliminar_noExistente_lanzaResourceNotFoundException() {
        // Given
        Long id = 99L;
        when(usuarioRepository.existsById(id)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> usuarioService.eliminarUsuario(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("actualizarUrlAvatar - perfil existente - actualiza y retorna perfil")
    void actualizarUrlAvatar_existente_actualizaYRetornaPerfil() {
        // Given
        Long id = 1L;
        String nuevaUrl = "/api/media/avatar/nueva.png";
        UsuarioPerfil perfil = new UsuarioPerfil();
        perfil.setAvatarUrl("/api/media/avatar/vieja.png");
        when(userProfileRepository.findByUsuarioId(id)).thenReturn(Optional.of(perfil));
        when(userProfileRepository.save(perfil)).thenReturn(perfil);

        // When
        UsuarioPerfil resultado = usuarioService.actualizarUrlAvatar(id, nuevaUrl);

        // Then
        assertThat(resultado.getAvatarUrl()).isEqualTo(nuevaUrl);
        verify(userProfileRepository).save(perfil);
    }

    @Test
    @DisplayName("obtenerUsuarioPerfilPorId - id existente - retorna UsuarioPerfil")
    void obtenerUsuarioPerfilPorId_existente_retornaPerfil() {
        // Given
        Long id = 1L;
        UsuarioPerfil perfil = new UsuarioPerfil();
        perfil.setBiografia("Bio de prueba");
        when(userProfileRepository.findByUsuarioId(id)).thenReturn(Optional.of(perfil));

        // When
        UsuarioPerfil resultado = usuarioService.obtenerUsuarioPerfilPorId(id);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getBiografia()).isEqualTo("Bio de prueba");
    }

    @Test
    @DisplayName("obtenerUsuarioPerfilPorId - id no existente - lanza RuntimeException")
    void obtenerUsuarioPerfilPorId_noExistente_lanzaException() {
        // Given
        Long id = 99L;
        when(userProfileRepository.findByUsuarioId(id)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> usuarioService.obtenerUsuarioPerfilPorId(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrado");
    }
}
