package com.zscat.mall.portal.controller;


import com.zscat.cms.model.CmsSubject;
import com.zscat.cms.service.CmsSubjectService;
import com.zscat.common.annotation.IgnoreAuth;
import com.zscat.common.result.CommonResult;
import com.zscat.mall.portal.constant.RedisKey;
import com.zscat.mall.portal.entity.MemberProductCollection;
import com.zscat.mall.portal.repository.MemberProductCollectionRepository;
import com.zscat.mall.portal.service.HomeService;
import com.zscat.mall.portal.service.RedisService;
import com.zscat.mall.portal.util.JsonUtil;
import com.zscat.mall.portal.vo.GeetInit;
import com.zscat.mall.portal.vo.GeetestLib;
import com.zscat.mall.portal.vo.HomeContentResult;
import com.zscat.mall.portal.vo.HomeFlashPromotion;
import com.zscat.pms.dto.PmsProductQueryParam;
import com.zscat.pms.model.PmsProduct;
import com.zscat.pms.model.PmsProductAttributeCategory;
import com.zscat.pms.model.PmsProductCategory;
import com.zscat.pms.service.*;
import com.zscat.ums.model.SmsCoupon;
import com.zscat.ums.model.SmsHomeAdvertise;
import com.zscat.ums.model.SmsHomeAdvertiseExample;
import com.zscat.ums.model.UmsMember;
import com.zscat.ums.service.SmsHomeAdvertiseService;
import com.zscat.ums.service.UmsMemberCouponService;
import com.zscat.ums.service.UmsMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * 首页内容管理Controller
 * Created by zscat on 2019/1/28.
 */
@RestController
@Api(tags = "HomeController", description = "首页内容管理")
@RequestMapping("/api/home")
public class HomeController extends ApiBaseAction {
    @Resource
    private HomeService homeService;
    @Resource
    UmsMemberCouponService umsMemberCouponService;
    @Resource
    private PmsProductAttributeCategoryService productAttributeCategoryService;
    @Resource
    private SmsHomeAdvertiseService advertiseService;
    @Resource
    private PmsProductService pmsProductService;
    @Resource
    private PmsProductAttributeService productAttributeService;

    @Resource
    private PmsProductCategoryService productCategoryService;
    @Resource
    private CmsSubjectService subjectService;
    @Resource
    private RedisService redisService;
    @Resource
    private UmsMemberService memberService;
    @Resource
    private MemberProductCollectionRepository productCollectionRepository;
    @Resource
    private PmsBrandService brandService;
    @IgnoreAuth
    @ApiOperation("首页内容页信息展示")
    @RequestMapping(value = "/content", method = RequestMethod.GET)
    public Object content() {
        HomeContentResult contentResult = null;
        String bannerJson = redisService.get(RedisKey.HomeContentResult);
        if(bannerJson!=null){
            contentResult = JsonUtil.jsonToPojo(bannerJson,HomeContentResult.class);
        }else {
            HomeContentResult result = new HomeContentResult();
            //获取首页广告
            SmsHomeAdvertiseExample exampleAdv = new SmsHomeAdvertiseExample();
            exampleAdv.createCriteria().andTypeEqualTo(1).andStatusEqualTo(1);
            exampleAdv.setOrderByClause("sort desc");
            result.setAdvertiseList(advertiseService.selectByExample(exampleAdv));
            //获取推荐品牌
            result.setBrandList(brandService.listBrand(null,1,4));
            //获取秒杀信息
           // result.setHomeFlashPromotion(getHomeFlashPromotion());
            PmsProductQueryParam newQueryParam = new PmsProductQueryParam();
            newQueryParam.setPageNum(1);newQueryParam.setPageSize(4);
            List<PmsProduct> productList = pmsProductService.list(newQueryParam);
            //获取新品推荐
            result.setNewProductList(productList);
            //获取人气推荐
            result.setHotProductList(productList);
            //获取推荐专题
            result.setSubjectList(subjectService.list(null,1,4));
            List<PmsProductAttributeCategory> productAttributeCategoryList = productAttributeCategoryService.getList(10, 1);

            for (PmsProductAttributeCategory gt : productAttributeCategoryList) {
                PmsProductQueryParam productQueryParam = new PmsProductQueryParam();
                productQueryParam.setPageSize(6);
                productQueryParam.setPageNum(1);
                productQueryParam.setProductAttributeCategoryId(gt.getId());
                List<PmsProduct> goodsList = pmsProductService.list(productQueryParam);
                if (goodsList!=null && goodsList.size()>0){
                    PmsProduct pmsProduct = goodsList.get(0);
                    PmsProduct product =  new PmsProduct();
                    BeanUtils.copyProperties(pmsProduct, product);
                    product.setType(1);
                    goodsList.add(product);
                }
                gt.setGoodsList(goodsList);
            }
            result.setCat_list(productAttributeCategoryList);

            redisService.set(RedisKey.HomeContentResult,JsonUtil.objectToJson(contentResult));
            redisService.expire(RedisKey.HomeContentResult,24*60*60);
        }
        return new CommonResult().success(contentResult);
    }


