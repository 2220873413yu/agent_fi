package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.NodePackageOrderCancel;
import com.xms.dao.service.INodePackageOrderCancelService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 节点套餐取消订单归档后台查询Controller。
 *
 * <p>该页面只读展示取消归档，不提供新增、编辑、删除能力，避免审计记录被后台误改。</p>
 */
@RestController
@RequestMapping("/xms/nodePackageOrderCancel")
public class NodePackageOrderCancelController extends BaseController {
	@Autowired
	private INodePackageOrderCancelService nodePackageOrderCancelService;

	/**
	 * 查询节点套餐取消订单归档列表。
	 *
	 * <p>支持按原订单、用户、钱包地址、订单来源、原状态和取消时间筛选，用于追溯取消节点时的订单快照。</p>
	 */
	@PreAuthorize("@ss.hasPermi('xms:nodePackageOrderCancel:list')")
	@GetMapping("/list")
	public TableDataInfo list(NodePackageOrderCancel nodePackageOrderCancel) {
		startPage();
		List<NodePackageOrderCancel> list = nodePackageOrderCancelService.selectNodePackageOrderCancelList(nodePackageOrderCancel);
		return getDataTable(list);
	}

	/**
	 * 导出节点套餐取消订单归档列表。
	 *
	 * <p>导出结果保留原订单快照、取消信息和AFI释放暂停快照，方便财务和运营核对。</p>
	 */
	@PreAuthorize("@ss.hasPermi('xms:nodePackageOrderCancel:export')")
	@Log(title = "节点取消订单归档", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, NodePackageOrderCancel nodePackageOrderCancel) {
		List<NodePackageOrderCancel> list = nodePackageOrderCancelService.selectNodePackageOrderCancelList(nodePackageOrderCancel);
		ExcelUtil<NodePackageOrderCancel> util = new ExcelUtil<>(NodePackageOrderCancel.class);
		util.exportExcel(response, list, "节点取消订单归档数据");
	}

	/**
	 * 获取节点套餐取消订单归档详情。
	 *
	 * <p>详情用于排查单笔取消订单的原主表id、释放订单id和取消时AFI释放快照。</p>
	 */
	@PreAuthorize("@ss.hasPermi('xms:nodePackageOrderCancel:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(nodePackageOrderCancelService.getById(id));
	}
}
