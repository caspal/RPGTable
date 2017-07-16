package info.pascalkrause.rpgtable.data;

import java.util.Arrays;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Image {

    String id;
    String name;
    String hash;
    int contentLength;
    @JsonIgnore
    byte[] content;
    String mediaType;

    public Image() {
    }

    public Image(String name, String hash, int contentLength, byte[] content, String mediaType) {
        super();
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.hash = hash;
        this.contentLength = contentLength;
        this.content = content;
        this.mediaType = mediaType;
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

    public byte[] getContent() {
        return content;
    }

    public String getMediaType() {
        return mediaType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contentLength;
        result = prime * result + ((hash == null) ? 0 : hash.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((content == null) ? 0 : Arrays.hashCode(content));
        result = prime * result + ((mediaType == null) ? 0 : mediaType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Image && hashCode() == obj.hashCode()) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Image [id=" + id + ", name=" + name + ", hash=" + hash + ", contentLength=" + contentLength
                + ", content(Hashcode)=" + Arrays.hashCode(content) + ", mediaType=" + mediaType + "]";
    }
}