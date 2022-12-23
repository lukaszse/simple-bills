package pl.com.seremak.simplebills.commons.model;

import lombok.Data;

@Data
public abstract class VersionedEntity {

    private Metadata metadata;
}
