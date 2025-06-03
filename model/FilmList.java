package com.example.moodmovies.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Entity
@Table(name = "MOODMOVIES_FILMS_LIST") // DB'deki PK tanımı: PRIMARY KEY (LIST_ID)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilmList {

    @Id
    @GeneratedValue(generator = "list_id_generator")
    @GenericGenerator(
        name = "list_id_generator",
        strategy = "com.example.moodmovies.config.ListIdGenerator" // Tam yolu belirtin
    )
    @Column(name = "LIST_ID", length = 15)
    private String listId;

    @ManyToOne(fetch = FetchType.LAZY) // Bir liste bir kullanıcıya aittir.
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @NotNull(message = "Liste durumu (status) boş olamaz.")
    @Column(name = "STATUS", nullable = false)
    private Integer status; // Örneğin: 1=Aktif, 0=Arşivlenmiş

    @NotBlank(message = "Liste adı boş olamaz.")
    @Size(max = 100, message = "Liste adı en fazla 100 karakter olabilir.")
    @Column(name = "NAME", length = 100, nullable = false)
    private String name;

    @NotBlank(message = "Liste etiketi (tag) boş olamaz.") // DB'de NOT NULL olarak tanımladık
    @Size(max = 50, message = "Etiket en fazla 50 karakter olabilir.") // DB'deki VARCHAR(50) ile uyumlu
    @Column(name = "TAG", length = 50, nullable = false)
    private String tag;

    @NotNull(message = "Görünürlük (visible) durumu boş olamaz.")
    @Column(name = "VISIBLE", nullable = false)
    private Integer visible; // Örneğin: 0=Sadece Ben (Private), 1=Herkes (Public)

    @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir.")
    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "CREATED", nullable = false, updatable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "LAST_UPD")
    private LocalDateTime lastUpd;

    // Bir listenin içinde birden fazla film olabilir.
    // Liste silindiğinde, o listedeki filmler de (FilmListInfo kayıtları) silinsin (cascade, orphanRemoval).
    @OneToMany(mappedBy = "filmList", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // Lombok Builder kullanırken koleksiyonu initialize etmek için.
    private List<FilmListInfo> filmListInfos = new ArrayList<>();

    // Helper metotlar (İlişkinin her iki tarafını da senkronize etmek için kullanışlıdır)
    public void addFilm(FilmInfo filmInfoEntity, User listOwner) { // FilmInfo yerine filmId String olarak da alınabilir
        FilmListInfoId filmListInfoId = new FilmListInfoId(this.listId, filmInfoEntity.getId());
        FilmListInfo newFilmInList = FilmListInfo.builder()
                .id(filmListInfoId)
                .filmList(this)
                .user(listOwner) // FilmListInfo'daki user'ı listenin sahibi olarak ayarla
                .build();
        if (!this.filmListInfos.contains(newFilmInList)) { // equals/hashCode FilmListInfoId'de tanımlı olmalı
            this.filmListInfos.add(newFilmInList);
        }
    }

    public void removeFilm(String filmId) {
        FilmListInfoId filmListInfoIdToRemove = new FilmListInfoId(this.listId, filmId);
        this.filmListInfos.removeIf(fli -> fli.getId().equals(filmListInfoIdToRemove));
    }

    // Lombok @EqualsAndHashCode'u dikkatli kullanmak gerekebilir,
    // özellikle @OneToMany/@ManyToOne ilişkileri olan entity'lerde sonsuz döngüye yol açabilir.
    // Genellikle sadece @Id alanı üzerinden yapılır veya manuel implemente edilir.
    // Şimdilik Lombok'un varsayılanını bırakıyorum, sorun olursa düzenleriz.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilmList filmList = (FilmList) o;
        return Objects.equals(listId, filmList.listId); // Sadece ID üzerinden eşitlik kontrolü
    }

    @Override
    public int hashCode() {
        return Objects.hash(listId); // Sadece ID üzerinden hashCode
    }
}