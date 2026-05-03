package com.xms.web.controller.system;

import java.util.List;

import com.google.protobuf.ServiceException;
import com.xms.common.annotation.RepeatSubmit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xms.common.annotation.Log;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.system.domain.SysNotice;
import com.xms.system.service.ISysNoticeService;

/**
 * 公告 信息操作处理
 *
 *
 */
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController extends BaseController {
	@Autowired
	private ISysNoticeService noticeService;
	/**
	 * 获取通知公告列表
	 */
	@PreAuthorize("@ss.hasPermi('system:notice:list')")
	@GetMapping("/list")
	public TableDataInfo list(SysNotice notice) {
		startPage();
		List<SysNotice> list = noticeService.selectNoticeList(notice);
		return getDataTable(list);
	}

	/**
	 * 根据通知公告编号获取详细信息
	 */
	@PreAuthorize("@ss.hasPermi('system:notice:query')")
	@GetMapping(value = "/{noticeId}")
	public AjaxResult getInfo(@PathVariable Long noticeId) {
		return success(noticeService.selectNoticeById(noticeId));
	}

	/**
	 * 新增通知公告
	 */
	@PreAuthorize("@ss.hasPermi('system:notice:add')")
	@Log(title = "通知公告", businessType = BusinessType.INSERT)
	@PostMapping
	@RepeatSubmit
	public AjaxResult add(@Validated @RequestBody SysNotice notice) throws Exception{
		notice.setNoticeType(1);
		if(notice.getNoticeType() == null){
			throw new ServiceException("通知类型不能为空");
		}
		if(notice.getType() == null){
			throw new ServiceException("语言不能为空");
		}
		notice.setCreateBy(getUsername());
		if(!notice.getNoticeType().equals(3)){
			notice.setContentImage(null);
		}
		return toAjax(noticeService.insertNotice(notice));
	}

	/**
	 * 修改通知公告
	 */
	@PreAuthorize("@ss.hasPermi('system:notice:edit')")
	@Log(title = "通知公告", businessType = BusinessType.UPDATE)
	@PutMapping
	@RepeatSubmit
	public AjaxResult edit(@Validated @RequestBody SysNotice notice) throws Exception {
		notice.setNoticeType(1);
		if(notice.getNoticeType() == null){
			throw new ServiceException("通知类型不能为空");
		}
		if(notice.getType() == null){
			throw new ServiceException("语言不能为空");
		}
		notice.setUpdateBy(getUsername());

		if(!notice.getNoticeType().equals(3)){
			notice.setContentImage(null);
		}
		return toAjax(noticeService.updateNotice(notice));
	}

	/**
	 * 删除通知公告
	 */
	@PreAuthorize("@ss.hasPermi('system:notice:remove')")
	@Log(title = "通知公告", businessType = BusinessType.DELETE)
	@DeleteMapping("/{noticeIds}")
	public AjaxResult remove(@PathVariable Long[] noticeIds) {
		return toAjax(noticeService.deleteNoticeByIds(noticeIds));
	}
}
