package com.example.huanbei_dev4.mycustombettercameraapplication;


import java.io.Serializable;

/**
 * 图片对象
 */
public class ImageItem implements Serializable {
    public ImageItem() {

    }

    public ImageItem(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    private static final long serialVersionUID = -7188270558443739436L;
    public String imageId;
    public String thumbnailPath;
    public String sourcePath;
    public boolean isSelected = false;
    public String imageUrl;
    public boolean isRepeatPubish = false;
}
