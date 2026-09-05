package cn.bugstack.trigger.http;

import cn.bugstack.api.request.coupon.CouponAvailablePageRequest;
import cn.bugstack.api.request.coupon.CouponClaimRequest;
import cn.bugstack.api.request.coupon.CouponPageRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.coupon.CouponDetailResponse;
import cn.bugstack.api.response.page.PageResponse;
import cn.bugstack.domain.groupbuy.model.entity.CouponEntity;
import cn.bugstack.domain.groupbuy.service.coupon.ICouponCenterService;
import cn.bugstack.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 领券中心 Controller
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/coupon/")
public class CouponCenterController {

    @Resource
    private ICouponCenterService couponCenterService;

    /**
     * 分页查询优惠券列表（公开接口）
     */
    @RequestMapping(value = "queryCouponPage", method = RequestMethod.POST)
    public Response<PageResponse<CouponDetailResponse>> queryCouponPage(@RequestBody CouponPageRequest request) {
        log.info("领券中心-分页查询开始 pageNo:{} pageSize:{} couponType:{}", request.getPageNo(), request.getPageSize(), request.getCouponType());
        try {
            List<CouponEntity> couponList = couponCenterService.queryCouponPage(
                    request.getStatus(), request.getCouponType(), request.getPageNo(), request.getPageSize());
            long total = couponCenterService.countCouponPage(request.getStatus(), request.getCouponType());

            List<CouponDetailResponse> detailList = couponList.stream()
                    .map(coupon -> CouponDetailResponse.builder()
                            .couponId(coupon.getCouponId())
                            .couponName(coupon.getCouponName())
                            .couponType(coupon.getCouponType())
                            .thresholdAmount(coupon.getThresholdAmount())
                            .discountAmount(coupon.getDiscountAmount())
                            .discountRate(coupon.getDiscountRate())
                            .status(coupon.getStatus())
                            .startTime(coupon.getStartTime())
                            .endTime(coupon.getEndTime())
                            .build())
                    .collect(Collectors.toList());

            PageResponse<CouponDetailResponse> pageResponse = PageResponse.<CouponDetailResponse>builder()
                    .total(total)
                    .pageNo(request.getPageNo())
                    .pageSize(request.getPageSize())
                    .list(detailList)
                    .build();

            log.info("领券中心-分页查询完成 total:{}", total);
            return Response.<PageResponse<CouponDetailResponse>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("领券中心-分页查询失败", e);
            return Response.<PageResponse<CouponDetailResponse>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 领取优惠券（需要登录）
     */
    @RequestMapping(value = "claimCoupon", method = RequestMethod.POST)
    public Response<Void> claimCoupon(@RequestBody CouponClaimRequest request) {
        log.info("领券中心-开始领券 couponId:{}", request.getCouponId());
        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String userId = (String) httpRequest.getAttribute("userId");

            if (StringUtils.isBlank(userId)) {
                log.warn("领券中心-缺少登录态，拒绝处理");
                return Response.<Void>builder()
                        .code(ResponseCode.NO_LOGIN.getCode())
                        .info(ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }

            couponCenterService.claimCoupon(userId, request.getCouponId());

            log.info("领券中心-领券成功 couponId:{}", request.getCouponId());
            return Response.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("领券中心-领券失败 couponId:{}", request.getCouponId(), e);
            return Response.<Void>builder()
                    .code(e instanceof cn.bugstack.types.exception.AppException
                            ? ((cn.bugstack.types.exception.AppException) e).getCode()
                            : ResponseCode.UN_ERROR.getCode())
                    .info(e.getMessage() != null ? e.getMessage() : ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 分页查询当前用户可用优惠券（需要登录，返回 status=0、启用且未过期的券）
     */
    @RequestMapping(value = "queryMyAvailableCoupons", method = RequestMethod.POST)
    public Response<PageResponse<CouponDetailResponse>> queryMyAvailableCoupons(@RequestBody CouponAvailablePageRequest request) {
        log.info("领券中心-查询用户可用券 pageNo:{} pageSize:{} couponType:{}", request.getPageNo(), request.getPageSize(), request.getCouponType());
        try {
            HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String userId = (String) httpRequest.getAttribute("userId");

            if (StringUtils.isBlank(userId)) {
                log.warn("领券中心-查询可用券缺少登录态，拒绝处理");
                return Response.<PageResponse<CouponDetailResponse>>builder()
                        .code(ResponseCode.NO_LOGIN.getCode())
                        .info(ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }

            int pageNo = request.getSafePageNo();
            int pageSize = request.getSafePageSize();
            List<CouponEntity> couponList = couponCenterService.queryMyAvailableCoupons(userId, request.getCouponType(), pageNo, pageSize);
            long total = couponCenterService.countMyAvailableCoupons(userId, request.getCouponType());

            List<CouponDetailResponse> detailList = couponList.stream()
                    .map(coupon -> CouponDetailResponse.builder()
                            .couponId(coupon.getCouponId())
                            .couponName(coupon.getCouponName())
                            .couponType(coupon.getCouponType())
                            .thresholdAmount(coupon.getThresholdAmount())
                            .discountAmount(coupon.getDiscountAmount())
                            .discountRate(coupon.getDiscountRate())
                            .status(coupon.getStatus())
                            .startTime(coupon.getStartTime())
                            .endTime(coupon.getEndTime())
                            .build())
                    .collect(Collectors.toList());

            PageResponse<CouponDetailResponse> pageResponse = PageResponse.<CouponDetailResponse>builder()
                    .total(total)
                    .pageNo(pageNo)
                    .pageSize(pageSize)
                    .list(detailList)
                    .build();

            log.info("领券中心-查询用户可用券完成 total:{} 本页:{}", total, detailList.size());
            return Response.<PageResponse<CouponDetailResponse>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("领券中心-查询用户可用券失败", e);
            return Response.<PageResponse<CouponDetailResponse>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

}
