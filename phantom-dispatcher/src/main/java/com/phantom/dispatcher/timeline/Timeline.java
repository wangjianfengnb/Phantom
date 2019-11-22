package com.phantom.dispatcher.timeline;

import java.util.List;

/**
 * 基于TimeLine模型
 *
 * @author Jianfeng Wang
 * @since 2019/11/13 13:52
 */
public interface Timeline {

    /**
     * 从timeline抓取用户的离线消息
     * <p>
     * 考虑这样一种情况，现在timeline的的消息由于某些原因存放消息是这样的
     * <p>
     * 1，2,3,4,6  此时客户端根据sequence = 0来抓数据，抓到了1，2,3,4,6,
     * <p>
     * 此时客户端会发现少了5，此时timeline模型数据变成了1,2,3,4,5,6,
     * <p>
     * 客户端发现抓到的数据不是顺序的，则会丢弃，下一次过来抓取的时候，根据sequence = 4来抓，此时就可以抓到5,6消息
     * <p>
     * <p>
     * timeline逻辑为：每次抓的时候，删除小于或等于sequence的消息，
     * 客户端的处理逻辑为：抓取到的消息按照消息顺序严格递增，符合则保存，从不符合的消息开始后面都丢弃
     * <p>
     * 对于多端同步的处理，因为目前对于某一个用户而言，他只有一个timeline，所有消息都是保存在timeline中的，
     * <p>
     * 需要额外维护一个数据结构用于保存各个端当前同步消息的sequence是多少，假如现在timeline中数据为：
     * <p>
     * 1,2,3,4,5,6  但是支持APP端和Web端，APP端的sequence为10，web端的offset为5，则把他们两个的最小值5之前的消息清除。
     * <p>
     * <p>
     * <p>
     * <p>
     * 考虑这样一种情况：假如APP一直登录，但是WEB过了很久都不登录，这有可能导致Timeline中的消息列表过大。
     * <p>
     * 如果多个端相差过多，比如超过200条记录(可配置),则只保留最新的200条记录。
     * <p>
     * 例如：web的sequence = 100，app的sequence为1000，则会调整web的sequence为800，web端会丢失600条消息
     *
     * @param request 请求参数
     * @return 消息，如果没有消息返回空集合
     */
    List<TimelineMessage> fetchMessage(FetchRequest request);


    /**
     * 保存消息到timeline
     * <p>
     * 按照时间为消息排序，同时为消息生成一个唯一的严格递增的ID,用于保证消息顺序性。
     *
     * @param timelineMessage 消息
     */
    void saveMessage(TimelineMessage timelineMessage);

}
