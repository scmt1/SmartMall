package com.yami.shop.bean.dto;

import lombok.Data;

@Data
public class FilleData {
	public String sbid;
	public String url;
	public String fileName;
	public String fileType;
	public byte[] bytes;
	public String originalFilename;
}
