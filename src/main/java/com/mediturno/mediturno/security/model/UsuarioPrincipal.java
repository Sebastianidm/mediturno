package com.mediturno.mediturno.security.model;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mediturno.mediturno.modules.user.model.Usuario;

public class UsuarioPrincipal implements UserDetails {

    private final Usuario usuario;

    public UsuarioPrincipal(Usuario usuario){
        this.usuario = usuario;
    }

    // 1. Mapeamos los roles de la BD a las autoridades SS
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
                .collect(Collectors.toList());
    }

    // 2. We Return the encrypted password
    @Override
    public String getPassword() {
        return usuario.getPassword();
    }  

    // 3. We use the email like our "username" or access credential
    @Override
    public String getUsername(){
        return usuario.getEmail();
    }

    // 4. Acount acces control 
    @Override
    public boolean isAccountNonExpired(){
        return true; // We do'nt handle acount expiration yet
    }

    @Override
    public boolean isAccountNonLocked(){
        return true; // We don't handle acount block yet
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true; // We don't handle expiration of credential yet
    }

    @Override
    public boolean isEnabled(){
        return usuario.getActivo(); // If user is active on BD, the acount is enable
    }

    // A useful method in case we need to retrieve the user's full data later on
    public Usuario getUsuario(){
        return this.usuario;
    }   
}
