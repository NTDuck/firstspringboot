package com.viettelsoftware.firstspringboot.entities;

import lombok.*;
import javax.persistence.*;

// https://www.javaguides.net/2020/01/spring-boot-mariadb-crud-example-tutorial.html
@Entity
@Table(name = "tasks")
@Getter
@Setter
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Convert(converter = Id_.Converter_.class)
    private @NonNull Task.Id_ id;

    @Column(name = "task", nullable = false)
    private String description;

    // https://github.com/NTDuck/tomfoolery/blob/master/app/src/main/java/org/tomfoolery/core/domain/documents/Document.java
    @Value
    public static class Id_ {
        @NonNull long value;

        @Converter(autoApply = true)
        public static class Converter_ implements AttributeConverter<Id_, @NonNull Long> {
            @Override
            public @NonNull Long convertToDatabaseColumn(@NonNull Task.Id_ id) {
                return id.value;
            }

            @Override
            public @NonNull Task.Id_ convertToEntityAttribute(@NonNull Long value) {
                return new Id_(value);
            }
        }
    }
}
