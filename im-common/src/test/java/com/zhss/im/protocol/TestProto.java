package com.zhss.im.protocol;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * 测试Protobuf序列化
 *
 * @author Jianfeng Wang
 * @since 2019/10/28 22:18
 */
public class TestProto {

    public static void main(String[] args) throws InvalidProtocolBufferException {
        AuthenticateRequest request = AuthenticateRequest.newBuilder()
                .setToken("test_token_001")
                .setUid("test_uid_001")
                .setTimestamp(System.currentTimeMillis())
                .build();

        System.out.println("before encode : " + request.toString());
        AuthenticateRequest authenticateRequest =
                AuthenticateRequest.parseFrom(request.toByteArray());
        System.out.println("after decode : " + authenticateRequest);
        System.out.println("Assert equal : " + authenticateRequest.equals(request));

    }

}
