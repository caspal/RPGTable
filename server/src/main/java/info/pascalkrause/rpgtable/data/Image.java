package info.pascalkrause.rpgtable.data;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Image {

    private String id;
    private String name;
    private String hash;
    private int contentLength;
    @JsonIgnore
    private String path;

    public Image() {
    }

    public Image(String name, String hash, int contentLength, String path) {
        super();
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.hash = hash;
        this.contentLength = contentLength;
        this.path = path;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHash() {
        return hash;
    }

    public int getContentLength() {
        return contentLength;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contentLength;
        result = prime * result + ((hash == null) ? 0 : hash.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Image && hashCode() == obj.hashCode()) {
            return true;
        }
        return false;
    }
}