    @IgnoreAuth
    @ApiOperation("分页获取推荐商品")
    @RequestMapping(value = "/recommendProductList", method = RequestMethod.GET)
    public Object recommendProductList(@RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize,
                                       @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        PmsProductQueryParam productQueryParam = new PmsProductQueryParam();
        productQueryParam.setPageNum(pageNum);productQueryParam.setPageSize(pageSize);
        List<PmsProduct> productList = pmsProductService.list(productQueryParam);
        return new CommonResult().success(productList);
    }
    @IgnoreAuth
    @ApiOperation("分页获取推荐商品")
    @RequestMapping(value = "/hotProductList", method = RequestMethod.GET)
    public Object hotProductList(@RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize,
                                       @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        PmsProductQueryParam productQueryParam = new PmsProductQueryParam();
        productQueryParam.setPageNum(pageNum);productQueryParam.setPageSize(pageSize);
        List<PmsProduct> productList = pmsProductService.list(productQueryParam);
        return new CommonResult().success(productList);
    }
    @IgnoreAuth
    @ApiOperation("分页获取推荐商品")
    @RequestMapping(value = "/newProductList", method = RequestMethod.GET)
    public Object newProductList(@RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize,
                                 @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        PmsProductQueryParam productQueryParam = new PmsProductQueryParam();
        productQueryParam.setPageNum(pageNum);productQueryParam.setPageSize(pageSize);
        List<PmsProduct> productList = pmsProductService.list(productQueryParam);
        return new CommonResult().success(productList);
    }
    @IgnoreAuth
    @ApiOperation("分页获取推荐商品")
    @RequestMapping(value = "/skillGoods", method = RequestMethod.GET)
    public Object skillGoods(@RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize,
                                 @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        HomeFlashPromotion productList = homeService.getHomeFlashPromotion();
        return new CommonResult().success(productList);
    }

    @IgnoreAuth
    @ApiOperation("获取首页商品分类")
    @RequestMapping(value = "/productCateList", method = RequestMethod.GET)
    public Object getProductCateList(@RequestParam(value = "parentId", required = false, defaultValue = "0") Long parentId) {
        List<PmsProductCategory> productCategoryList = homeService.getProductCateList(parentId);
        return new CommonResult().success(productCategoryList);
    }
    @IgnoreAuth
    @ApiOperation("根据分类获取专题")
    @RequestMapping(value = "/subjectList", method = RequestMethod.GET)
    public Object getSubjectList(@RequestParam(required = false) Long cateId,
                                 @RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize,
                                 @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<CmsSubject> subjectList = homeService.getSubjectList(cateId, pageSize, pageNum);
        return new CommonResult().success(subjectList);
    }
    @IgnoreAuth
    @GetMapping(value = "/subjectDetail")
    @ApiOperation(value = "据分类获取专题")
    public Object subjectDetail(@RequestParam(value = "id", required = false, defaultValue = "0") Long id) {
        CmsSubject cmsSubject = subjectService.selectByPrimaryKey(id);
        UmsMember umsMember = this.getCurrentMember();
        if (umsMember != null && umsMember.getId() != null) {
            MemberProductCollection findCollection = productCollectionRepository.findByMemberIdAndProductId(
                    umsMember.getId(), id);
            if(findCollection!=null){
                cmsSubject.setIs_favorite(1);
            }else{
                cmsSubject.setIs_favorite(2);
            }
        }
        return new CommonResult().success(cmsSubject);
    }



    /**
     * banner
     *
     * @return
     */
    @IgnoreAuth
    @GetMapping("/bannerList")
    public Object bannerList(@RequestParam(value = "type", required = false, defaultValue = "10") Integer type) {
        List<SmsHomeAdvertise> bannerList = null;
        String bannerJson = redisService.get(RedisKey.appletBannerKey+type);
        if(bannerJson!=null && bannerJson!="[]"){
            bannerList = JsonUtil.jsonToList(bannerJson,SmsHomeAdvertise.class);
        }else {
            bannerList = advertiseService.list(null, type, null, 5, 1);
            redisService.set(RedisKey.appletBannerKey+type,JsonUtil.objectToJson(bannerList));
            redisService.expire(RedisKey.appletBannerKey+type,24*60*60);
        }
      //  List<SmsHomeAdvertise> bannerList = advertiseService.list(null, type, null, 5, 1);
        return new CommonResult().success(bannerList);
    }


    @IgnoreAuth
    @RequestMapping(value = "/navList",method = RequestMethod.GET)
    @ApiOperation(value = "获取导航栏")
    public Object getNavList(){

        return new CommonResult().success(null);
    }

    @RequestMapping(value = "/member/geetestInit",method = RequestMethod.GET)
    @ApiOperation(value = "极验初始化")
    public String geetesrInit(HttpServletRequest request){

        GeetestLib gtSdk = new GeetestLib(GeetestLib.id, GeetestLib.key,GeetestLib.newfailback);

        String resStr = "{}";

        //自定义参数,可选择添加
        HashMap<String, String> param = new HashMap<String, String>();

        //进行验证预处理
        int gtServerStatus = gtSdk.preProcess(param);

        //将服务器状态设置到redis中

        String key = UUID.randomUUID().toString();
     //   jedisClient.set(key,gtServerStatus+"");
       // jedisClient.expire(key,360);

        resStr = gtSdk.getResponseStr();
        GeetInit geetInit = JsonUtil.jsonToPojo(resStr,GeetInit.class);
               // new Gson().fromJson(resStr,GeetInit.class);
        geetInit.setStatusKey(key);
        return JsonUtil.objectToJson(geetInit);
    }


    @IgnoreAuth
    @ApiOperation("分页获取推荐商品")
    @RequestMapping(value = "/getHomeCouponList", method = RequestMethod.GET)
    public Object getHomeCouponList() {
        List<SmsCoupon> couponList = new ArrayList<>();
        UmsMember umsMember = this.getCurrentMember();
        if (umsMember != null && umsMember.getId() != null) {
            couponList = umsMemberCouponService.selectNotRecive(umsMember.getId());
        }
        return new CommonResult().success(couponList);
    }
}
