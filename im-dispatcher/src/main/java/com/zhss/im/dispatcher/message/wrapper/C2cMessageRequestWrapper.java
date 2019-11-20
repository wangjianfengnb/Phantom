package com.zhss.im.dispatcher.message.wrapper;

import com.zhss.im.common.C2CMessageRequest;
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
public class C2cMessageRequestWrapper implements Identifyable {

    private C2CMessageRequest c2cMessageRequest;

    @Override
    public String getUid() {
        return c2cMessageRequest.getSenderId();
    }

    public static C2cMessageRequestWrapper create(C2CMessageRequest request) {
        return C2cMessageRequestWrapper.builder()
                .c2cMessageRequest(request)
                .build();
    }


}
