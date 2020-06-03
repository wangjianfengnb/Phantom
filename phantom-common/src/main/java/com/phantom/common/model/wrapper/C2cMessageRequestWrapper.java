package com.phantom.common.model.wrapper;

import com.phantom.common.C2cMessageRequest;
import lombok.Builder;
import lombok.Getter;

/**
 * 对C2C请求包装，因为封住的时候发现经常会需要对protobuf进行序列化，搞一个holder的东西，保存起来，避免多次序列化
 *
 * @author Jianfeng Wang
 * @since 2019/11/19 17:26
 */
@Builder
@Getter
public class C2cMessageRequestWrapper {

    private C2cMessageRequest c2cMessageRequest;

    public static C2cMessageRequestWrapper create(C2cMessageRequest request) {
        return C2cMessageRequestWrapper.builder()
                .c2cMessageRequest(request)
                .build();
    }


}
