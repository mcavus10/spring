package com.example.moodmovies.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationRequestDTO {

    @NotBlank(message = "Kullanıcı adı boş olamaz")
    @Size(min=3,max=25,message = "Kullanıcı adı 3 ile 25 karakter arasında olmalıdır")
    private String name;

    @NotBlank(message = "E-mail is cannot be empty")
    @Email(message = "Please enter a valid email adress")
    @Size(max = 50, message = "Email adress can be up to 50 characters ")
    private String email;

    @NotBlank(message = "Password area cannot be empty")
    @Size(min = 8,message = "Password must at least 8 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Şifre en az 8 karakter uzunluğunda olmalı ve en az bir büyük harf, bir küçük harf, bir rakam ve bir özel karakter içermelidir.")
    private String password;
}
