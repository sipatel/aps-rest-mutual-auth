/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.domain;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Email;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A user.
 */
@Entity
@Table(name = "USER_INFO")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @NotNull
    @Size(min = 0, max = 50)
    @Id
    private String login;
    
    @JsonIgnore
    @Size(min = 0, max = 100)
    @Column(name = "user_password")
    private String password;
    
    @Size(min = 0, max = 50)
    @Column(name = "first_name")
    private String firstName;
    
    @Size(min = 0, max = 50)
    @Column(name = "last_name")
    private String lastName;
    
    @Email
    @Size(min = 0, max = 100)
    private String email;
    
    @Column(name = "read_only_user")
    private Boolean readOnlyUser;
    
    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "USER_AUTHORITY",
        joinColumns = {@JoinColumn(name = "login", referencedColumnName = "login")},
        inverseJoinColumns = {@JoinColumn(name = "name", referencedColumnName = "name")})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Authority> authorities;
    
    public String getLogin() {
        return login;
    }
    
    public void setLogin(String login) {
        this.login = login;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Boolean getReadOnlyUser() {
        return readOnlyUser;
    }
    
    public void setReadOnlyUser(Boolean readOnlyUser) {
        this.readOnlyUser = readOnlyUser;
    }
    
    public Set<Authority> getAuthorities() {
        return authorities;
    }
    
    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }
    
    @Override
    public int hashCode() {
        return login.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        User user = (User) o;
        
        if (!login.equals(user.login)) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        return "User{" +
            "login='" + login + '\'' +
            ", password='" + password + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            "}";
    }
}
