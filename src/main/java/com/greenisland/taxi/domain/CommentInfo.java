package com.greenisland.taxi.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.annotations.GenericGenerator;

import com.greenisland.taxi.common.utils.CustomDateSerializer;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-7-30上午10:43:27
 */
@Entity
@Table(name = "ts_comment")
public class CommentInfo {
	private String id;
	private Integer level;
	private String content;
	private CallApplyInfo applyInfo;
	private Date createDate;
	private Date updateDate;

	@Id
	@GenericGenerator(name = "idGenerator", strategy = "uuid")
	@GeneratedValue(generator = "idGenerator")
	@Column(name = "ID_")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "COMMENT_LEVEL_")
	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	@Column(name = "COMMENT_CONTENT_")
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public CommentInfo() {
		super();
	}

	@Column(name = "CREATE_DATE_")
	@JsonSerialize(using = CustomDateSerializer.class)
	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	@Column(name = "UPDATE_DATE_")
	@JsonSerialize(using = CustomDateSerializer.class)
	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	
	@OneToOne(mappedBy = "commentInfo")
	public CallApplyInfo getApplyInfo() {
		return applyInfo;
	}

	public void setApplyInfo(CallApplyInfo applyInfo) {
		this.applyInfo = applyInfo;
	}

	public CommentInfo(String id, Integer level, String content, Date createDate, Date updateDate) {
		super();
		this.id = id;
		this.level = level;
		this.content = content;
		this.createDate = createDate;
		this.updateDate = updateDate;
	}

}
