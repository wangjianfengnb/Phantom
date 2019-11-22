package com.phantom.dispatcher.message.wrapper;

import com.phantom.common.C2GMessageRequest;
import lombok.Builder;
import lombok.Getter;

/**
 * 对C2G请求包装，因为封住的时候发现经常会需要对protobuf进行序列化，搞一个holder的东西，保存起来，避免多次序列化
 *
 * @author Jianfeng Wang
 * @since 2019/11/19 17:35
 */
@Builder
@Getter
public class C2gMessageRequestWrapper implements Identifyable {

    private C2GMessageRequest c2GMessageRequest;

    public static C2gMessageRequestWrapper create(C2GMessageRequest request) {
        return C2gMessageRequestWrapper.builder()
                .c2GMessageRequest(request)
                .build();
    }

    @Override
    public String getUid() {
        return c2GMessageRequest.getSenderId();
    }
}
