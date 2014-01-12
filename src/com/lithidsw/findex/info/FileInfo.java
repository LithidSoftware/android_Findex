package com.lithidsw.findex.info;

import java.io.Serializable;

public class FileInfo implements Serializable {
    public String folder;
    public String path;
    public String name;
    public String type;
    public String storage;
    public String path_old;
    public long modified;
    public long size;
}
