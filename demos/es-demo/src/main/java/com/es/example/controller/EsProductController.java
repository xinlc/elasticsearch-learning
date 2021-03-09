package com.es.example.controller;

import com.es.example.common.api.CommonPage;
import com.es.example.common.api.CommonResult;
import com.es.example.domain.EsProduct;
import com.es.example.domain.EsProductRelatedInfo;
import com.es.example.service.EsProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索商品管理Controller
 */
@RestController
@Api(tags = "搜索商品管理")
@RequestMapping("/esProduct")
public class EsProductController {
	@Autowired
	private EsProductService esProductService;

	@ApiOperation(value = "导入所有数据库中商品到ES")
	@PostMapping("/importAll")
	public CommonResult<Integer> importAllList() {
		int count = esProductService.importAll();
		return CommonResult.success(count);
	}

	@ApiOperation(value = "根据id删除商品")
	@GetMapping("/delete/{id}")
	public CommonResult<Object> delete(@PathVariable Long id) {
		esProductService.delete(id);
		return CommonResult.success(null);
	}

	@ApiOperation(value = "根据id批量删除商品")
	@PostMapping("/delete/batch")
	public CommonResult<Object> delete(@RequestParam("ids") List<Long> ids) {
		esProductService.delete(ids);
		return CommonResult.success(null);
	}

	@ApiOperation(value = "根据id创建商品")
	@PostMapping("/create/{id}")
	public CommonResult<EsProduct> create(@PathVariable Long id) {
		EsProduct esProduct = esProductService.create(id);
		if (esProduct != null) {
			return CommonResult.success(esProduct);
		} else {
			return CommonResult.failed();
		}
	}

	@ApiOperation(value = "简单搜索")
	@GetMapping("/search/simple")
	public CommonResult<CommonPage<EsProduct>> search(@RequestParam(required = false) String keyword,
													  @RequestParam(required = false, defaultValue = "0") Integer pageNum,
													  @RequestParam(required = false, defaultValue = "5") Integer pageSize) {
		Page<EsProduct> esProductPage = esProductService.search(keyword, pageNum, pageSize);
		return CommonResult.success(CommonPage.restPage(esProductPage));
	}

	@ApiOperation(value = "综合搜索、筛选、排序")
	@ApiImplicitParam(name = "sort", value = "排序字段:0->按相关度；1->按新品；2->按销量；3->价格从低到高；4->价格从高到低",
			defaultValue = "0", allowableValues = "0,1,2,3,4", paramType = "query", dataType = "integer")
	@GetMapping("/search")
	public CommonResult<CommonPage<EsProduct>> search(@RequestParam(required = false) String keyword,
													  @RequestParam(required = false) Long brandId,
													  @RequestParam(required = false) Long productCategoryId,
													  @RequestParam(required = false, defaultValue = "0") Integer pageNum,
													  @RequestParam(required = false, defaultValue = "5") Integer pageSize,
													  @RequestParam(required = false, defaultValue = "0") Integer sort) {
		Page<EsProduct> esProductPage = esProductService.search(keyword, brandId, productCategoryId, pageNum, pageSize, sort);
		return CommonResult.success(CommonPage.restPage(esProductPage));
	}

	@ApiOperation(value = "根据商品id推荐商品")
	@GetMapping("/recommend/{id}")
	public CommonResult<CommonPage<EsProduct>> recommend(@PathVariable Long id,
														 @RequestParam(required = false, defaultValue = "0") Integer pageNum,
														 @RequestParam(required = false, defaultValue = "5") Integer pageSize) {
		Page<EsProduct> esProductPage = esProductService.recommend(id, pageNum, pageSize);
		return CommonResult.success(CommonPage.restPage(esProductPage));
	}

	@ApiOperation(value = "获取搜索的相关品牌、分类及筛选属性")
	@GetMapping("/search/relate")
	public CommonResult<EsProductRelatedInfo> searchRelatedInfo(@RequestParam(required = false) String keyword) {
		EsProductRelatedInfo productRelatedInfo = esProductService.searchRelatedInfo(keyword);
		return CommonResult.success(productRelatedInfo);
	}
}
