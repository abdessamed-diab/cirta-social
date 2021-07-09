package dz.cirta.data.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;

@Entity
public class CirtaUser implements UserDetails, Comparable<CirtaUser> {

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @JsonIgnore
   private long id;

   @Column(nullable = true, unique = true, updatable = false)
   @JsonIgnore
   private String facebookId;

   @Column(nullable = false)
   private String firstName;

   @Column(nullable = false)
   private String lastName;

   @Column(nullable = false, unique = true, updatable = true)
   private String name;

   @Column
   @JsonIgnore
   private String password;

   @JsonIgnore
   transient private boolean accountNonExpired = true;

   @JsonIgnore
   transient private boolean accountNonLocked = true;

   @JsonIgnore
   transient private boolean credentialsNonExpired = true;

   @JsonIgnore
   transient private boolean enabled = true;

   @Column
   private byte language;

   @ManyToMany(fetch = FetchType.EAGER)
   @JoinTable(
         name = "user_authority",
         joinColumns = @JoinColumn(name = "cirta_user_id"),
         inverseJoinColumns = @JoinColumn(name = "cirta_authority_id")
   )
   @JsonIgnore
   private Set<CirtaAuthority> authorities;

   @OneToOne(cascade = {CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE}, fetch = FetchType.LAZY, orphanRemoval = true)
   @JoinColumn(name = "user_temp_auth", referencedColumnName = "id", updatable = true)
   @JsonIgnore
   private TempAuthentication tempAuthentication;

   @Column(length = 500000)
   @JsonIgnore
   private String profileImage;

   public CirtaUser() {
   }

   public CirtaUser(long id) {
      this.id = id;
   }

   public CirtaUser(String facebookId, String firstName, String lastName, String name) {
      this.facebookId = facebookId;
      this.firstName = firstName;
      this.lastName = lastName;
      this.name = name;
   }


   @Override
   public String toString() {
      return getClass().getSimpleName() + ":" + id;
   }

   @Override
   public Collection<? extends GrantedAuthority> getAuthorities() {
      return authorities;
   }

   public void setAuthorities(Set<CirtaAuthority> authorities) {
      this.authorities = authorities;
   }

   public TempAuthentication getTempAuthentication() {
      return tempAuthentication;
   }

   public void setTempAuthentication(TempAuthentication tempAuthentication) {
      this.tempAuthentication = tempAuthentication;
   }

   @Override
   public String getPassword() {
      return password;
   }

   @Override
   public String getUsername() {
      return name;
   }

   @Override
   public boolean isAccountNonExpired() {
      return accountNonExpired;
   }

   @Override
   public boolean isAccountNonLocked() {
      return accountNonLocked;
   }

   @Override
   public boolean isCredentialsNonExpired() {
      return credentialsNonExpired;
   }

   @Override
   public boolean isEnabled() {
      return enabled;
   }

   public long getId() {
      return id;
   }

   public String getFacebookId() {
      return facebookId;
   }

   public void setUserName(String username) {
      this.name = username;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   public void setLastName(String lastName) {
      this.lastName = lastName;
   }

   public byte getLanguage() {
      return language;
   }

   public void setLanguage(byte language) {
      this.language = language;
   }

   @Override
   public int compareTo(CirtaUser comparedTo) {
      return name.compareTo(comparedTo.name);
   }

   @Override
   public boolean equals(Object toObj) {
      if (toObj == null || !toObj.getClass().isAssignableFrom(CirtaUser.class)) {
         return false;
      }

      CirtaUser comparedTo = (CirtaUser) toObj;

      return Long.valueOf(id).equals(comparedTo.id);
   }

   public String getProfileImage() {
      return profileImage;
   }

   public void setProfileImage(String profileImage) {
      this.profileImage = profileImage;
   }

   public String getFirstName() {
      return firstName;
   }

   public String getLastName() {
      return lastName;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }
}
