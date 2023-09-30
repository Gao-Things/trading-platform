package com.usyd.capstone.service;

import com.github.dreamyoung.mprelation.IService;
import com.usyd.capstone.common.DTO.Result;
import com.usyd.capstone.entity.NormalUser;

public interface NormalUserService {

    public Result setPriceThresholdSingle(String token, Long productId, boolean isMinimum, double threshold);

    public Result setPriceThresholdBatch(String token, Long productId, boolean isMinimum, double threshold);

    public Result makeAnOffer(String token, Long productId, String note, double price);

    public Result acceptAnOffer(String token, Long offerId);

    public Result cancelAnOffer(String token, Long offerId);
}
