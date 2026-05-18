package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.NodePackageReleaseOrder;
import com.xms.dao.service.INodePackageReleaseOrderService;
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
 * 节点认购AFI线性释放订单后台查询Controller。
 *
 * <p>该页面只用于查看和导出释放计划，不提供新增、编辑、删除能力，避免后台误改释放金额和释放进度。</p>
 */
@RestController
@RequestMapping("/xms/nodePackageReleaseOrder")
public class NodePackageReleaseOrderController extends BaseController {
	@Autowired
	private INodePackageReleaseOrderService nodePackageReleaseOrderService;

	/**
	 * 查询节点认购AFI线性释放订单列表。
	 *
	 * <p>支持按用户、钱包地址、来源节点订单、节点等级、释放状态和初始化批次筛选，列表结果用于节点管理菜单展示。</p>
	 */
	@PreAuthorize("@ss.hasPermi('xms:nodePackageReleaseOrder:list')")
	@GetMapping("/list")
	public TableDataInfo list(NodePackageReleaseOrder nodePackageReleaseOrder) {
		startPage();
		List<NodePackageReleaseOrder> list = nodePackageReleaseOrderService.selectNodePackageReleaseOrderList(nodePackageReleaseOrder);
		return getDataTable(list);
	}

	/**
	 * 导出节点认购AFI线性释放订单列表。
	 *
	 * <p>导出的字段与列表查询口径一致，方便财务核对总释放、每日释放、已释放和剩余释放金额。</p>
	 */
	@PreAuthorize("@ss.hasPermi('xms:nodePackageReleaseOrder:export')")
	@Log(title = "节点线性释放订单", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, NodePackageReleaseOrder nodePackageReleaseOrder) {
		List<NodePackageReleaseOrder> list = nodePackageReleaseOrderService.selectNodePackageReleaseOrderList(nodePackageReleaseOrder);
		ExcelUtil<NodePackageReleaseOrder> util = new ExcelUtil<>(NodePackageReleaseOrder.class);
		util.exportExcel(response, list, "节点线性释放订单数据");
	}

	/**
	 * 获取节点认购AFI线性释放订单详情。
	 *
	 * <p>详情用于排查单笔来源节点订单的权重快照、释放进度和最后释放日期。</p>
	 */
	@PreAuthorize("@ss.hasPermi('xms:nodePackageReleaseOrder:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(nodePackageReleaseOrderService.getById(id));
	}
}
