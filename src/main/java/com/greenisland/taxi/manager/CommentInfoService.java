package com.greenisland.taxi.manager;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.greenisland.taxi.common.BaseHibernateDao;
import com.greenisland.taxi.domain.CommentInfo;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-20下午3:31:31
 */
@Component("commentInfoService")
public class CommentInfoService extends BaseHibernateDao {
	/**
	 * 保存用户评价
	 * 
	 * @param commentInfo
	 * @return
	 */
	@Transactional
	public String saveCommentInfo(CommentInfo commentInfo) {
		return (String) this.getHibernateTemplate().save(commentInfo);
	}

	@Transactional
	public void updateCommentInfo(CommentInfo commentInfo) {
		this.getHibernateTemplate().update(commentInfo);
	}
}
